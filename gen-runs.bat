@echo off
setlocal
cd /d "%~dp0"
set "GRADLE_USER_HOME=%~dp0.gradle-codex"
call gradlew.bat genNeoForgeRuns %*
exit /b %ERRORLEVEL%
