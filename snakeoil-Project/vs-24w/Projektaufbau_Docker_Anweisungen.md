
# Projektaufbau und Docker-Anweisungen

## Projektaufbau

1. **Build-Prozess ausführen**
   ```bash
   gradle clean build
   ```
2. **.jar Datei kopieren**
   - Die erzeugte `.jar`-Datei aus dem Ordner `build/libs` kopieren (nicht die Plain-Version).
3. **In den Zielordner kopieren**
   - Die `.jar`-Datei in den `app`-Ordner des `snakeoil`-Projekts kopieren.

## Ports vordefinieren
- Mit der `.env`-Datei können die Ports vordefiniert werden.

---

## Docker-Anweisungen

### Docker Image bauen
```bash
docker build -t snakeoil-${USER}:latest .
```

### Docker Image löschen
```bash
docker rmi $(docker images -q)
```

### Docker Container löschen
```bash
docker rm $(docker ps -aq)
```

### Docker Container starten
```bash
docker run --network host --env-file app/.env --name snakeoil-${USER} snakeoil-${USER}:latest
```

### Docker Container stoppen
```bash
docker stop $(docker ps -aq)
```

### Docker Logs anzeigen
```bash
docker logs <ID_des_Containers>
```

### Docker Container Informationen anzeigen
```bash
docker inspect <ID_des_Containers>
```
