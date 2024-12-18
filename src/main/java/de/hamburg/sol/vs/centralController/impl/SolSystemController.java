package de.hamburg.sol.vs.centralController.impl;

import de.hamburg.sol.vs.centralController.api.SystemHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vs/v1/system")
public class SolSystemController {

    private final SystemHandler systemHandler;


    public SolSystemController(@Lazy SystemHandler systemHandler) {
        this.systemHandler = systemHandler;
    }

    @GetMapping("/{comUUID}")
    public ResponseEntity<String> getComponent(@PathVariable String comUUID, @RequestParam() String star){
        return systemHandler.handleGetRequest(comUUID, star);
    }

    @DeleteMapping("/{comUUID}")
    public ResponseEntity<String> handleDeleteRequest(@PathVariable String comUUID, @RequestParam() String star,
                                                      @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor, HttpServletRequest request){
        return systemHandler.handleDeleteRequest(comUUID, star);
    }
}
