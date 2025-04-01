package org.openremote.modbus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
public class DeviceDataController {

    private static final String DATA_FILE = "device.json";
    private final AtomicInteger deviceIdCounter = new AtomicInteger(getNextDeviceId());
    private final AtomicInteger registerIdCounter = new AtomicInteger(getNextRegisterId());

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
        saveData(data);
        return device;
    }

    @GetMapping("/registers/{deviceId}")
    public List<Register> getRegistersByDevice(@PathVariable int deviceId) {
        Map<String, Object> data = loadData();
        List<Register> allRegisters = objectMapper().convertValue(data.get("registers"), new TypeReference<List<Register>>() {});
        List<Register> deviceRegisters = new ArrayList<>();
        for (Register register : allRegisters) {
            if (register.getDeviceId() == deviceId) {
                deviceRegisters.add(register);
            }
        }
        return deviceRegisters;
    }

    @PostMapping("/registers")
    public Register addRegister(@RequestBody Register register) {
        Map<String, Object> data = loadData();
        List<Register> registers = objectMapper().convertValue(data.get("registers"), new TypeReference<List<Register>>() {});
        register.setId(registerIdCounter.incrementAndGet());
        registers.add(register);
        saveData(data);
        return register;
    }

    private Map<String, Object> loadData() {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(DATA_FILE);
        try {
            if (file.exists()) {
                return objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {});
            } else {
                return Map.of("devices", new ArrayList<>(), "registers", new ArrayList<>());
            }
        } catch (IOException e) {
            throw new RuntimeException("Fout bij het laden van data uit " + DATA_FILE, e);
        }
    }

    private void saveData(Map<String, Object> data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(DATA_FILE), data);
        } catch (IOException e) {
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