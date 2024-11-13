import java.util.UUID;
import lombok.Data;

@Data
public class Star {
    private UUID starUUID;
    private UUID solUUID;
    private String solIp;
    private int solTcpPort;
}

@Data
public class Component {
    private UUID comUUID;
    private String ip;
    private int port;
    private int status;
}
