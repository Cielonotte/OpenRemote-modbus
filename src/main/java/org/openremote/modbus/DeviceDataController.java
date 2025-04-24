package org.openremote.modbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        logger.info("API-endpoint /api/trigger-actie is aangeroepen.");
        // Hier roep je het Java-bestand aan dat de library gebruikt
        boolean actieGestart = startRelayViaLibrary();
        if (actieGestart) {
            return "Relay actie succesvol gesimuleerd!";
        } else {
            return "Fout bij simuleren relay actie.";
        }
    }

    // Simuleer de aanroep naar het Java-bestand met de library
    private boolean startRelayViaLibrary() {
        logger.info("Methode startRelayViaLibrary() is uitgevoerd.");
        // Hier zou de code staan die de library gebruikt om de relay aan te sturen
        // Voor de POC simuleren we dat het gelukt is
        return true;
    }

    // ... andere API-endpoint methoden die je eerder had ...
}