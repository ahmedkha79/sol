# Snakeoil

## TODO
* In der [Dockerfile](Dockerfile) die Variable CI_PROJECT_NAME dem Teamnamen anpassen.
* Die Ports nach Aufgabenblatt anpassen 
* Es empfiehlt sich alle notwendigen Prozeduren in einem Script zu verfassen. Dafür ist die [autorun.sh](userdata/autorun.sh) gedacht, welche beim Imagebau ausgeführt wird.

## DON'T in der VM
* **NICHT** weitere Ports verändern oder verwenden, Sie sind nicht alleine auf der VM
* **NICHT** den SSHD weiter absichern wollen
* **NICHT** per Firewall aussperren

## Hilfestellung / CheatSheet
### Image bauen
```bash
# cd $ORDNER_MIT_Dockerfile
docker build -t snakeoil-${USER}:latest .
```

### Image im Container starten
```bash
# TODO: Port nach Aufgabenblatt anpassen
docker run -d -p 8000:8000 --name snakeoil-${USER} snakeoil-${USER}:latest 
``` 

### Terminal im Container verwenden
```bash
docker exec -ti snakeoil-${USER} bash -c 'echo Hallo ${CI_PROJECT_NAME} && bash' 
``` 

### Container stoppen
```bash
docker stop snakeoil-${USER}
```

### Container löschen
```bash
docker rm snakeoil-${USER}
```

### Images löschen / Speicherplatz schaffen
```bash
# Vorhandene Images listen
docker images
# Ausgewähltes Image löschen
docker rmi $IMAGE_ID
```
