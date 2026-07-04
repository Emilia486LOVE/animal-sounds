@echo off
cd /d "D:\Program Files\MySQL\MySQL Server 8.0\bin"
mysql.exe -u root -e "SELECT User, Host, plugin FROM mysql.user;"
pause