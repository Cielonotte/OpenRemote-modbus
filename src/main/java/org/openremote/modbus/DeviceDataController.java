package org.openremote.modbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.openremote.modbus.connector.socket;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class DeviceDataController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceDataController.class);

    public static void main(String[] args) {
        SpringApplication.run(DeviceDataController.class, args);
    }

    @GetMapping("/trigger-actie")
    public String startRelayActiePoc() {
        logger.info("API-endpoint /api/trigger-actie was called");

        boolean toggleRelay = socket.writecoil("10.0.0.103", 502, "tcp", 0, 21760);

        if (!toggleRelay) {
            return "An error occurred";
        }

        boolean relayStatus = socket.readcoil("10.0.0.103", 502, "tcp", 0);

        if (relayStatus) {
            return "Relay turned on";
        }
        return "Relay turned off";
    }

    // Simuleer de aanroep naar het Java-bestand met de library
    private boolean startRelayViaLibrary() {
        logger.info("Methode startRelayViaLibrary() has run successfully");
        // Hier zou de code staan die de library gebruikt om de relay aan te sturen
        // Voor de POC simuleren we dat het gelukt is
        return true;
    }

    // ... andere API-endpoint methoden die je eerder had ...
}