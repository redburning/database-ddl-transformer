# Database DDL transformer

![](./img/ddl-transformer-logo.gif)



## 功能特性

- 目前支持 `MySQL`、`Oracle`、`PostgreSQL` 和 `达梦` 数据库之间的转换，当然同种类型数据库的转换也支持;
- 支持 95% 以上的数据类型映射（余下的 5% 是各数据库的特有类型，例如 MySQL 的 point/polygon/geo，在目标端找不到支持的类型）;
- 支持 `column comment`, `default value`，`default function` 等 DDL 特性；
- 支持整库同步;
- 提供了一个灵活的架构，使得未来添加对新数据库的支持变得轻而易举；

## 转换效果

MySQL 到 Oracle 的转换效果：

![](./img/MySQL2Oracle.svg)

MySQL 到 PostgreSQL 的转换效果：

![](./img/MySQL2PostgreSQL.svg)

PostgreSQL 到 MySQL 的转换效果：

![](./img/PostgreSQL2MySQL.svg)

## 使用方式

MySQL 到 Oracle 的转换例子：

```java
DataBaseConnection mysqlConn = DataBaseConnection.builder()
        .jdbcUrl("jdbc:mysql://127.0.0.1:3306")
        .user("username")
        .password("password")
        .build();

DataBaseConnection oracleConn = DataBaseConnection.builder()
        .jdbcUrl("jdbc:oracle:thin:@127.0.0.1:1521/orcl")
        .user("username")
        .password("password")
        .build();

String createTableDDL = new Transformer(mysqlConn, oracleConn)
        .transformCreateTableDDL("cdc_test_source", ".*")    // .* 表示整库同步, 或配置具体表名
        .getTransformedCreateTableDDL();
System.out.println(createTableDDL);
```

MySQL 到 达梦 的转换例子：

```java
DataBaseConnection mysqlConn = DataBaseConnection.builder()
        .jdbcUrl("jdbc:mysql://127.0.0.1:3306")
        .user("username")
        .password("password")
        .build();

DataBaseConnection damengConn = DataBaseConnection.builder()
        .jdbcUrl("jdbc:dm://127.0.0.1:5236")
        .user("username")
        .password("password")
        .build();

boolean result = new Transformer(mysqlConn, postgresqlConn)
        .transformCreateTableDDL("cdc_test_source", "test_table_1")
        .executeCreateTable();  // execute ddl
```

## 扩展方式

1. 先查阅数据库支持哪些数据类型，重点关注数据类型的精度、存储限制，以免在数据同步过程中出现精度损失。准备好数据类型的映射关系，文件命名：xxx2zzz.json，例如 `MySQL2Hive.json`；

2. 在 `DataBaseType` 类中新增支持的数据库类型

   ```java
   public enum DataBaseType {
       MYSQL("MySQL"), ORACLE("Oracle"), POSTGRESQL("PostgreSQL"), DAMENG("DM");
   }
   ```

3. 在 `DataBaseConnection` 类中新增对 jdbcUrl 的识别分类：

   ```java
   /**
    * 从jdbcUrl中解析数据库类型
    * 
    * @param jdbcUrl
    * @return
    */
   private DataBaseType parseDataBaseTypeFromJdbcUrl(String jdbcUrl) {
       if (jdbcUrl.startsWith("jdbc:mysql")) {
           return DataBaseType.MYSQL;
       } else if (jdbcUrl.startsWith("jdbc:oracle")) {
           return DataBaseType.ORACLE;
       } else if (jdbcUrl.startsWith("jdbc:dm")) {
           return DataBaseType.DAMENG;
       } else if (jdbcUrl.startsWith("jdbc:postgresql")) {
           return DataBaseType.POSTGRESQL;
       } else {
           throw new IllegalArgumentException("Unsupported jdbc type for " + jdbcUrl);
       }
   }
   ```

4. 新增 `BaseMetaDataFetcher`  的扩展类 `XxxMetaDataFetcher` 

5. 新增 `BaseDdlGenerator` 的扩展类 `XxxDdlGenerator`
