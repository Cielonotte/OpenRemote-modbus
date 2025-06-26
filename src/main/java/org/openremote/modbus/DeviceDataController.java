package org.openremote.modbus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Belangrijk: Importeer @Value
import org.springframework.stereotype.Service; // Of @Component, afhankelijk van de rol
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct; // Importeer PostConstruct

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths; // Voor robuustere pad manipulatie
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList; // Gebruik voor thread-safe lijst
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DeviceDataController {

    // BELANGRIJK: Dit moet het ABSOLUTE PAD naar je device.json file zijn.
    // Dit pad moet verwijzen naar het JSON-bestand zelf, niet naar een Java-klasse.
    // Controleer dit pad DUBBEL! Gebruik forward slashes of dubbele backslashes.
    private static final String DATA_FILE_PATH = "C:/Users/tomhe/Desktop/HBO ICT/S2/Openlearning/Modbus Ind/DataDisplay-DataLogger/DataDisplay/src/main/resources/device.json";

    private final AtomicInteger deviceIdCounter = new AtomicInteger(0); // Initialiseer met 0, wordt later gezet
    private final AtomicInteger registerIdCounter = new AtomicInteger(0); // Initialiseer met 0, wordt later gezet
    private static final Logger logger = LoggerFactory.getLogger(DeviceDataController.class);

    private final ModbusScanService modbusScanService;
    private final ObjectMapper objectMapper; // Maak ObjectMapper een final veld
    
    // De in-memory lijsten voor apparaten en registers
    // Gebruik CopyOnWriteArrayList voor thread-veiligheid bij add/remove
    private List<Device> devicesInMemory = new CopyOnWriteArrayList<>();
    private List<Register> registersInMemory = new CopyOnWriteArrayList<>();

    // De constructor om ModbusScanService te injecteren
    @Autowired
    public DeviceDataController(ModbusScanService modbusScanService) {
        this.modbusScanService = modbusScanService;
        this.objectMapper = new ObjectMapper(); // Eenmalig initialiseren
    }

    // Deze methode wordt automatisch aangeroepen door Spring na constructie
    @PostConstruct
    public void init() {
        loadData(); // Laad de initiële data bij opstarten
        // Initialiseer ID-tellers op basis van de geladen data
        this.deviceIdCounter.set(getNextDeviceIdFromLoadedData());
        this.registerIdCounter.set(getNextRegisterIdFromLoadedData());
        logger.info("DeviceDataController geïnitialiseerd. Huidige Device ID teller: {}, Register ID teller: {}", 
                    deviceIdCounter.get(), registerIdCounter.get());
    }

    @GetMapping("/devices")
    public List<Device> getDevices() {
        return devicesInMemory; // Retourneer de in-memory lijst
    }

    @PostMapping("/devices")
    public Device addDevice(@RequestBody Device device) {
        // Controleer of een apparaat met hetzelfde adres al bestaat in de in-memory lijst
        boolean alreadyExists = devicesInMemory.stream()
                .anyMatch(d -> d.getAddress().equals(device.getAddress()));

        if (alreadyExists) {
            logger.warn("Apparaat met adres {} bestaat al, wordt niet toegevoegd.", device.getAddress());
            throw new RuntimeException("Apparaat met adres " + device.getAddress() + " bestaat al.");
        }

        device.setId(deviceIdCounter.incrementAndGet()); // Increment en krijg nieuwe ID
        devicesInMemory.add(device); // Voeg toe aan in-memory lijst
        
        // Sla alle bijgewerkte data (devices en registers) op
        Map<String, Object> dataToSave = Map.of(
            "devices", devicesInMemory,
            "registers", registersInMemory // Zorg ervoor dat registers ook worden opgeslagen
        );
        saveData(dataToSave); // Sla de in-memory data op naar het bestand
        
        logger.info("Apparaat toegevoegd: {}", device);
        return device;
    }

    @GetMapping("/scan-modbus")
    public List<Device> scanModbusDevices(@RequestParam(defaultValue = "127.0.0.2") String startIp,
                                          @RequestParam(defaultValue = "127.0.0.253") String endIp) {
        logger.info("API-verzoek ontvangen voor Modbus scan van {} tot {}", startIp, endIp);
        List<Device> foundDevices = modbusScanService.scanModbusDevices(startIp, endIp);
        logger.info("Modbus scan API voltooid. {} apparaten gevonden.", foundDevices.size());
        return foundDevices;
    }

    @GetMapping("/registers/{deviceId}")
    public List<Register> getRegistersByDevice(@PathVariable int deviceId) {
        // Filter de in-memory registers
        return registersInMemory.stream()
                .filter(register -> register.getDeviceId() == deviceId)
                .collect(Collectors.toList());
    }

    @PostMapping("/registers")
    public Register addRegister(@RequestBody Register register) {
        register.setId(registerIdCounter.incrementAndGet()); // Increment en krijg nieuwe ID
        registersInMemory.add(register); // Voeg toe aan in-memory lijst
        
        // Sla alle bijgewerkte data op
        Map<String, Object> dataToSave = Map.of(
            "devices", devicesInMemory,
            "registers", registersInMemory
        );
        saveData(dataToSave); // Sla de in-memory data op naar het bestand

        logger.info("Register toegevoegd: {}", register);
        return register;
    }

    @PutMapping("/registers/{id}")
    public Register updateRegister(@PathVariable int id, @RequestBody Register updatedRegister) {
        logger.info("Verzoek ontvangen om register met ID {} te bewerken: {}", id, updatedRegister);
        boolean found = false;
        for (int i = 0; i < registersInMemory.size(); i++) {
            if (registersInMemory.get(i).getId() == id) {
                updatedRegister.setId(id); // Zorg ervoor dat het ID behouden blijft
                registersInMemory.set(i, updatedRegister); // Update in-memory lijst
                found = true;
                break;
            }
        }

        if (found) {
            // Sla alle bijgewerkte data op
            Map<String, Object> dataToSave = Map.of(
                "devices", devicesInMemory,
                "registers", registersInMemory
            );
            saveData(dataToSave); // Sla de in-memory data op naar het bestand
            logger.info("Register met ID {} succesvol bewerkt: {}", id, updatedRegister);
            return updatedRegister;
        } else {
            logger.warn("Register met ID {} niet gevonden.", id);
            throw new RuntimeException("Register met ID " + id + " niet gevonden.");
        }
    }

    @DeleteMapping("/registers/{id}")
    public void deleteRegister(@PathVariable int id) {
        logger.info("Verzoek ontvangen om register met ID {} te verwijderen.", id);
        
        boolean removed = registersInMemory.removeIf(register -> register.getId() == id); // Verwijder uit in-memory lijst
        
        if (removed) {
            // Sla alle bijgewerkte data op
            Map<String, Object> dataToSave = Map.of(
                "devices", devicesInMemory,
                "registers", registersInMemory
            );
            saveData(dataToSave); // Sla de in-memory data op naar het bestand
            logger.info("Register met ID {} succesvol verwijderd.", id);
        } else {
            logger.warn("Register met ID {} niet gevonden voor verwijdering.", id);
            throw new RuntimeException("Register met ID " + id + " niet gevonden.");
        }
    }

    // Laadt data van het JSON-bestand bij applicatie-opstart
    private void loadData() {
        File file = new File(DATA_FILE_PATH);
        logger.info("Proberen data te laden uit bestand: {}", file.getAbsolutePath());
        try {
            if (file.exists()) {
                logger.info("Bestand bestaat. Bezig met lezen...");
                Map<String, Object> data = objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {});
                
                // Converteer en zet de geladen lijsten in de in-memory variabelen
                Object devicesObj = data.get("devices");
                if (devicesObj instanceof List) {
                    this.devicesInMemory = objectMapper.convertValue(devicesObj, new TypeReference<List<Device>>() {});
                } else {
                    this.devicesInMemory = new CopyOnWriteArrayList<>();
                }

                Object registersObj = data.get("registers");
                if (registersObj instanceof List) {
                    this.registersInMemory = objectMapper.convertValue(registersObj, new TypeReference<List<Register>>() {});
                } else {
                    this.registersInMemory = new CopyOnWriteArrayList<>();
                }
                
                logger.info("Data succesvol geladen: {} apparaten, {} registers.", 
                            this.devicesInMemory.size(), this.registersInMemory.size());

            } else {
                logger.warn("Bestand niet gevonden. Maak een nieuw bestand aan: {}", file.getAbsolutePath());
                // Maak de directory indien deze niet bestaat
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                // Initialiseer lege lijsten
                this.devicesInMemory = new CopyOnWriteArrayList<>();
                this.registersInMemory = new CopyOnWriteArrayList<>();
                
                Map<String, Object> initialData = Map.of("devices", devicesInMemory, "registers", registersInMemory);
                try {
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, initialData);
                    logger.info("Nieuwe device.json aangemaakt op {}", file.getAbsolutePath());
                } catch (IOException createEx) {
                    logger.error("Kon nieuwe device.json niet aanmaken op {}: {}", file.getAbsolutePath(), createEx.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Fout bij het laden van data uit {}: {}", DATA_FILE_PATH, e.getMessage(), e);
            throw new RuntimeException("Fout bij het laden van data uit " + DATA_FILE_PATH, e);
        }
    }

    // Slaat de huidige in-memory data op naar het JSON-bestand
    private void saveData(Map<String, Object> data) {
        try {
            File file = new File(DATA_FILE_PATH);
            logger.info("Bezig met opslaan van data naar {}: {}", file.getAbsolutePath(), data);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
            logger.info("Data succesvol opgeslagen.");
        } catch (IOException e) {
            logger.error("Fout bij het opslaan van data naar {}: {}", DATA_FILE_PATH, e.getMessage(), e);
            throw new RuntimeException("Fout bij het opslaan van data naar " + DATA_FILE_PATH, e);
        }
    }

    // Deze methoden worden alleen gebruikt om de AtomicInteger counters bij opstarten te initialiseren
    private int getNextDeviceIdFromLoadedData() {
        return devicesInMemory.stream().mapToInt(Device::getId).max().orElse(0);
    }

    private int getNextRegisterIdFromLoadedData() {
        return registersInMemory.stream().mapToInt(Register::getId).max().orElse(0);
    }

    // Omdat ObjectMapper nu een final veld is, is deze methode niet meer nodig.
    // private ObjectMapper objectMapper() {
    //     return new ObjectMapper();
    // }
}