package org.openremote.modbus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DeviceDataController {

    private static final String DATA_FILE = "C:\\Users\\tomhe\\Desktop\\HBO ICT\\S2\\Openlearning\\Modbus Ind\\DataDisplay V2\\DataDisplay\\device.json";
    private final AtomicInteger deviceIdCounter = new AtomicInteger(getNextDeviceId());
    private final AtomicInteger registerIdCounter = new AtomicInteger(getNextRegisterId());
    private static final Logger logger = LoggerFactory.getLogger(DeviceDataController.class);

    @GetMapping("/devices")
    public List<Device> getDevices() {
        Map<String, Object> data = loadData();
        return objectMapper().convertValue(data.get("devices"), new TypeReference<List<Device>>() {});
    }

    @PostMapping("/devices")
    public Device addDevice(@RequestBody Device device) {
        Map<String, Object> data = loadData();
        List<Device> devices = objectMapper().convertValue(data.get("devices"), new TypeReference<List<Device>>() {});
        device.setId(deviceIdCounter.incrementAndGet());
        devices.add(device);
        data.put("devices", devices);
        saveData(data);
        return device;
    }

    @GetMapping("/registers/{deviceId}")
    public List<Register> getRegistersByDevice(@PathVariable int deviceId) {
        Map<String, Object> data = loadData();
        List<Register> allRegisters = objectMapper().convertValue(data.get("registers"), new TypeReference<List<Register>>() {});
        return allRegisters.stream()
                .filter(register -> register.getDeviceId() == deviceId)
                .collect(Collectors.toList());
    }

    @PostMapping("/registers")
    public Register addRegister(@RequestBody Register register) {
        Map<String, Object> data = loadData();
        List<Register> registers = objectMapper().convertValue(data.get("registers"), new TypeReference<List<Register>>() {});
        register.setId(registerIdCounter.incrementAndGet());
        registers.add(register);
        data.put("registers", registers);
        saveData(data);
        return register;
    }

    @PutMapping("/registers/{id}")
    public Register updateRegister(@PathVariable int id, @RequestBody Register updatedRegister) {
        logger.info("Verzoek ontvangen om register met ID {} te bewerken: {}", id, updatedRegister);
        Map<String, Object> data = loadData();
        List<Register> registers = objectMapper().convertValue(data.get("registers"), new TypeReference<List<Register>>() {});

        for (int i = 0; i < registers.size(); i++) {
            if (registers.get(i).getId() == id) {
                updatedRegister.setId(id); // Zorg ervoor dat het ID behouden blijft
                registers.set(i, updatedRegister);
                saveData(data);
                logger.info("Register met ID {} succesvol bewerkt: {}", id, updatedRegister);
                return updatedRegister;
            }
        }

        logger.warn("Register met ID {} niet gevonden.", id);
        throw new RuntimeException("Register met ID " + id + " niet gevonden.");
    }

    @DeleteMapping("/registers/{id}")
    public void deleteRegister(@PathVariable int id) {
        logger.info("Verzoek ontvangen om register met ID {} te verwijderen.", id);
        Map<String, Object> data = loadData();
        List<Register> registers = objectMapper().convertValue(data.get("registers"), new TypeReference<List<Register>>() {});

        registers.removeIf(register -> register.getId() == id);
        saveData(data);
        logger.info("Register met ID {} succesvol verwijderd.", id);
    }

    private Map<String, Object> loadData() {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(DATA_FILE);
        logger.info("Proberen data te laden uit bestand: {}", file.getAbsolutePath());
        try {
            if (file.exists()) {
                logger.info("Bestand bestaat. Bezig met lezen...");
                Map<String, Object> data = objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {});
                logger.info("Data succesvol geladen: {}", data);
                return data;
            } else {
                logger.warn("Bestand niet gevonden. Retourneer initiële data.");
                return Map.of("devices", new ArrayList<>(), "registers", new ArrayList<>());
            }
        } catch (IOException e) {
            logger.error("Fout bij het laden van data uit {}: {}", DATA_FILE, e.getMessage(), e);
            throw new RuntimeException("Fout bij het laden van data uit " + DATA_FILE, e);
        }
    }

    private void saveData(Map<String, Object> data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            logger.info("Bezig met opslaan van data naar {}: {}", DATA_FILE, data);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(DATA_FILE), data);
            logger.info("Data succesvol opgeslagen.");
        } catch (IOException e) {
            logger.error("Fout bij het opslaan van data naar {}: {}", DATA_FILE, e.getMessage(), e);
            throw new RuntimeException("Fout bij het opslaan van data naar " + DATA_FILE, e);
        }
    }

    private int getNextDeviceId() {
        Map<String, Object> data = loadData();
        List<Device> devices = objectMapper().convertValue(data.get("devices"), new TypeReference<List<Device>>() {});
        return devices.stream().mapToInt(Device::getId).max().orElse(0);
    }

    private int getNextRegisterId() {
        Map<String, Object> data = loadData();
        List<Register> registers = objectMapper().convertValue(data.get("registers"), new TypeReference<List<Register>>() {});
        return registers.stream().mapToInt(Register::getId).max().orElse(0);
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}