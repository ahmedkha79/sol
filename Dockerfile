# JDK was verwendet wird

FROM openjdk:21-jdk-slim

# Setze das Arbeitsverzeichnis

WORKDIR /app

# Kopiere die Anwendung ins Image

COPY build/libs/*.jar app.jar


# Default Port
EXPOSE 8006


# Definiere den Startbefehl für die Anwendung
CMD ["java", "-jar", "app.jar"]