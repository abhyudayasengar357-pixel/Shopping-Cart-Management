#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export JAVA_HOME="$SCRIPT_DIR/.tools/jdk-17.0.21+9"
export PATH="$JAVA_HOME/bin:$SCRIPT_DIR/.tools/apache-maven-3.9.6/bin:$PATH"
exec "$SCRIPT_DIR/.tools/apache-maven-3.9.6/bin/mvn" -Dmaven.repo.local="$SCRIPT_DIR/.m2/repository" "$@"
