package org.openremote.modbus;

import org.slf4j.Logger; // Zorg dat deze import er is
import org.slf4j.LoggerFactory; // Zorg dat deze import er is

import java.util.ArrayList;
import java.util.List;

public class MockDeviceGenerator {

    private static final Logger logger = LoggerFactory.getLogger(MockDeviceGenerator.class); // Voeg deze regel toe

    public static List<Device> generateMockDevices(String startIpStr, String endIpStr) {
        logger.info("MOCK GENERATOR: Start met genereren van apparaten voor bereik: {}-{}", startIpStr, endIpStr); // Log 3
        List<Device> devices = new ArrayList<>();
        long startLong = ipToLong(startIpStr);
        long endLong = ipToLong(endIpStr);

        if (startLong > endLong) {
            logger.warn("MOCK GENERATOR: Start IP is groter dan Eind IP. Geen apparaten gegenereerd."); // Log 4
            return devices; // Retourneer een lege lijst als het bereik ongeldig is
        }

        for (long i = startLong; i <= endLong; i++) {
            String ipAddress = longToIp(i);
            int deviceId = (int) (i - startLong + 1); // Simpele ID voor mock-apparaten
            Device device = new Device();
            device.setId(deviceId);
            device.setName("Simulated Device " + deviceId);
            device.setAddress(ipAddress);
            device.setConnectionLink("Simulated TCP");
            device.setRefreshRate(5); // Standaard refresh rate voor simulatie
            devices.add(device);
            logger.debug("MOCK GENERATOR: Apparaat toegevoegd: {}", device.getAddress()); // Log 5 (gebruik debug voor veel output)
        }
        logger.info("MOCK GENERATOR: Generatie voltooid. Totaal aantal gesimuleerde apparaten: {}", devices.size()); // Log 6
        return devices;
    }

    private static long ipToLong(String ipAddress) {
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            logger.error("IP TO LONG FOUT: Ongeldig IP-formaat: {}", ipAddress);
            throw new IllegalArgumentException("Invalid IP address format: " + ipAddress);
        }
        long result = 0;
        for (int i = 0; i < 4; i++) {
            int part = Integer.parseInt(parts[i]);
            if (part < 0 || part > 255) {
                logger.error("IP TO LONG FOUT: IP-deel buiten bereik: {}", part);
                throw new IllegalArgumentException("IP address part out of range (0-255): " + part);
            }
            result = (result << 8) | part;
        }
        return result;
    }

    private static String longToIp(long ipLong) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.insert(0, (ipLong & 0xFF));
            if (i < 3) {
                sb.insert(0, ".");
            }
            ipLong >>= 8;
        }
        return sb.toString();
    }
}