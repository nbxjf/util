package mybatis.generator.plugin;

import java.util.List;

import mybatis.generator.plugin.tools.ClassElementGeneratorUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * Created by Jeff_xu on 2020/12/6.
 *
 * @author Jeff_xu
 */
public class ColumnModelPlugin extends PluginAdapter {

    /**
     * 内部Enum名
     */
    public static final String ENUM_NAME = "Column";

    /**
     * 自定义方法
     */
    public static final String METHOD_EXCLUDES = "excludes";
    public static final String METHOD_ALL = "all";
    public static final String METHOD_GET_ESCAPED_COLUMN_NAME = "getEscapedColumnName";
    public static final String METHOD_GET_ALIASED_ESCAPED_COLUMN_NAME = "getAliasedEscapedColumnName";

    public static final String CONST_BEGINNING_DELIMITER = "BEGINNING_DELIMITER";
    public static final String CONST_ENDING_DELIMITER = "ENDING_DELIMITER";

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addInnerEnum(this.generateColumnEnum(topLevelClass, introspectedTable));
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addInnerEnum(this.generateColumnEnum(topLevelClass, introspectedTable));
        return super.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addInnerEnum(this.generateColumnEnum(topLevelClass, introspectedTable));
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 生成Column字段枚举
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    private InnerEnum generateColumnEnum(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 生成内部枚举
        InnerEnum innerEnum = new InnerEnum(new FullyQualifiedJavaType(ENUM_NAME));
        innerEnum.setVisibility(JavaVisibility.PUBLIC);
        innerEnum.setStatic(true);

        // 生成常量
        Field beginningDelimiterField = ClassElementGeneratorUtil.generateField(
            CONST_BEGINNING_DELIMITER,
            JavaVisibility.PRIVATE,
            FullyQualifiedJavaType.getStringInstance(),
            "\"" + StringUtility.escapeStringForJava(context.getBeginningDelimiter()) + "\""
        );
        beginningDelimiterField.setStatic(true);
        beginningDelimiterField.setFinal(true);
        innerEnum.addField(beginningDelimiterField);

        Field endingDelimiterField = ClassElementGeneratorUtil.generateField(
            CONST_ENDING_DELIMITER,
            JavaVisibility.PRIVATE,
            FullyQualifiedJavaType.getStringInstance(),
            "\"" + StringUtility.escapeStringForJava(context.getEndingDelimiter()) + "\""
        );
        endingDelimiterField.setStatic(true);
        endingDelimiterField.setFinal(true);
        innerEnum.addField(endingDelimiterField);

        // 生成属性和构造函数
        Field columnField = new Field("column", FullyQualifiedJavaType.getStringInstance());
        columnField.setVisibility(JavaVisibility.PRIVATE);
        columnField.setFinal(true);
        innerEnum.addField(columnField);

        Field isColumnNameDelimitedField = new Field("isColumnNameDelimited", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        isColumnNameDelimitedField.setVisibility(JavaVisibility.PRIVATE);
        isColumnNameDelimitedField.setFinal(true);
        innerEnum.addField(isColumnNameDelimitedField);

        Field javaPropertyField = new Field("javaProperty", FullyQualifiedJavaType.getStringInstance());
        javaPropertyField.setVisibility(JavaVisibility.PRIVATE);
        javaPropertyField.setFinal(true);
        innerEnum.addField(javaPropertyField);

        Field jdbcTypeField = new Field("jdbcType", FullyQualifiedJavaType.getStringInstance());
        jdbcTypeField.setVisibility(JavaVisibility.PRIVATE);
        jdbcTypeField.setFinal(true);
        innerEnum.addField(jdbcTypeField);

        Method mValue = new Method("value");
        mValue.setVisibility(JavaVisibility.PUBLIC);
        mValue.setReturnType(FullyQualifiedJavaType.getStringInstance());
        mValue.addBodyLine("return this.column;");
        ClassElementGeneratorUtil.addMethodWithBestPosition(innerEnum, mValue);

        Method mGetValue = new Method("getValue");
        mGetValue.setVisibility(JavaVisibility.PUBLIC);
        mGetValue.setReturnType(FullyQualifiedJavaType.getStringInstance());
        mGetValue.addBodyLine("return this.column;");
        ClassElementGeneratorUtil.addMethodWithBestPosition(innerEnum, mGetValue);

        Method mGetJavaProperty = ClassElementGeneratorUtil.generateGetterMethod(javaPropertyField);
        ClassElementGeneratorUtil.addMethodWithBestPosition(innerEnum, mGetJavaProperty);

        Method mGetJdbcType = ClassElementGeneratorUtil.generateGetterMethod(jdbcTypeField);
        ClassElementGeneratorUtil.addMethodWithBestPosition(innerEnum, mGetJdbcType);

        Method constructor = new Method(ENUM_NAME);
        constructor.setConstructor(true);
        constructor.addBodyLine("this.column = column;");
        constructor.addBodyLine("this.javaProperty = javaProperty;");
        constructor.addBodyLine("this.jdbcType = jdbcType;");
        constructor.addBodyLine("this.isColumnNameDelimited = isColumnNameDelimited;");
        constructor.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "column"));
        constructor.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "javaProperty"));
        constructor.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "jdbcType"));
        constructor.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "isColumnNameDelimited"));
        ClassElementGeneratorUtil.addMethodWithBestPosition(innerEnum, constructor);

        // Enum枚举
        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            Field field = JavaBeansUtil.getJavaBeansField(introspectedColumn, context, introspectedTable);

            StringBuffer sb = new StringBuffer();
            sb.append(field.getName());
            sb.append("(\"");
            sb.append(introspectedColumn.getActualColumnName());
            sb.append("\", \"");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append("\", \"");
            sb.append(introspectedColumn.getJdbcTypeName());
            sb.append("\", ");
            sb.append(introspectedColumn.isColumnNameDelimited());
            sb.append(")");

            innerEnum.addEnumConstant(sb.toString());
        }

        // asc 和 desc 方法
        Method desc = new Method("desc");
        desc.setVisibility(JavaVisibility.PUBLIC);
        desc.setReturnType(FullyQualifiedJavaType.getStringInstance());
        desc.addBodyLine("return this." + METHOD_GET_ESCAPED_COLUMN_NAME + "() + \" DESC\";");
        ClassElementGeneratorUtil.addMethodWithBestPosition(innerEnum, desc);

        Method asc = new Method("asc");
        asc.setVisibility(JavaVisibility.PUBLIC);
        asc.setReturnType(FullyQualifiedJavaType.getStringInstance());
        asc.addBodyLine("return this." + METHOD_GET_ESCAPED_COLUMN_NAME + "() + \" ASC\";");
        ClassElementGeneratorUtil.addMethodWithBestPosition(innerEnum, asc);

        // excludes
        topLevelClass.addImportedType("java.util.Arrays");
        topLevelClass.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());
        Method mExcludes = ClassElementGeneratorUtil.generateMethod(
            METHOD_EXCLUDES,
            JavaVisibility.PUBLIC,
            new FullyQualifiedJavaType(ENUM_NAME + "[]"),
            new Parameter(innerEnum.getType(), "excludes", true)
        );
        mExcludes.setStatic(true);
        ClassElementGeneratorUtil.generateMethodBody(
            mExcludes,
            "ArrayList<Column> columns = new ArrayList<>(Arrays.asList(Column.values()));",
            "if (excludes != null && excludes.length > 0) {",
            "columns.removeAll(new ArrayList<>(Arrays.asList(excludes)));",
            "}",
            "return columns.toArray(new Column[]{});"
        );
        ClassElementGeneratorUtil.addMethodWithBestPosition(innerEnum, mExcludes);

        // all
        Method mAll = ClassElementGeneratorUtil.generateMethod(
            METHOD_ALL,
            JavaVisibility.PUBLIC,
            new FullyQualifiedJavaType(ENUM_NAME + "[]")
        );
        mAll.setStatic(true);
        mAll.addBodyLine("return Column.values();");
        ClassElementGeneratorUtil.addMethodWithBestPosition(innerEnum, mAll);

        // getEscapedColumnName
        Method mGetEscapedColumnName = ClassElementGeneratorUtil.generateMethod(
            METHOD_GET_ESCAPED_COLUMN_NAME,
            JavaVisibility.PUBLIC,
            FullyQualifiedJavaType.getStringInstance()
        );
        ClassElementGeneratorUtil.generateMethodBody(
            mGetEscapedColumnName,
            "if (this.isColumnNameDelimited) {",
            "return new StringBuilder().append(" + CONST_BEGINNING_DELIMITER + ").append(this.column).append(" + CONST_ENDING_DELIMITER + ").toString();",
            "} else {",
            "return this.column;",
            "}"
        );
        ClassElementGeneratorUtil.addMethodWithBestPosition(innerEnum, mGetEscapedColumnName);

        // getAliasedEscapedColumnName
        Method mGetAliasedEscapedColumnName = ClassElementGeneratorUtil.generateMethod(
            METHOD_GET_ALIASED_ESCAPED_COLUMN_NAME,
            JavaVisibility.PUBLIC,
            FullyQualifiedJavaType.getStringInstance()
        );
        if (StringUtility.stringHasValue(introspectedTable.getTableConfiguration().getAlias())) {
            String alias = introspectedTable.getTableConfiguration().getAlias();
            mGetAliasedEscapedColumnName.addBodyLine("StringBuilder sb = new StringBuilder();");
            mGetAliasedEscapedColumnName.addBodyLine("sb.append(\"" + alias + ".\");");
            mGetAliasedEscapedColumnName.addBodyLine("sb.append(this." + METHOD_GET_ESCAPED_COLUMN_NAME + "());");
            mGetAliasedEscapedColumnName.addBodyLine("sb.append(\" as \");");
            mGetAliasedEscapedColumnName.addBodyLine("if (this.isColumnNameDelimited) {");
            mGetAliasedEscapedColumnName.addBodyLine("sb.append(" + CONST_BEGINNING_DELIMITER + ");");
            mGetAliasedEscapedColumnName.addBodyLine("}");
            mGetAliasedEscapedColumnName.addBodyLine("sb.append(\"" + alias + "_\");");
            mGetAliasedEscapedColumnName.addBodyLine("sb.append(this.column);");
            mGetAliasedEscapedColumnName.addBodyLine("if (this.isColumnNameDelimited) {");
            mGetAliasedEscapedColumnName.addBodyLine("sb.append(" + CONST_BEGINNING_DELIMITER + ");");
            mGetAliasedEscapedColumnName.addBodyLine("}");
            mGetAliasedEscapedColumnName.addBodyLine("return sb.toString();");
        } else {
            mGetAliasedEscapedColumnName.addBodyLine("return this." + METHOD_GET_ESCAPED_COLUMN_NAME + "();");
        }
        ClassElementGeneratorUtil.addMethodWithBestPosition(innerEnum, mGetAliasedEscapedColumnName);

        return innerEnum;
    }
}
