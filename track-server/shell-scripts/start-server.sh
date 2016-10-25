#!/bin/sh

JVM_PARAMS="-Xms512m -Xmx2048m -XX:+TieredCompilation -XX:+UseCompressedOops -XX:+DisableExplicitGC -XX:+UseNUMA -server"
JAR_NAME="tracking-server-1.0.jar"

/usr/bin/java -jar $JVM_PARAMS $JAR_NAME 127.0.0.1 8081 >> /dev/null 2>&1 &

