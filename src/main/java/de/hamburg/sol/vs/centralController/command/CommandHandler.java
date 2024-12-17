package de.hamburg.sol.vs.centralController.command;


import de.hamburg.sol.vs.centralController.api.SystemHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/vs/v1/system")
public class CommandHandler {

    private final SystemHandler systemHandler;


    public CommandHandler(@Lazy SystemHandler systemHandler) {
        this.systemHandler = systemHandler;
    }

    @PostMapping("/command")
    public void handleCommand(@RequestParam("command") String command) {
        switch (command.trim().toUpperCase()) {
            case "CRASH":
                simulateCrash();
            case "EXIT":
                systemHandler.handleExitCommand();

        }
    }


    private void simulateCrash(){
        log.error("Applikation gecrasht");
        System.exit(1);
    }
}
