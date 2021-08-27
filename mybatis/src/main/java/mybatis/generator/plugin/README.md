# 基于 mybatis-generator 的插件
## 使用方法
1. 在项目中dao-module添加 依赖
    ```java
        <dependency>
            <groupId>com.yit</groupId>
            <artifactId>mybatis-generator-plugin</artifactId>
            <version>1.0.1-SNAPSHOT</version>
        </dependency>
    ```
2. 在 generator.xml 中添加需要使用的插件
    ```xml
               <!-- 增强 EqualsAndHashCode 插件 -->
               <plugin type="com.yit.mybatisplugin.EqualsHashCodePluginEnhancer"/>
               <!-- 给 DTO各列添加注释 -->
               <plugin type="com.yit.mybatisplugin.FieldCommentPlugin"/>
               <!-- DTO 中属性映射成枚举插件 -->
               <plugin type="com.yit.mybatisplugin.ColumnModelPlugin"/>
               <!-- 生成批量插入方法插件 -->
               <plugin type="com.yit.mybatisplugin.BatchInsertPlugin"/>
               <!-- 生成批量更新方法插件 -->
               <plugin type="com.yit.mybatisplugin.BatchUpdatePlugin"/>
               <!-- 分页插件 -->
               <plugin type="com.yit.mybatisplugin.LimitGeneratePlugin"/>
               <!-- 根据 example 查询单条结果插件 -->
               <plugin type="com.yit.mybatisplugin.SelectOneByExamplePlugin"/>
               <!-- 数据变更 tracer 插件 -->
               <plugin type="com.yit.mybatisplugin.TracerPlugin"/>
    ```
## 插件详情介绍
### 增强 EqualsAndHashCode 插件 `EqualsHashCodePluginEnhancer`
* Java 默认生成的 DTO 的`equals()` 和`hashCode()`方法包含全部参数的，有的时候我们需要重写`equals()` 和`hashCode()`方法并指定其中的部分列参与计算，此时可以通过该插件指定需要参与计算的列。
* 使用方法：在配置生成的表时，添加 `specifiedColumnsForEqualsHashCode` 属性，如：
    ```xml
        <table tableName="yitiao_am_submit_spu_activity" domainObjectName="AmSubmitSpuActivityDTO"
               enableDeleteByPrimaryKey="true"
               enableDeleteByExample="true">
            <generatedKey column="id" sqlStatement="SELECT LAST_INSERT_ID()" identity="true"/>
            <property name="specifiedColumnsForEqualsHashCode" value="id, spu_id"/>
        </table>
    ```
  
### 生成列注释插件 `FieldCommentPlugin`
* 将 sql 中建表语句中的注释信息，添加到 DTO 中各属性的注释中，对于规范建表、以及对 DTO 的快捷应用具有一定的帮助意义
* 生成的 DTO 效果实例如下：
    ```java
      public class ExchangeActivityDTO implements Serializable {
          /** 
           * 主键
           */
          private Integer id;
      
          /** 
           * 创建者id
           */
          private Integer creatorId;
      
          /** 
           * 礼品兑换活动名称/礼品名称
           */
          private String activityName;
      
          /** 
           * 兑换活动申请描述
           */
          private String activityDesc;
      
          /** 
           * 兑换类型(体验vip，付费vip)
           */
          private String resultType;
      
          /** 
           * 礼品唯一标示:优惠券类型为 code、兑换码类型为 兑换码ID
           */
          private String prizeIdentity;
      
          /** 
           * 兑换方式:CODE（兑换码） POINT（积分）
           */
          private String exchangeWay;
      
          /** 
           * code(兑换码):一个兑换码兑换一份礼品   point(积分):多少积分兑换一份礼品
           */
          private Integer prizePrice;
      }
    ```

### DTO 中属性映射成枚举插件 `ColumnModelPlugin`
* 目的：将 DTO 中的各字段生成一个内部枚举类，并标注各属性的信息
* 目前主要用于与 `BatchUpdatePlugin` 配合使用
* 效果如下所示：
    ```java
         public enum Column {
             id("id", "id", "INTEGER", false),
             userId("user_id", "userId", "INTEGER", false),
             inviterUserId("inviter_user_id", "inviterUserId", "INTEGER", false),
             inviteSignInTime("invite_sign_in_time", "inviteSignInTime", "TIMESTAMP", false),
             isDeleted("is_deleted", "isDeleted", "SMALLINT", false),
             createTime("create_time", "createTime", "TIMESTAMP", false),
             updateTime("update_time", "updateTime", "TIMESTAMP", false);
     
             private static final String BEGINNING_DELIMITER = "\"";
     
             private static final String ENDING_DELIMITER = "\"";
     
             private final String column;
     
             private final boolean isColumnNameDelimited;
     
             private final String javaProperty;
     
             private final String jdbcType;
     
             public String value() {
                 return this.column;
             }
     
             public String getValue() {
                 return this.column;
             }
     
             public String getJavaProperty() {
                 return this.javaProperty;
             }
     
             public String getJdbcType() {
                 return this.jdbcType;
             }
     
             Column(String column, String javaProperty, String jdbcType, boolean isColumnNameDelimited) {
                 this.column = column;
                 this.javaProperty = javaProperty;
                 this.jdbcType = jdbcType;
                 this.isColumnNameDelimited = isColumnNameDelimited;
             }
     
             public String desc() {
                 return this.getEscapedColumnName() + " DESC";
             }
     
             public String asc() {
                 return this.getEscapedColumnName() + " ASC";
             }
     
             public static Column[] excludes(Column ... excludes) {
                 ArrayList<Column> columns = new ArrayList<>(Arrays.asList(Column.values()));
                 if (excludes != null && excludes.length > 0) {
                     columns.removeAll(new ArrayList<>(Arrays.asList(excludes)));
                 }
                 return columns.toArray(new Column[]{});
             }
     
             public static Column[] all() {
                 return Column.values();
             }
     
             public String getEscapedColumnName() {
                 if (this.isColumnNameDelimited) {
                     return new StringBuilder().append(BEGINNING_DELIMITER).append(this.column).append(ENDING_DELIMITER).toString();
                 } else {
                     return this.column;
                 }
             }
     
             public String getAliasedEscapedColumnName() {
                 return this.getEscapedColumnName();
             }
         }
    ```

### 批量插入  `BatchInsertPlugin`
* 该插件会在 generatedMapper 中生成一个 `batchInsert()` 方法
    ```java
      long batchInsert(List<DiscoveryPacketDTO> list);
  
      long batchInsertSelective(List<DiscoveryPacketDTO> list);
    ```
* 该方法可以批量返回 DTO 的主键 id

### 批量更新插件
* 需要依赖 `FieldCommentPlugin` 插件
* 使用该方法，可以根据实际情况生成 `batchUpdateByPrimaryKeySelective()` 、`batchUpdateByAssignedColumnSelective()`两个方法，若 DTO 还存在Blob 字段，同时也会生成 `batchUpdateByPrimaryKeySelectiveWithBlobs()` 和 `batchUpdateByAssignedColumnSelectiveWithBlobs`两个函数
* 其中，`batchUpdateByPrimaryKey()` 是根据主键批量更新的，类似于 `update xxx set ... where id = 1` 的条件
* 而 `batchUpdateByAssignedColumn()` 是根据自定义更新字段来进行更新的，类似于 `update xxx set ... where a = 1 and b = 2` 的条件
* 方法示例如下：
    ```java
          long batchUpdateByPrimaryKeySelective(@Param("list") List<SalesFlatOrderDTOWithBLOBs> list);
      
          long batchUpdateByPrimaryKeySelectiveWithBlobs(@Param("list") List<SalesFlatOrderDTOWithBLOBs> list);
      
          long batchUpdateByAssignedColumnSelective(@Param("list") List<SalesFlatOrderDTOWithBLOBs> list, @Param("dependAssignedColumns") SalesFlatOrderDTOWithBLOBs.Column ... dependAssignedColumns);
      
          long batchUpdateByAssignedColumnSelectiveWithBlobs(@Param("list") List<SalesFlatOrderDTOWithBLOBs> list, @Param("dependAssignedColumns") SalesFlatOrderDTOWithBLOBs.Column ... dependAssignedColumns);
    ```

### 物理分页 `LimitGeneratePlugin`
* 由于 RowBounds 是基于全量结果查询返回后的内存分页，不建议采用。
* 通过该插件会在生成的 `example` 类中添加2个属性 `offset`、`rows`，和对应的 set、get 方法，并修改 xml 生成对应的 limit 查询 sql语句，用于达到 mysql 原生 sql 查询语句如 `limit 0,10` 物理分页查询的目的。
* 使用方法：
   ```java
      Example example = new Example();
      example.setOrderByClause("id DESC, create_time ASC");
      // 分别设置 offset 、 rows
      example.setOffset(0);
      example.setRows(100);
      // 或者调用 limit(Integer rows)方法指定查询结果大小
      example.limit(10);
      // 或者调用limit(Integer offset, Integer rows)
      example.limit(0,100);
    ```

### 单结果查询
* 很多时候我们会遇到通过 example 查询单调结果的场景，但是 mybatis 默认`selectByExample()`方法返回的结果集是 list。
* 通过该插件，可以生成`selectOneByExample()`方法，返回单条数据结果集。
* 生成的函数示例如下：
    ```java
        ExchangeCodeDTO selectOneByExample(ExchangeCodeDTOExample example);
    ```

### 变更追踪  `TracerPlugin`
* 该插件会在 generatedDTO 中生成一个 `getChangeInfo` 方法以及一个内部类 `Tracer`， 需要将common升级至3.2.3及以上版本
### DTO差量更新
  ```java
        ExampleDTO exampleDTO = exampleDTOMapper.selectByPrimaryKey(id);
        // 建立数据副本（com.yit.redis.common.ddd.utils.DeepCloneUtil）
        ExampleDTO source = DeepCloneUtil.clone(exampleDTO);
        // 修改exampleDTO
        exampleDTO.setColumn("change something");
        // 返回变更字段
        ExampleDTO updateDTO = exampleDTO.getChangeInfo(source);
        // 根据ID保存修改的数据列
        exampleDTOMapper.updateByPrimaryKeySelective(updateDTO);
  ```
### DTO变更追踪
  ```java
        ExampleDTO exampleDTO = exampleDTOMapper.selectByPrimaryKey(id);
        // 创建变更追踪器
        ExampleDTO.Tracer tracer = exampleDTO.new Tracer();
        // 开始追踪（内部实现为创建exampleDTO数据快照）
        tracer.attach();
        // 修改exampleDTO
        exampleDTO.setColumn("change something");
        // 返回变更字段
        ExampleDTO updateDTO = tracer.getChangeInfo();
        // 根据ID保存修改的数据列
        exampleDTOMapper.updateByPrimaryKeySelective(updateDTO);
        // 结束追踪
        tracer.detach();
        // 若仍需使用exampleDTO，可重新开启追踪attach()
  ```

## 参考文档
1. ![MyBatis Generator官方文档](http://mybatis.org/generator/reference/pluggingIn.html)