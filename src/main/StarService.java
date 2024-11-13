import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class StarService {
    private Star sol;
    private ConcurrentHashMap<UUID, Component> components = new ConcurrentHashMap<>();

    public Star createSol(String ip, int port) {
        Star newSol = new Star();
        newSol.setStarUUID(UUID.randomUUID());
        newSol.setSolUUID(UUID.randomUUID());
        newSol.setSolIp(ip);
        newSol.setSolTcpPort(port);
        this.sol = newSol;
        return newSol;
    }

    public boolean addComponent(Component component) {
        if (components.containsKey(component.getComUUID())) return false;
        components.put(component.getComUUID(), component);
        return true;
    }

    public boolean updateComponentStatus(UUID comUUID, int status) {
        Component component = components.get(comUUID);
        if (component != null) {
            component.setStatus(status);
            return true;
        }
        return false;
    }

    public boolean removeComponent(UUID comUUID) {
        return components.remove(comUUID) != null;
    }
}
