package de.hamburg.sol.vs.galaxy.model;


import de.hamburg.sol.vs.galaxy.datatype.StarInfo;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@NoArgsConstructor
@Log4j2
public class GalaxyModel {

    private ConcurrentHashMap<String, StarInfo> stars = new ConcurrentHashMap<>();




    public void putStarIntoMap(StarInfo starInfo){
        stars.put(starInfo.getStar(), starInfo);
        log.info("Size: {} ", stars.size());
    }

    public StarInfo getStarInfo(String starUUID){
        return stars.get(starUUID);
    }

    public boolean containsStar(String starUUID){
        return stars.containsKey(starUUID);
    }

    public List<StarInfo>  getAllStars(){
        return new ArrayList<>(stars.values());
    }


}
