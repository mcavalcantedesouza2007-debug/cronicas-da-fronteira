#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p tests/out
javac -encoding UTF-8 -d tests/out src/main/java/br/com/cronicasdafronteira/AdventureRules.java src/main/java/br/com/cronicasdafronteira/DialogueData.java tests/TestAdventureRules.java tests/TestDialogueData.java tests/ParseJavaSources.java
java -cp tests/out TestAdventureRules
java -cp tests/out TestDialogueData
java -cp tests/out ParseJavaSources
