#!/bin/bash
echo "START...";

cd ../
cd app

echo "JAVA_HOME ist: $JAVA_HOME"
echo "PATH ist: $PATH"
java -version || { echo "Java nicht gefunden"; exit 1; }

bash /app/run_project.sh


