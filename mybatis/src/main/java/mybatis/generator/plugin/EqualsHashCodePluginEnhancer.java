package mybatis.generator.plugin;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.plugins.EqualsHashCodePlugin;

/**
 * Created by Jeff_xu on 2020/12/6.
 *
 * @author Jeff_xu
 */
public class EqualsHashCodePluginEnhancer extends EqualsHashCodePlugin {

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns = null;
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            columns = introspectedTable.getNonBLOBColumns();
        } else {
            String specifiedColumnsForEqualsHashCode = introspectedTable.getTableConfigurationProperty(
                "specifiedColumnsForEqualsHashCode");
            if (specifiedColumnsForEqualsHashCode != null && specifiedColumnsForEqualsHashCode.length() > 0) {
                String[] columnNames = specifiedColumnsForEqualsHashCode.split(",");
                if (columnNames.length > 0) {
                    columns = new ArrayList<>();
                    for (String column : columnNames) {
                        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
                            if (column.trim().equalsIgnoreCase(introspectedColumn.getActualColumnName())) {
                                columns.add(introspectedColumn);
                            }
                        }
                    }
                }
            }
            if (columns == null || columns.size() == 0) {
                columns = introspectedTable.getAllColumns();
            }
        }

        generateEquals(topLevelClass, columns, introspectedTable);
        generateHashCode(topLevelClass, columns, introspectedTable);
        return true;
    }
}
