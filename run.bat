@echo off
setlocal enabledelayedexpansion

:: ========================================================
:: LeetCode-Style Java DSA Practice Runner (Windows)
:: ========================================================

cd /d "%~dp0"

:: Check Java and javac
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if defined JAVA_HOME (
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    ) else (
        echo [Error] Java is not found. Please install JDK and set JAVA_HOME.
        exit /b 1
    )
)

where javac >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [Error] javac is not found. Ensure a full JDK is installed and in PATH.
    exit /b 1
)

:: Validate files
if not exist "Solution.java" (
    echo [Error] Solution.java not found.
    exit /b 1
)

if not exist "testcases.json" (
    echo [Error] testcases.json not found.
    exit /b 1
)

if not exist ".build" mkdir ".build"

:: Remove old Solution classes
del /q ".build\Solution*.class" 2>nul

:: Build classpath
set "CP=.build;lib\*"

:: Compile
javac -encoding UTF-8 -cp "%CP%" -d ".build" runner\*.java Solution.java
if %ERRORLEVEL% neq 0 (
    echo [Compilation Failed] Please fix compilation errors in Solution.java.
    exit /b 1
)

:: Run
java -cp "%CP%" runner.Main %*
exit /b %ERRORLEVEL%
