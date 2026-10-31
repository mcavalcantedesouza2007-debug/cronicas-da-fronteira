#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
echo 'Requer JDK 25 e Gradle 9.7.1+' 
gradle clean build
