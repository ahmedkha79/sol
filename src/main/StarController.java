import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/vs/v1/system")
public class StarController {

    private ConcurrentHashMap<UUID, Component> components = new ConcurrentHashMap<>();
    private Star sol;  // Aktuell aktive SOL-Komponente

    // Registrierung einer neuen Komponente
    @PostMapping("/")
    public ResponseEntity<String> registerComponent(@RequestBody Component component) {
        if (sol == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No active SOL");
        }
        
        if (components.containsKey(component.getComUUID())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Component already registered");
        }

        components.put(component.getComUUID(), component);
        return ResponseEntity.ok("Component registered");
    }

    // Komponenten-Lebenszeichen
    @PatchMapping("/{comUUID}")
    public ResponseEntity<String> updateComponentStatus(
            @PathVariable UUID comUUID, 
            @RequestBody Component component) {
        if (!components.containsKey(comUUID)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Component not found");
        }

        components.put(comUUID, component);  // Status aktualisieren
        return ResponseEntity.ok("Status updated");
    }

    // Abmeldung einer Komponente
    @DeleteMapping("/{comUUID}")
    public ResponseEntity<String> deregisterComponent(@PathVariable UUID comUUID) {
        if (!components.containsKey(comUUID)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Component not found");
        }

        components.remove(comUUID);
        return ResponseEntity.ok("Component deregistered");
    }
    
    // Prüfen ob SOL vorhanden ist
    @GetMapping("/{comUUID}")
    public ResponseEntity<Component> checkComponentStatus(@PathVariable UUID comUUID) {
        Component component = components.get(comUUID);
        if (component == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(component);
    }
}
