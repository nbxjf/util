package mybatis.generator.plugin;

import java.util.List;

import mybatis.generator.plugin.tools.ClassElementGeneratorUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.PrimitiveTypeWrapper;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * Created by Jeff_xu on 2020/12/6.
 *
 * @author Jeff_xu
 */
public class LimitGeneratePlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        PrimitiveTypeWrapper integerWrapper = FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper();
        // 添加offset和rows字段
        Field offsetField = ClassElementGeneratorUtil.generateField("offset", JavaVisibility.PROTECTED, integerWrapper, null);
        topLevelClass.addField(offsetField);

        Field rowsField = ClassElementGeneratorUtil.generateField("rows", JavaVisibility.PROTECTED, integerWrapper, null);
        topLevelClass.addField(rowsField);

        // 增加getter && setter 方法
        Method mSetOffset = ClassElementGeneratorUtil.generateSetterMethod(offsetField);
        ClassElementGeneratorUtil.addMethodWithBestPosition(topLevelClass, mSetOffset);

        Method mGetOffset = ClassElementGeneratorUtil.generateGetterMethod(offsetField);
        ClassElementGeneratorUtil.addMethodWithBestPosition(topLevelClass, mGetOffset);

        Method mSetRows = ClassElementGeneratorUtil.generateSetterMethod(rowsField);
        ClassElementGeneratorUtil.addMethodWithBestPosition(topLevelClass, mSetRows);

        Method mGetRows = ClassElementGeneratorUtil.generateGetterMethod(rowsField);
        ClassElementGeneratorUtil.addMethodWithBestPosition(topLevelClass, mGetRows);

        // 提供几个快捷方法
        Method setLimit = ClassElementGeneratorUtil.generateMethod("limit", JavaVisibility.PUBLIC, topLevelClass.getType(), new Parameter(integerWrapper, "rows"));

        setLimit = ClassElementGeneratorUtil.generateMethodBody(setLimit, "this.rows = rows;", "return this;");
        ClassElementGeneratorUtil.addMethodWithBestPosition(topLevelClass, setLimit);

        Method setLimit2 = ClassElementGeneratorUtil.generateMethod(
            "limit",
            JavaVisibility.PUBLIC,
            topLevelClass.getType(),
            new Parameter(integerWrapper, "offset"),
            new Parameter(integerWrapper, "rows")
        );
        setLimit2 = ClassElementGeneratorUtil.generateMethodBody(
            setLimit2,
            "this.offset = offset;",
            "this.rows = rows;",
            "return this;"
        );
        ClassElementGeneratorUtil.addMethodWithBestPosition(topLevelClass, setLimit2);

        // clear 方法增加 offset 和 rows的清理
        List<Method> methodList = topLevelClass.getMethods();
        for (Method method : methodList) {
            if ("clear".equals(method.getName())) {
                method.addBodyLine("rows = null;");
                method.addBodyLine("offset = null;");
            }
        }

        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        this.generateLimitElement(element);
        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        this.generateLimitElement(element);
        return true;
    }

    /**
     * 生成limit节点
     *
     * @param element
     */
    private void generateLimitElement(XmlElement element) {
        XmlElement ifLimitNotNullElement = new XmlElement("if");
        ifLimitNotNullElement.addAttribute(new Attribute("test", "rows != null"));

        XmlElement ifOffsetNotNullElement = new XmlElement("if");
        ifOffsetNotNullElement.addAttribute(new Attribute("test", "offset != null"));
        ifOffsetNotNullElement.addElement(new TextElement("limit ${offset}, ${rows}"));
        ifLimitNotNullElement.addElement(ifOffsetNotNullElement);

        XmlElement ifOffsetNullElement = new XmlElement("if");
        ifOffsetNullElement.addAttribute(new Attribute("test", "offset == null"));
        ifOffsetNullElement.addElement(new TextElement("limit ${rows}"));
        ifLimitNotNullElement.addElement(ifOffsetNullElement);

        element.addElement(ifLimitNotNullElement);
    }
}
