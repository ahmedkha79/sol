package de.hamburg.sol.vs.client.messages;


import de.hamburg.sol.vs.client.model.instance.SolComponent;
import de.hamburg.sol.vs.messages.api.MessageHandler;
import de.hamburg.sol.vs.messages.datatype.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Lazy
public class MessageHandlerSolComponent implements MessageHandler {

    @Qualifier("dynamicSolComponent")
    private SolComponent solComponent;

    private RestTemplate restTemplate;

    private String baseMessageUrl;

    public MessageHandlerSolComponent(RestTemplate restTemplate, @Lazy SolComponent solComponent) {
        this.restTemplate = restTemplate;
        this.solComponent = solComponent;
        this.baseMessageUrl = String.format("http://%s:%d/vs/v2/messages", solComponent.getSolIpAddress(), solComponent.getSolPort());
    }

    @Override
    public ResponseEntity<String> handlePostMessageRequest(Message message) {
        return restTemplate.postForEntity(baseMessageUrl, message, String.class);
    }

    @Override
    public ResponseEntity<String> handleReceivedPostMessageRequest(String msg_id, Message message) {
        return restTemplate.postForEntity(baseMessageUrl + "/" + msg_id, message, String.class);
    }

    @Override
    public ResponseEntity<String> handleGetMessageRequest(String star, String scope, String view) {
        String url = String.format("%s?star=%s&scope=%s", baseMessageUrl, star, scope);
        return restTemplate.getForEntity(url, String.class);
    }

    @Override
    public ResponseEntity<String> handleGetSingleMessage(String msg_id, String star) {
        String url = String.format("%s/%s?star=%s", baseMessageUrl, msg_id, star);
        return restTemplate.getForEntity(url, String.class);
    }

    @Override
    public ResponseEntity<String> handleDeleteMessage(String msg_id, String star) {
        String url = String.format("%s/%s?star=%s", baseMessageUrl, msg_id, star);
       return restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
    }
}
