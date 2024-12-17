package de.hamburg.sol.server.model;


import de.hamburg.sol.vs.server.model.ComponentInfo;
import de.hamburg.sol.vs.utils.InetAddressHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComponentInfoTest {

    @Test
    public void testTimeoutReset() {
        AtomicBoolean timeoutTriggered = new AtomicBoolean(false);
        ComponentInfo componentInfo = new ComponentInfo("9999", InetAddressHandler.getLocalHostAddress(), 8080);

        componentInfo.startTimeout(2, TimeUnit.SECONDS, () -> timeoutTriggered.set(true));
        try {


            Thread.sleep(2500);
            componentInfo.resetTimeout(2, TimeUnit.SECONDS);


            assertTrue(timeoutTriggered.get());

            componentInfo.stopTimeout();

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
