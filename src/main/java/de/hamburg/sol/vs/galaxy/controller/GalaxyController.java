package de.hamburg.sol.vs.galaxy.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import de.hamburg.sol.vs.config.global.GlobalConfig;
import de.hamburg.sol.vs.galaxy.datatype.StarInfo;
import de.hamburg.sol.vs.galaxy.datatype.StarInfoList;
import de.hamburg.sol.vs.galaxy.model.GalaxyModel;
import de.hamburg.sol.vs.server.instance.SolServer;
import de.hamburg.sol.vs.utils.ProtocolHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;

import static com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.UUID;

@RestController
@RequestMapping("/vs/v1/star")
@Log4j2
@Lazy
public class GalaxyController {


    private GalaxyModel galaxyModel;
    private String solStarUUID;
    private SolServer solServer;


    public GalaxyController(@Lazy SolServer solServer, GalaxyModel galaxyModel) throws IllegalAccessException {
            this.solServer = solServer;
            this.galaxyModel = galaxyModel;
            this.solStarUUID = GlobalConfig.getStarUUID();
    }


    @PostMapping()
    public ResponseEntity<String> registerStar(@RequestBody StarInfo starInfo) throws JsonProcessingException {
        galaxyModel.putStarIntoMap(starInfo);

        log.info("Stern: {} zu den bekannten Galaxien hinzugefügt", starInfo.getStar());

        StarInfo solStarInfo = galaxyModel.getStarInfo(solStarUUID);

        String starInfoConverted = ProtocolHandler.writeValueAsString(solStarInfo);

        log.info("Eigene StarInfo abgeschickt");

        return ResponseEntity.status(200).body(starInfoConverted);
    }

    @PatchMapping("/{star}")
    public ResponseEntity<String> updateStar(@PathVariable String star, @RequestBody StarInfo starInfo){
       if(!checkStar(star, starInfo)) {
           log.warn("Stern: {} stimmt nicht überein mit Stern:{}", star, starInfo.getStar());
           return ResponseEntity.status(HttpStatus.CONFLICT).build();
       }



       galaxyModel.putStarIntoMap(starInfo);

       log.info("Stern: {} erfolgreich gepatched", star);

       return ResponseEntity.status(200).body(HttpStatus.OK.getReasonPhrase());

    }



    @GetMapping("/{star}")
    public ResponseEntity<?> getStar(@PathVariable String star) throws JsonProcessingException {
        if(!galaxyModel.containsStar(star)){
            log.warn("Angeforderter Stern: {} nicht bekannt", star);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        log.info("Angeforderter Stern: {} in der Galaxie bekannt", star);

        return ResponseEntity.status(200).body(galaxyModel.getStarInfo(star));
    }

    @GetMapping()
    public ResponseEntity<?> getAllStars(){
        List<StarInfo> starInfoList = galaxyModel.getAllStars();
        StarInfoList stars = StarInfoList.builder().totalResults(starInfoList.size()).stars(starInfoList).build();
        return ResponseEntity.status(200).body(stars);


    }

    @DeleteMapping("/{star}")
    public ResponseEntity<String> deleteStars(@PathVariable String star, HttpServletRequest request){

        if(!galaxyModel.containsStar(star)){
            log.warn("Der Stern: {} ist unbekannt und kann nicht gelöscht werden", star);
            return ResponseEntity.status(404).body(HttpStatus.NOT_FOUND.getReasonPhrase());
        }
        StarInfo starInfo = galaxyModel.getStarInfo(star);
        String requestIp = extractIpAddress(request);


         if(!starInfo.getStatus().equals("200")){
             log.warn("Stern: {} ist nicht als aktiv markiert", star);
             log.info("Status: {} des Sterns: {} ", starInfo.getStatus(), star );
             return ResponseEntity.status(404).body(HttpStatus.NOT_FOUND.getReasonPhrase());
         }


        if(!checkIpAddress(starInfo.getIpAddress(), requestIp)){
            log.info("Die Ip des Requests: {} stimmt nicht überein mit der Ip des Stern: {}", requestIp, starInfo.getIpAddress());
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        starInfo.setStatus("left");
        galaxyModel.putStarIntoMap(starInfo);

        log.info("Stern: {} erfolgreich entfernt", star);

        return ResponseEntity.status(200).body(HttpStatus.OK.getReasonPhrase());


    }




    private boolean checkStar(String starUUID, StarInfo starInfo){
        return starInfo.getStar().equals(starUUID);
    }

    private boolean checkIpAddress(String ipAddress, String otherIpAddress){
        return ipAddress.trim().equals(otherIpAddress.trim());
    }

    private String extractIpAddress(HttpServletRequest request){
        String clientIp;
        String xForwardedHeader = request.getHeader("X-Forwarded-For");
        if(xForwardedHeader != null && !xForwardedHeader.isEmpty()){
            clientIp = xForwardedHeader.split(",")[0];
        } else {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}
