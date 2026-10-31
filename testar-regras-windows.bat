@echo off
cd /d "%~dp0"
if not exist tests\out mkdir tests\out
javac -encoding UTF-8 -d tests\out src\main\java\br\com\cronicasdafronteira\AdventureRules.java src\main\java\br\com\cronicasdafronteira\DialogueData.java tests\TestAdventureRules.java tests\TestDialogueData.java tests\ParseJavaSources.java
if errorlevel 1 (echo ERRO em javac & pause & exit /b 1)
java -cp tests\out TestAdventureRules
if errorlevel 1 (pause & exit /b 1)
java -cp tests\out TestDialogueData
if errorlevel 1 (pause & exit /b 1)
java -cp tests\out ParseJavaSources
pause
