# JDK was verwendet wird

FROM openjdk:21-jdk-slim

# Setze das Arbeitsverzeichnis

# Install curl
RUN apt-get update && apt-get install -y curl && apt-get clean


RUN apt-get update && apt-get install -y net-tools



WORKDIR /app

# Kopiere die Anwendung ins Image

COPY build/libs/*.jar app.jar


# Default Port
EXPOSE 8006


# Definiere den Startbefehl für die Anwendung
CMD ["java", "-jar", "app.jar"]