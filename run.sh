#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export JAVA_HOME="$SCRIPT_DIR/.tools/jdk-17.0.21+9"
export PATH="$JAVA_HOME/bin:$SCRIPT_DIR/.tools/apache-maven-3.9.6/bin:$PATH"

echo "=========================================================="
echo " Starting Online Shopping Cart & Order Management System  "
echo "=========================================================="
echo "Java version:"
java -version
echo "----------------------------------------------------------"

if [ -f "$SCRIPT_DIR/target/online-cart-management-1.0.0.jar" ]; then
    echo "Running packaged JAR on port 8080..."
    java -jar "$SCRIPT_DIR/target/online-cart-management-1.0.0.jar" "$@"
else
    echo "JAR not found. Building and running with Maven wrapper..."
    "$SCRIPT_DIR/mvnw" spring-boot:run "$@"
fi
