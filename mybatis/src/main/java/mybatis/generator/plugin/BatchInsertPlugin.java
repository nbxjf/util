package mybatis.generator.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mybatis.generator.plugin.tools.ClassElementGeneratorUtil;
import org.apache.commons.collections.CollectionUtils;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.IntrospectedTable.TargetRuntime;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.ibatis2.Ibatis2FormattingUtilities;

/**
 * Created by Jeff_xu on 2020/12/3.
 * batchInsert 插件
 * 条件：
 * 1. Mybatis 版本大于等于1.3
 * 2. 不能对参数设置别名，默认是 list
 * 3. 设置 useGeneratedKeys="true" keyProperty="id" 用户主键 id 回写
 *
 * @author Jeff_xu
 */
public class BatchInsertPlugin extends PluginAdapter {

    private static final String BATCH_INSERT_METHOD_NAME = "batchInsert";

    private static final String BATCH_INSERT_SELECTIVE_METHOD_NAME = "batchInsertSelective";

    private Map<FullyQualifiedTable, List<XmlElement>> elementsToAdd;

    public BatchInsertPlugin() {
        elementsToAdd = new HashMap<>();
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze,
                                               IntrospectedTable introspectedTable) {

        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        listType.addTypeArgument(introspectedTable.getRules().calculateAllFieldsClass());

        Method newMethod = ClassElementGeneratorUtil.generateMethod(
            BATCH_INSERT_METHOD_NAME,
            JavaVisibility.PUBLIC,
            new FullyQualifiedJavaType("long"),
            new Parameter(listType, "list")
        );
        ClassElementGeneratorUtil.addMethodWithBestPosition(interfaze, newMethod);

        Method newSelectiveMethod = ClassElementGeneratorUtil.generateMethod(
            BATCH_INSERT_SELECTIVE_METHOD_NAME,
            JavaVisibility.PUBLIC,
            new FullyQualifiedJavaType("long"),
            new Parameter(listType, "list")
        );
        ClassElementGeneratorUtil.addMethodWithBestPosition(interfaze, newSelectiveMethod);

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        interfaze.addImportedType(type);
        return true;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        List<XmlElement> xmlElementList = new ArrayList<>();
        xmlElementList.add(generatorInsertSelectiveXml(introspectedTable));
        xmlElementList.add(generatorBatchInsertXml(introspectedTable));
        elementsToAdd.put(introspectedTable.getFullyQualifiedTable(), xmlElementList);
        return super.sqlMapInsertElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document,
                                           IntrospectedTable introspectedTable) {
        List<XmlElement> elements = elementsToAdd.get(introspectedTable.getFullyQualifiedTable());
        if (elements != null) {
            for (XmlElement element : elements) {
                document.getRootElement().addElement(element);
            }
        }
        return true;
    }

    @Override
    public boolean validate(List<String> warnings) {
        return context.getTargetRuntime().equalsIgnoreCase(TargetRuntime.MYBATIS3.name());
    }

    private XmlElement generatorInsertSelectiveXml(IntrospectedTable introspectedTable) {
        XmlElement batchInsetXmlElement = new XmlElement("insert");

        batchInsetXmlElement.addAttribute(new Attribute("id", BATCH_INSERT_SELECTIVE_METHOD_NAME));
        batchInsetXmlElement.addAttribute(new Attribute("parameterType", "java.util.List"));
        batchInsetXmlElement.addAttribute(new Attribute("useGeneratedKeys", "true"));
        if (CollectionUtils.isNotEmpty(introspectedTable.getPrimaryKeyColumns())) {
            batchInsetXmlElement.addAttribute(new Attribute("keyProperty", introspectedTable.getPrimaryKeyColumns().get(0).getJavaProperty()));
        }

        StringBuilder insertClause = new StringBuilder();

        // 构造 foreach 标签
        //<foreach collection="list" item="item" index="index" separator=",">
        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("separator", ";"));

        insertClause.append("INSERT INTO ");
        insertClause.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        // insert into xxxx（table）
        foreachElement.addElement(new TextElement(insertClause.toString()));

        XmlElement columnTrimXmlElement = new XmlElement("trim");
        columnTrimXmlElement.addAttribute(new Attribute("prefix", "("));
        columnTrimXmlElement.addAttribute(new Attribute("suffix", ")"));
        columnTrimXmlElement.addAttribute(new Attribute("suffixOverrides", ","));

        XmlElement valueTrimXmlElement = new XmlElement("trim");
        valueTrimXmlElement.addAttribute(new Attribute("prefix", "values ("));
        valueTrimXmlElement.addAttribute(new Attribute("suffix", ")"));
        valueTrimXmlElement.addAttribute(new Attribute("suffixOverrides", ","));

        Iterator<IntrospectedColumn> iter = introspectedTable.getAllColumns().iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();
            if (introspectedColumn.isIdentity()) {
                // cannot set values on identity fields
                continue;
            }
            // <if test="activityId != null" >
            // activity_id,
            // </if>
            XmlElement columnIfElement = new XmlElement("if");
            columnIfElement.addAttribute(new Attribute("test", "item." + introspectedColumn.getJavaProperty() + " != null"));
            columnIfElement.addElement(new TextElement(Ibatis2FormattingUtilities.getEscapedColumnName(introspectedColumn) + ","));
            columnTrimXmlElement.addElement(columnIfElement);
            //  <if test="activityId != null" >
            //  #{activityId,jdbcType=INTEGER},
            //  </if>

            XmlElement valueIfElement = new XmlElement("if");
            valueIfElement.addAttribute(new Attribute("test", "item." + introspectedColumn.getJavaProperty() + " != null"));
            valueIfElement.addElement(new TextElement("#{item." + introspectedColumn.getJavaProperty() + ",jdbcType=" + introspectedColumn.getJdbcTypeName() + "},"));
            valueTrimXmlElement.addElement(valueIfElement);
        }
        foreachElement.addElement(columnTrimXmlElement);
        foreachElement.addElement(valueTrimXmlElement);

        batchInsetXmlElement.addElement(foreachElement);
        return batchInsetXmlElement;
    }

    private XmlElement generatorBatchInsertXml(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("insert");

        answer.addAttribute(new Attribute("id", BATCH_INSERT_METHOD_NAME));
        answer.addAttribute(new Attribute("parameterType", "java.util.List"));
        answer.addAttribute(new Attribute("useGeneratedKeys", "true"));
        if (CollectionUtils.isNotEmpty(introspectedTable.getPrimaryKeyColumns())) {
            answer.addAttribute(new Attribute("keyProperty", introspectedTable.getPrimaryKeyColumns().get(0).getJavaProperty()));
        }

        StringBuilder insertClause = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();

        // 构造 foreach 标签
        //<foreach collection="list" item="item" index="index" separator=",">
        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("separator", ","));

        insertClause.append("INSERT INTO ");
        insertClause.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        insertClause.append(" (");
        valuesClause.append(" (");
        List<String> valuesClauses = new ArrayList<String>();
        Iterator<IntrospectedColumn> iter = introspectedTable.getAllColumns().iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();
            if (introspectedColumn.isIdentity()) {
                // cannot set values on identity fields
                continue;
            }

            insertClause.append(Ibatis2FormattingUtilities.getEscapedColumnName(introspectedColumn));
            valuesClause.append("#{item.").append(introspectedColumn.getJavaProperty()).append(",jdbcType=").append(introspectedColumn.getJdbcTypeName()).append('}');
            if (iter.hasNext()) {
                insertClause.append(", ");
                valuesClause.append(", ");
            }

            if (valuesClause.length() > 80) {
                answer.addElement(new TextElement(insertClause.toString()));
                insertClause.setLength(0);
                OutputUtilities.xmlIndent(insertClause, 1);

                valuesClauses.add(valuesClause.toString());
                valuesClause.setLength(0);
                OutputUtilities.xmlIndent(valuesClause, 1);
            }
        }

        insertClause.append(')');
        answer.addElement(new TextElement(insertClause.toString()));
        answer.addElement(new TextElement("VALUES"));

        valuesClause.append(')');
        valuesClauses.add(valuesClause.toString());
        for (String clause : valuesClauses) {
            foreachElement.addElement(new TextElement(clause));
        }
        answer.addElement(foreachElement);
        return answer;
    }

}
