package mybatis.generator.plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import mybatis.generator.plugin.tools.ClassElementGeneratorUtil;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.IntrospectedTable.TargetRuntime;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.ibatis2.Ibatis2FormattingUtilities;
import org.mybatis.generator.config.PluginConfiguration;

/**
 * Created by Jeff_xu on 2020/12/6.
 *
 * @author Jeff_xu
 */
@Slf4j
public class BatchUpdatePlugin extends PluginAdapter {

    private static final String BATCH_UPDATE_BY_ID_METHOD_NAME = "batchUpdateByPrimaryKeySelective";
    private static final String BATCH_UPDATE_BY_ID_WITH_BLOBS_METHOD_NAME = "batchUpdateByPrimaryKeySelectiveWithBlobs";

    private static final String BATCH_UPDATE_BY_ASSIGNED_COLUMN_METHOD_NAME = "batchUpdateByAssignedColumnSelective";
    private static final String BATCH_UPDATE_BY_ASSIGNED_COLUMN_WITH_BLOBS_METHOD_NAME = "batchUpdateByAssignedColumnSelectiveWithBlobs";

    private Map<FullyQualifiedTable, List<XmlElement>> elementsToAdd;

    /**
     * 是否需要生成指定字段 batchUpdate 的方法
     * 默认为 true
     */
    private boolean generateAssignedColumnsMethod;

    public BatchUpdatePlugin() {
        elementsToAdd = new HashMap<>();
        generateAssignedColumnsMethod = true;
    }

    public BatchUpdatePlugin(boolean generateAssignedColumnsMethod) {
        elementsToAdd = new HashMap<>();
        this.generateAssignedColumnsMethod = generateAssignedColumnsMethod;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean validate(List<String> warnings) {
        if (!context.getTargetRuntime().equalsIgnoreCase(TargetRuntime.MYBATIS3.name())) {
            return false;
        }
        if (generateAssignedColumnsMethod) {
            try {
                Field field = context.getClass().getDeclaredField("pluginConfigurations");
                field.setAccessible(true);
                List<PluginConfiguration> pluginConfigurations = (List<PluginConfiguration>)field.get(context);

                for (PluginConfiguration pluginConfiguration : pluginConfigurations) {
                    if (ColumnModelPlugin.class.getName().equals(pluginConfiguration.getConfigurationType())) {
                        return true;
                    }
                }
                log.error("执行BatchUpdatePlugin插件需要依赖ColumnModelPlugin插件，请添加ColumnModelPlugin后重新执行");
            } catch (Exception e) {
                log.error("校验ColumnModelPlugin失败", e);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {

        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        listType.addTypeArgument(introspectedTable.getRules().calculateAllFieldsClass());

        Method batchUpdateByIdSelectiveMethod = ClassElementGeneratorUtil.generateMethod(
            BATCH_UPDATE_BY_ID_METHOD_NAME,
            JavaVisibility.PUBLIC,
            new FullyQualifiedJavaType("long"),
            new Parameter(listType, "list", "@Param(\"list\")")
        );
        ClassElementGeneratorUtil.addMethodWithBestPosition(interfaze, batchUpdateByIdSelectiveMethod);

        if (introspectedTable.hasBLOBColumns()) {
            Method batchUpdateByIdSelectiveWithBlobsMethod = ClassElementGeneratorUtil.generateMethod(
                BATCH_UPDATE_BY_ID_WITH_BLOBS_METHOD_NAME,
                JavaVisibility.PUBLIC,
                new FullyQualifiedJavaType("long"),
                new Parameter(listType, "list", "@Param(\"list\")")
            );
            ClassElementGeneratorUtil.addMethodWithBestPosition(interfaze, batchUpdateByIdSelectiveWithBlobsMethod);
        }

        if (generateAssignedColumnsMethod) {
            FullyQualifiedJavaType selectiveType = new FullyQualifiedJavaType(introspectedTable.getRules().calculateAllFieldsClass().getShortName() + "." + ColumnModelPlugin.ENUM_NAME);
            Method batchUpdateByAssignedColumnSelectiveMethod = ClassElementGeneratorUtil.generateMethod(
                BATCH_UPDATE_BY_ASSIGNED_COLUMN_METHOD_NAME,
                JavaVisibility.PUBLIC,
                new FullyQualifiedJavaType("long"),
                new Parameter(listType, "list", "@Param(\"list\")"),
                new Parameter(selectiveType, "dependAssignedColumns", "@Param(\"dependAssignedColumns\")", true)
            );
            ClassElementGeneratorUtil.addMethodWithBestPosition(interfaze, batchUpdateByAssignedColumnSelectiveMethod);

            if (introspectedTable.hasBLOBColumns()) {
                Method batchUpdateByAssignedColumnSelectiveWithBlobsMethod = ClassElementGeneratorUtil.generateMethod(
                    BATCH_UPDATE_BY_ASSIGNED_COLUMN_WITH_BLOBS_METHOD_NAME,
                    JavaVisibility.PUBLIC,
                    new FullyQualifiedJavaType("long"),
                    new Parameter(listType, "list", "@Param(\"list\")"),
                    new Parameter(selectiveType, "dependAssignedColumns", "@Param(\"dependAssignedColumns\")", true)
                );
                ClassElementGeneratorUtil.addMethodWithBestPosition(interfaze, batchUpdateByAssignedColumnSelectiveWithBlobsMethod);
            }
        }

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        interfaze.addImportedType(type);

        return true;

    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        return super.clientUpdateByExampleWithBLOBsMethodGenerated(method, topLevelClass, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        List<XmlElement> xmlElementList = new ArrayList<>();
        xmlElementList.add(generatorBatchUpdateByPrimaryKeySelective(introspectedTable));
        if (introspectedTable.hasBLOBColumns()) {
            xmlElementList.add(generatorBatchUpdateByPrimaryKeySelectiveWithBlobs(introspectedTable));
        }

        if (generateAssignedColumnsMethod) {
            xmlElementList.add(generatorBatchUpdateByAssignedColumnsSelective(introspectedTable));
            if (introspectedTable.hasBLOBColumns()) {
                xmlElementList.add(generatorBatchUpdateByAssignedColumnsSelectiveWithBlobs(introspectedTable));
            }
        }
        elementsToAdd.put(introspectedTable.getFullyQualifiedTable(), xmlElementList);
        return super.sqlMapUpdateByExampleSelectiveElementGenerated(element, introspectedTable);
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

    private XmlElement generatorBatchUpdateByPrimaryKeySelective(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("update");

        answer.addAttribute(new Attribute("id", BATCH_UPDATE_BY_ID_METHOD_NAME));
        context.getCommentGenerator().addComment(answer);

        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("separator", ";"));
        StringBuilder sb = new StringBuilder();

        sb.append("update ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        foreachElement.addElement(new TextElement(sb.toString()));

        XmlElement setElement = new XmlElement("set");

        for (IntrospectedColumn introspectedColumn : introspectedTable.getBaseColumns()) {
            XmlElement isNotNullElement = new XmlElement("if");
            isNotNullElement.addAttribute(new Attribute("test", introspectedColumn.getJavaProperty("item.") + " != null"));

            sb.setLength(0);
            sb.append(Ibatis2FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append("#{item.").append(introspectedColumn.getJavaProperty()).append(",jdbcType=").append(introspectedColumn.getJdbcTypeName()).append("},");

            isNotNullElement.addElement(new TextElement(sb.toString()));
            setElement.addElement(isNotNullElement);
        }

        foreachElement.addElement(setElement);

        sb.setLength(0);
        IntrospectedColumn primaryColumn = introspectedTable.getPrimaryKeyColumns().get(0);
        sb.append("where ")
            .append(Ibatis2FormattingUtilities.getAliasedEscapedColumnName(primaryColumn))
            .append(" = ")
            .append("#{item.").append(primaryColumn.getJavaProperty()).append(",jdbcType=").append(primaryColumn.getJdbcTypeName()).append('}');

        foreachElement.addElement(new TextElement(sb.toString()));
        answer.addElement(foreachElement);
        return answer;
    }

    private XmlElement generatorBatchUpdateByPrimaryKeySelectiveWithBlobs(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("update");

        answer.addAttribute(new Attribute("id", BATCH_UPDATE_BY_ID_WITH_BLOBS_METHOD_NAME));
        context.getCommentGenerator().addComment(answer);

        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("separator", ";"));
        StringBuilder sb = new StringBuilder();

        sb.append("update ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        foreachElement.addElement(new TextElement(sb.toString()));

        XmlElement setElement = new XmlElement("set");

        for (IntrospectedColumn introspectedColumn : introspectedTable.getNonPrimaryKeyColumns()) {
            XmlElement isNotNullElement = new XmlElement("if");
            isNotNullElement.addAttribute(new Attribute("test", introspectedColumn.getJavaProperty("item.") + " != null"));

            sb.setLength(0);
            sb.append(Ibatis2FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append("#{item.").append(introspectedColumn.getJavaProperty()).append(",jdbcType=").append(introspectedColumn.getJdbcTypeName()).append("},");

            isNotNullElement.addElement(new TextElement(sb.toString()));
            setElement.addElement(isNotNullElement);
        }

        foreachElement.addElement(setElement);

        sb.setLength(0);
        IntrospectedColumn primaryColumn = introspectedTable.getPrimaryKeyColumns().get(0);
        sb.append("where ")
            .append(Ibatis2FormattingUtilities.getAliasedEscapedColumnName(primaryColumn))
            .append(" = ")
            .append("#{item.").append(primaryColumn.getJavaProperty()).append(",jdbcType=").append(primaryColumn.getJdbcTypeName()).append("}");

        foreachElement.addElement(new TextElement(sb.toString()));
        answer.addElement(foreachElement);
        return answer;
    }

    private XmlElement generatorBatchUpdateByAssignedColumnsSelective(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("update");

        answer.addAttribute(new Attribute("id", BATCH_UPDATE_BY_ASSIGNED_COLUMN_METHOD_NAME));
        context.getCommentGenerator().addComment(answer);

        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("separator", ";"));
        StringBuilder sb = new StringBuilder();

        sb.append("update ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        foreachElement.addElement(new TextElement(sb.toString()));

        XmlElement setElement = new XmlElement("set");

        for (IntrospectedColumn introspectedColumn : introspectedTable.getNonBLOBColumns()) {
            XmlElement isNotNullElement = new XmlElement("if");
            isNotNullElement.addAttribute(new Attribute("test", introspectedColumn.getJavaProperty("item.") + " != null"));

            sb.setLength(0);
            sb.append(Ibatis2FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append("#{item.").append(introspectedColumn.getJavaProperty()).append(",jdbcType=").append(introspectedColumn.getJdbcTypeName()).append("},");

            isNotNullElement.addElement(new TextElement(sb.toString()));
            setElement.addElement(isNotNullElement);
        }

        foreachElement.addElement(setElement);

        sb.setLength(0);
        sb.append("where ");
        foreachElement.addElement(new TextElement(sb.toString()));
        //<foreach collection="selective" item="column" separator=",">

        XmlElement whereForeachElement = new XmlElement("foreach");
        whereForeachElement.addAttribute(new Attribute("collection", "dependAssignedColumns"));
        whereForeachElement.addAttribute(new Attribute("item", "column"));
        whereForeachElement.addAttribute(new Attribute("separator", "and"));

        for (IntrospectedColumn introspectedColumn : introspectedTable.getNonBLOBColumns()) {
            XmlElement isEqualsElement = new XmlElement("if");
            isEqualsElement.addAttribute(new Attribute("test", "'" + introspectedColumn.getActualColumnName() + "'.toString() == column.value"));
            sb.setLength(0);
            sb.append(Ibatis2FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn))
                .append(" = ")
                .append("#{item.").append(introspectedColumn.getJavaProperty()).append(",jdbcType=").append(introspectedColumn.getJdbcTypeName()).append("}");
            isEqualsElement.addElement(new TextElement(sb.toString()));
            whereForeachElement.addElement(isEqualsElement);
        }

        foreachElement.addElement(whereForeachElement);

        answer.addElement(foreachElement);
        return answer;
    }

    private XmlElement generatorBatchUpdateByAssignedColumnsSelectiveWithBlobs(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("update");

        answer.addAttribute(new Attribute("id", BATCH_UPDATE_BY_ASSIGNED_COLUMN_WITH_BLOBS_METHOD_NAME));
        context.getCommentGenerator().addComment(answer);

        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("separator", ";"));
        StringBuilder sb = new StringBuilder();

        sb.append("update ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        foreachElement.addElement(new TextElement(sb.toString()));

        XmlElement setElement = new XmlElement("set");

        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            XmlElement isNotNullElement = new XmlElement("if");
            isNotNullElement.addAttribute(new Attribute("test", introspectedColumn.getJavaProperty("item.") + " != null"));

            sb.setLength(0);
            sb.append(Ibatis2FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append("#{item.").append(introspectedColumn.getJavaProperty()).append(",jdbcType=").append(introspectedColumn.getJdbcTypeName()).append("},");

            isNotNullElement.addElement(new TextElement(sb.toString()));
            setElement.addElement(isNotNullElement);
        }

        foreachElement.addElement(setElement);

        sb.setLength(0);
        sb.append("where ");
        foreachElement.addElement(new TextElement(sb.toString()));
        //<foreach collection="selective" item="column" separator=",">

        XmlElement whereForeachElement = new XmlElement("foreach");
        whereForeachElement.addAttribute(new Attribute("collection", "dependAssignedColumns"));
        whereForeachElement.addAttribute(new Attribute("item", "column"));
        whereForeachElement.addAttribute(new Attribute("separator", "and"));

        for (IntrospectedColumn introspectedColumn : introspectedTable.getNonBLOBColumns()) {
            XmlElement isEqualsElement = new XmlElement("if");
            isEqualsElement.addAttribute(new Attribute("test", "'" + introspectedColumn.getActualColumnName() + "'.toString() == column.value"));
            sb.setLength(0);
            sb.append(Ibatis2FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn))
                .append(" = ")
                .append("#{item.").append(introspectedColumn.getJavaProperty()).append(",jdbcType=").append(introspectedColumn.getJdbcTypeName()).append("}");

            isEqualsElement.addElement(new TextElement(sb.toString()));
            whereForeachElement.addElement(isEqualsElement);
        }

        foreachElement.addElement(whereForeachElement);

        answer.addElement(foreachElement);
        return answer;
    }

}
