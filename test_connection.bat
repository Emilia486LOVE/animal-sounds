@echo off
cd /d "D:\Program Files\MySQL\MySQL Server 8.0\bin"
echo === Test 1: Root without password ===
mysql.exe -u root -e "SELECT VERSION();" > "e:\animal sounds\test_result.txt" 2>&1
echo === Test 2: Root with password ===
mysql.exe -u root -ppassword -e "SELECT VERSION();" >> "e:\animal sounds\test_result.txt" 2>&1
echo === Test 3: Check users ===
mysql.exe -u root -e "SELECT User, Host, plugin FROM mysql.user;" >> "e:\animal sounds\test_result.txt" 2>&1
echo === Done ===