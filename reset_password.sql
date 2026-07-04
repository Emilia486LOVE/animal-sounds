UPDATE mysql.user SET authentication_string = NULL WHERE User = 'root' AND Host = 'localhost';
FLUSH PRIVILEGES;