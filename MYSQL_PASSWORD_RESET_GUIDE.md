# MySQL密码重置指南

## 问题描述

当前MySQL服务器运行正常，但root用户无法使用密码登录：
```
ERROR 1045 (28000): Access denied for user 'root'@'localhost' (using password: YES)
```

## 解决方案

### 方法一：使用MySQL Workbench（推荐）

1. 打开MySQL Workbench
2. 在"Database Connections"中找到本地连接（通常命名为"Local instance MySQL80"）
3. 双击连接，可能会直接进入（因为当前root用户没有密码或使用Socket认证）
4. 进入后执行以下SQL：
```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
FLUSH PRIVILEGES;
```
5. 关闭连接，重新测试：
```cmd
mysql.exe -u root -ppassword -e "SELECT VERSION();"
```

### 方法二：使用管理员命令提示符

1. 按下 `Win + X`，选择"Windows PowerShell (管理员)"或"命令提示符 (管理员)"
2. 执行以下命令：
```cmd
cd "D:\Program Files\MySQL\MySQL Server 8.0\bin"
mysql.exe -u root
```
3. 如果成功进入MySQL命令行，执行：
```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
FLUSH PRIVILEGES;
EXIT;
```
4. 测试连接：
```cmd
mysql.exe -u root -ppassword -e "SELECT VERSION();"
```

### 方法三：完全重置MySQL（最后手段）

1. **备份数据**：
```cmd
cd "D:\Program Files\MySQL\MySQL Server 8.0\bin"
mysqldump.exe -u root --all-databases > backup.sql
```

2. **停止MySQL服务**：
```cmd
sc stop MySQL80
```

3. **删除数据目录**（注意：这会删除所有数据库）：
```cmd
rmdir /s /q "D:\ProgramData\MySQL\MySQL Server 8.0\Data"
```

4. **重新初始化MySQL**：
```cmd
mysqld.exe --initialize --console
```
记下输出中的临时密码

5. **启动MySQL服务**：
```cmd
sc start MySQL80
```

6. **设置新密码**：
```cmd
mysql.exe -u root -p<临时密码>
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
FLUSH PRIVILEGES;
EXIT;
```

7. **恢复数据**：
```cmd
mysql.exe -u root -ppassword < backup.sql
```

## 验证步骤

设置密码后，请按以下顺序验证：

### 步骤1：测试命令行连接
```cmd
cd "D:\Program Files\MySQL\MySQL Server 8.0\bin"
mysql.exe -u root -ppassword -e "SELECT VERSION();"
```

### 步骤2：测试数据库访问
```cmd
mysql.exe -u root -ppassword -e "CREATE DATABASE IF NOT EXISTS animal_voiceprint;"
mysql.exe -u root -ppassword -e "USE animal_voiceprint; SHOW TABLES;"
```

### 步骤3：测试应用连接
```cmd
cd e:\animal sounds\backend
mvn spring-boot:run
```

## 后端配置文件

配置文件位置：`backend/src/main/resources/application.yml`

当前配置：
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/animal_voiceprint?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&socketTimeout=30000&connectTimeout=10000
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
```

## 常见问题

### Q1: 修改密码后仍然无法登录
A: 确保使用的是 `mysql_native_password` 认证插件，可以执行以下命令检查：
```sql
SELECT User, Host, plugin FROM mysql.user;
```

### Q2: 忘记临时密码
A: 在MySQL初始化日志中查找，通常在 `D:\ProgramData\MySQL\MySQL Server 8.0\Data` 目录下的 `.err` 文件中

### Q3: 数据库被删除后如何恢复
A: 使用之前备份的 `backup.sql` 文件恢复：
```cmd
mysql.exe -u root -ppassword < backup.sql
```

## 联系支持

如果以上方法都无法解决问题，请提供以下信息：
1. MySQL安装目录：`D:\Program Files\MySQL\MySQL Server 8.0`
2. MySQL数据目录：`D:\ProgramData\MySQL\MySQL Server 8.0\Data`
3. MySQL配置文件：`D:\ProgramData\MySQL\MySQL Server 8.0\my.ini`
4. 执行以下命令的输出：
```cmd
cd "D:\Program Files\MySQL\MySQL Server 8.0\bin"
mysql.exe -u root -e "SELECT User, Host, plugin FROM mysql.user;"
```