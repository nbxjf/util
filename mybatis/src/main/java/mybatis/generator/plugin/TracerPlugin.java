package mybatis.generator.plugin;

import java.util.List;

import mybatis.generator.plugin.tools.ClassElementGeneratorUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.JavaBeansUtil;

/**
 * @author guankaiqiang-pro
 * @date 2021/1/14
 */
public class TracerPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateDiffClass(topLevelClass, introspectedTable.getBaseColumns(), introspectedTable);
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateDiffClass(topLevelClass, introspectedTable.getNonPrimaryKeyColumns(), introspectedTable);
        return super.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable);
    }

    private void generateDiffClass(TopLevelClass topLevelClass, List<IntrospectedColumn> introspectedColumns, IntrospectedTable introspectedTable) {
        topLevelClass.addImportedType("java.util.Objects");
        topLevelClass.addImportedType("com.yit.common.ddd.seedwork.ISnapshotTracing");

        // getChangeInfo
        Method compareMethod = new Method("getChangeInfo");
        compareMethod.setVisibility(JavaVisibility.PUBLIC);
        compareMethod.setReturnType(topLevelClass.getType());
        compareMethod.addParameter(new Parameter(topLevelClass.getType(), "source"));
        compareMethod.addBodyLine("boolean changed = false;");
        compareMethod.addBodyLine(
            introspectedTable.getRules().calculateAllFieldsClass().getShortName() + " updateDTO = new " + introspectedTable.getRules().calculateAllFieldsClass().getShortName()
                + "();");
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
            String getterMethod = JavaBeansUtil.getGetterMethodName(introspectedColumn.getJavaProperty(), fqjt);
            String setterMethod = JavaBeansUtil.getSetterMethodName(introspectedColumn.getJavaProperty());
            compareMethod.addBodyLine("updateDTO." + setterMethod + "(this." + getterMethod + "());");
        }
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
            String getterMethod = JavaBeansUtil.getGetterMethodName(introspectedColumn.getJavaProperty(), fqjt);
            String setterMethod = JavaBeansUtil.getSetterMethodName(introspectedColumn.getJavaProperty());
            compareMethod.addBodyLine("if (!Objects.equals(this." + getterMethod + "(), source." + getterMethod + "())) {");
            compareMethod.addBodyLine("updateDTO." + setterMethod + "(this." + getterMethod + "());");
            compareMethod.addBodyLine("changed = true;");
            compareMethod.addBodyLine("}");
        }
        compareMethod.addBodyLine("return changed ? updateDTO : null;");
        ClassElementGeneratorUtil.addMethodWithBestPosition(topLevelClass, compareMethod);

        InnerClass diffClass = new InnerClass("Tracer");
        diffClass.setVisibility(JavaVisibility.PUBLIC);
        diffClass.setStatic(false);

        FullyQualifiedJavaType diffInterface = new FullyQualifiedJavaType("com.yit.common.ddd.seedwork.ISnapshotTracing");
        diffInterface.addTypeArgument(topLevelClass.getType());
        diffInterface.addTypeArgument(topLevelClass.getType());
        diffClass.addSuperInterface(diffInterface);

        //_snapshot
        Field field = new Field("_snapshot", topLevelClass.getType());
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setTransient(true);
        diffClass.addField(field);

        // getCurrentData
        Method getCurrentDataMethod = new Method("getCurrentData");
        getCurrentDataMethod.addAnnotation("@Override");
        getCurrentDataMethod.setVisibility(JavaVisibility.PUBLIC);
        getCurrentDataMethod.setReturnType(topLevelClass.getType());
        getCurrentDataMethod.addBodyLine("return " + topLevelClass.getType().getShortName() + ".this;");
        ClassElementGeneratorUtil.addMethodWithBestPosition(diffClass, getCurrentDataMethod);

        // setSnapshot
        Method setSnapshotMethod = new Method("setSnapshot");
        setSnapshotMethod.addAnnotation("@Override");
        setSnapshotMethod.setVisibility(JavaVisibility.PUBLIC);
        setSnapshotMethod.addParameter(new Parameter(topLevelClass.getType(), "snapshot"));
        setSnapshotMethod.addBodyLine("this._snapshot = snapshot;");
        ClassElementGeneratorUtil.addMethodWithBestPosition(diffClass, setSnapshotMethod);

        // getLatestSnapshot
        Method getLatestSnapshotMethod = new Method("getLatestSnapshot");
        getLatestSnapshotMethod.addAnnotation("@Override");
        getLatestSnapshotMethod.setVisibility(JavaVisibility.PUBLIC);
        getLatestSnapshotMethod.setReturnType(topLevelClass.getType());
        getLatestSnapshotMethod.addBodyLine("return this._snapshot;");
        ClassElementGeneratorUtil.addMethodWithBestPosition(diffClass, getLatestSnapshotMethod);

        // innerClass#getChangeInfo
        Method getChangeInfoMethod = new Method("getChangeInfo");
        getChangeInfoMethod.addAnnotation("@Override");
        getChangeInfoMethod.setVisibility(JavaVisibility.PUBLIC);
        getChangeInfoMethod.setReturnType(topLevelClass.getType());
        getChangeInfoMethod.addBodyLine("return " + topLevelClass.getType().getShortName() + ".this.getChangeInfo(this._snapshot);");
        ClassElementGeneratorUtil.addMethodWithBestPosition(diffClass, getChangeInfoMethod);
        topLevelClass.addInnerClass(diffClass);
    }
}
