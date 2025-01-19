#!/bin/bash
service ssh start

JAR_FILE=$(ls /app/*.jar 2>/dev/null | head -n 1)

echo "Initialisiere Spring Boot Anwendung..."
if [ -n "$JAR_FILE" ]; then
    echo "Starte $JAR_FILE..."
    java -jar app/*.jar
else 
    echo "Keine JAR-Datei gefunden im Verzeichnis /app"

    tail -f /dev/null
fi