package mybatis.generator.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import mybatis.generator.plugin.tools.ClassElementGeneratorUtil;
import mybatis.generator.plugin.tools.XmlElementGeneratorUtil;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.IntrospectedTable.TargetRuntime;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * Created by Jeff_xu on 2020/11/30.
 * 根据 example 查询，返回结果仅取第一条记录插件
 *
 * @author Jeff_xu
 */
public class SelectOneByExamplePlugin extends PluginAdapter {

    private static final String SELECT_ONE_BY_EXAMPLE_METHOD_NAME = "selectOneByExample";
    private static final String SELECT_ONE_BY_EXAMPLE_WITH_BLOBS_METHOD_NAME = "selectOneByExampleWithBlobs";

    private Map<FullyQualifiedTable, List<XmlElement>> elementsToAdd;

    public SelectOneByExamplePlugin() {
        elementsToAdd = new HashMap<>();
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {

        Method newMethod = ClassElementGeneratorUtil.generateMethod(
            SELECT_ONE_BY_EXAMPLE_METHOD_NAME,
            JavaVisibility.PUBLIC,
            new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()),
            new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example")
        );
        ClassElementGeneratorUtil.addMethodWithBestPosition(interfaze, newMethod);

        if (introspectedTable.hasBLOBColumns()) {
            Method newMethodWithBlobs = ClassElementGeneratorUtil.generateMethod(
                SELECT_ONE_BY_EXAMPLE_WITH_BLOBS_METHOD_NAME,
                JavaVisibility.PUBLIC,
                introspectedTable.getRules().calculateAllFieldsClass(),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example")
            );
            ClassElementGeneratorUtil.addMethodWithBestPosition(interfaze, newMethodWithBlobs);
        }

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        interfaze.addImportedType(type);
        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        List<XmlElement> xmlElementList = Lists.newArrayList();
        xmlElementList.add(generateSelectOneXmlElement(introspectedTable));
        if (introspectedTable.hasBLOBColumns()) {
            xmlElementList.add(generateSelectOneWithBlobsXmlElement(introspectedTable));
        }
        elementsToAdd.put(introspectedTable.getFullyQualifiedTable(), xmlElementList);
        return super.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    public XmlElement generateSelectOneXmlElement(IntrospectedTable introspectedTable) {
        XmlElement selectOneElement = new XmlElement("select");

        // 添加ID
        selectOneElement.addAttribute(new Attribute("id", SELECT_ONE_BY_EXAMPLE_METHOD_NAME));
        // 添加返回类型
        selectOneElement.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
        // 添加参数类型
        selectOneElement.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
        selectOneElement.addElement(new TextElement("select"));

        StringBuilder sb = new StringBuilder();
        selectOneElement.addElement(XmlElementGeneratorUtil.getBaseColumnListElement(introspectedTable));

        sb.setLength(0);
        sb.append("from ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        selectOneElement.addElement(new TextElement(sb.toString()));
        selectOneElement.addElement(XmlElementGeneratorUtil.getExampleIncludeElement(introspectedTable));

        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "orderByClause != null"));
        ifElement.addElement(new TextElement("order by ${orderByClause}"));
        selectOneElement.addElement(ifElement);

        // 只查询一条
        selectOneElement.addElement(new TextElement("limit 1"));
        return selectOneElement;
    }

    public XmlElement generateSelectOneWithBlobsXmlElement(IntrospectedTable introspectedTable) {
        XmlElement selectOneElement = new XmlElement("select");

        // 添加ID
        selectOneElement.addAttribute(new Attribute("id", SELECT_ONE_BY_EXAMPLE_WITH_BLOBS_METHOD_NAME));
        // 添加返回类型
        selectOneElement.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
        // 添加参数类型
        selectOneElement.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
        selectOneElement.addElement(new TextElement("select"));

        selectOneElement.addElement(XmlElementGeneratorUtil.getBaseColumnListElement(introspectedTable));
        selectOneElement.addElement(new TextElement(","));
        selectOneElement.addElement(XmlElementGeneratorUtil.getBlobColumnListElement(introspectedTable));

        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        sb.append("from ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        selectOneElement.addElement(new TextElement(sb.toString()));
        selectOneElement.addElement(XmlElementGeneratorUtil.getExampleIncludeElement(introspectedTable));

        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "orderByClause != null"));
        ifElement.addElement(new TextElement("order by ${orderByClause}"));
        selectOneElement.addElement(ifElement);

        // 只查询一条
        selectOneElement.addElement(new TextElement("limit 1"));
        return selectOneElement;
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
}
