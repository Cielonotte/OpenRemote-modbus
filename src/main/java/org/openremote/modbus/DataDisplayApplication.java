package org.openremote.modbus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class DataDisplayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataDisplayApplication.class, args);
    }

    public static List<Device> getSimulatedDeviceData() {
        List<Device> deviceDatalist = new ArrayList<>();
        deviceDatalist.add(new Device(1, "VFD", "192.168.123.50", "TCP"));
        deviceDatalist.add(new Device(2, "Relay", "COM3", "RTU"));
        deviceDatalist.add(new Device(3, "PLC", "192.168.123.52", "TCP"));
        return deviceDatalist;
    }
}