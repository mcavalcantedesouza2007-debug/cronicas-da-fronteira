@echo off
cd /d "%~dp0"
echo Requer JDK 25 e Gradle 9.7.1 ou superior.
where gradle >nul 2>&1
if errorlevel 1 (
  echo Gradle nao localizado. Instale Gradle 9.7.1 ou use o IntelliJ para abrir o build.gradle.
  pause
  exit /b 1
)
gradle clean build --stacktrace
if errorlevel 1 (echo Falha de compilacao. & pause & exit /b 1)
echo Procure o JAR em build\libs
pause
