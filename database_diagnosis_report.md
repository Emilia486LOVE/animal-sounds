# 数据库连接诊断报告

## 一、系统状态检查

### 1.1 MySQL服务状态
- ✅ MySQL服务（MySQL80）运行正常
- ✅ 端口3306正在监听（TCP/IPv4和IPv6）
- ✅ 端口33060（MySQL X Protocol）正在监听

### 1.2 MySQL配置
- 配置文件位置：`D:\ProgramData\MySQL\MySQL Server 8.0\my.ini`
- 默认认证插件：`mysql_native_password`（已在配置中设置）
- 数据库目录：`D:\ProgramData\MySQL\MySQL Server 8.0\Data`
- 最大连接数：151

### 1.3 网络连通性
- ✅ 本地端口3306可达
- ✅ 无防火墙阻止本地连接

## 二、连接问题分析

### 2.1 问题现象
1. 使用命令行工具无法使用密码登录MySQL
2. 错误信息：`ERROR 1045 (28000): Access denied for user 'root'@'localhost' (using password: YES)`
3. 但以管理员身份通过TCP协议可以无密码登录

### 2.2 可能原因
1. **root用户认证插件问题**：MySQL 8.0默认使用`caching_sha2_password`认证插件，但客户端可能使用`mysql_native_password`
2. **localhost vs 127.0.0.1差异**：MySQL中`localhost`和`127.0.0.1`被视为不同的主机，可能有不同的用户记录
3. **密码加密方式不匹配**：密码可能使用了不同的哈希算法存储

## 三、解决方案

### 方案1：使用MySQL Workbench设置密码（推荐）
1. 打开MySQL Workbench
2. 连接到本地MySQL服务器（使用Socket连接，无需密码）
3. 执行以下SQL：
```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
FLUSH PRIVILEGES;
```

### 方案2：使用管理员权限重置密码
以管理员身份打开命令提示符，执行：
```cmd
cd "D:\Program Files\MySQL\MySQL Server 8.0\bin"
mysql.exe -u root
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
FLUSH PRIVILEGES;
```

### 方案3：创建专用应用用户
```sql
CREATE USER 'animal_app'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
CREATE DATABASE IF NOT EXISTS animal_voiceprint;
GRANT ALL PRIVILEGES ON animal_voiceprint.* TO 'animal_app'@'localhost';
FLUSH PRIVILEGES;
```

## 四、后端配置文件

当前配置文件位置：`backend/src/main/resources/application.yml`

配置内容：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/animal_voiceprint?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&socketTimeout=30000&connectTimeout=10000
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 五、验证步骤

设置密码后，执行以下命令验证连接：
```cmd
cd "D:\Program Files\MySQL\MySQL Server 8.0\bin"
mysql.exe -u root -ppassword -e "SELECT VERSION();"
mysql.exe -u root -ppassword -e "USE animal_voiceprint; SHOW TABLES;"
```

如果连接成功，再启动后端服务：
```cmd
cd backend
mvn spring-boot:run
```

## 六、连接池配置建议

当前使用Spring Boot默认的HikariCP连接池，建议配置：
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 10000
      max-lifetime: 1800000
```