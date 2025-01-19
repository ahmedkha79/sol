#!/bin/bash

# Schritt 1: Prüfen, ob die .tar-Datei existiert
IMAGE_TAR_PATH="solapplication.tar"

if [ ! -f "$IMAGE_TAR_PATH" ]; then
  echo "Fehler: Die Datei $IMAGE_TAR_PATH wurde nicht gefunden!"
  exit 1
fi

echo "Lade Docker-Image aus $IMAGE_TAR_PATH..."
docker load < "$IMAGE_TAR_PATH"

# Schritt 2: Image-Name aus Docker-Images abrufen
IMAGE_NAME=$(docker images --format "{{.Repository}}:{{.Tag}}" | head -n 1)

if [ -z "$IMAGE_NAME" ]; then
  echo "Fehler: Das Docker-Image konnte nicht geladen werden!"
  exit 1
fi

echo "Docker-Image erfolgreich geladen: $IMAGE_NAME"

# Schritt 3: Vorherigen Container stoppen und entfernen (falls vorhanden)
CONTAINER_NAME="solProject"
docker stop "$CONTAINER_NAME" 2>/dev/null || true
docker rm "$CONTAINER_NAME" 2>/dev/null || true

# Schritt 4: Container starten
echo "Starte Docker-Container $CONTAINER_NAME aus dem Image $IMAGE_NAME..."
docker run -d -p 8000:8000 --name "$CONTAINER_NAME" "$IMAGE_NAME"

# Status prüfen
if [ $? -eq 0 ]; then
  echo "Container $CONTAINER_NAME läuft jetzt auf Port 8000."
else
  echo "Fehler: Container konnte nicht gestartet werden!"
  exit 1
fi
