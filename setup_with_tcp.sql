SELECT User, Host, plugin, authentication_string FROM mysql.user;

ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
FLUSH PRIVILEGES;

SELECT User, Host, plugin FROM mysql.user WHERE User = 'root';