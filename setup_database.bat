@echo off
cd /d "D:\Program Files\MySQL\MySQL Server 8.0\bin"

echo === Step 1: Create database ===
mysql.exe -u root -e "CREATE DATABASE IF NOT EXISTS animal_voiceprint;"

echo === Step 2: Create user with all hosts ===
mysql.exe -u root -e "DROP USER IF EXISTS 'animal_app'@'%';"
mysql.exe -u root -e "CREATE USER 'animal_app'@'%' IDENTIFIED WITH mysql_native_password BY 'password';"

echo === Step 3: Grant privileges ===
mysql.exe -u root -e "GRANT ALL PRIVILEGES ON animal_voiceprint.* TO 'animal_app'@'%';"
mysql.exe -u root -e "FLUSH PRIVILEGES;"

echo === Step 4: Test connection ===
mysql.exe -u animal_app -h 127.0.0.1 -ppassword -e "SELECT VERSION();"

echo === Step 5: Check database ===
mysql.exe -u animal_app -h 127.0.0.1 -ppassword -e "USE animal_voiceprint; SHOW TABLES;"

echo === Done ===
pause