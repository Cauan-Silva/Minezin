@echo off
setlocal
cd /d "%~dp0"
java -jar gradle\wrapper\gradle-wrapper.jar %*
