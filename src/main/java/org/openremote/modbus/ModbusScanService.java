package org.openremote.modbus;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ModbusScanService {

    private static final Logger logger = LoggerFactory.getLogger(ModbusScanService.class);
    private static final int MODBUS_PORT = 502;
    // Gebruik een thread pool voor parallelle scans om het sneller te maken
    private final ExecutorService executorService = Executors.newFixedThreadPool(50); // Bijv. 50 threads

    /**
     * Scans a given IP range for Modbus TCP devices.
     * @param startIp The starting IP address (e.g., "127.0.0.2")
     * @param endIp The ending IP address (e.g., "127.0.0.253")
     * @return A list of found Modbus devices with basic info.
     */
    public List<Device> scanModbusDevices(String startIp, String endIp) {
        List<Device> foundDevices = new ArrayList<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        try {
            String[] startParts = startIp.split("\\.");
            String[] endParts = endIp.split("\\.");

            if (startParts.length != 4 || endParts.length != 4) {
                logger.error("Ongeldig IP-bereik opgegeven: {} - {}", startIp, endIp);
                return foundDevices;
            }

            // Aanname: Alleen het laatste octet varieert
            int startLastOctet = Integer.parseInt(startParts[3]);
            int endLastOctet = Integer.parseInt(endParts[3]);
            String baseIp = String.format("%s.%s.%s.", startParts[0], startParts[1], startParts[2]);

            logger.info("Start Modbus scan van {} tot {}", startIp, endIp);

            for (int i = startLastOctet; i <= endLastOctet; i++) {
                final String currentIp = baseIp + i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    if (testModbusConnection(currentIp)) {
                        synchronized (foundDevices) { // Synchroniseer toegang tot de lijst
                            // ID wordt door de DeviceDataController gegenereerd, 0 is placeholder
                            // refreshRate is hier ook een placeholder; wordt via UI ingesteld
                            Device device = new Device(0, "Modbus Apparaat op " + currentIp, currentIp, "TCP", 5);
                            foundDevices.add(device);
                        }
                    }
                }, executorService);
                futures.add(future);
            }

            // Wacht op alle parallelle taken om te voltooien
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            logger.info("Modbus scan voltooid. Aantal gevonden apparaten: {}", foundDevices.size());

        } catch (NumberFormatException e) {
            logger.error("Fout bij het parsen van IP-adressen in scanbereik: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Algemene fout tijdens Modbus scan: {}", e.getMessage(), e);
        }
        // De executorService wordt niet hier afgesloten zodat de service hergebruikt kan worden.
        // Voor een Spring @Service wordt de levenscyclus beheerd door Spring.
        return foundDevices;
    }

    /**
     * Attempts to connect and perform a basic Modbus query to verify if it's a Modbus device.
     * @param ipAddress The IP address to test.
     * @return true if a Modbus device is likely present, false otherwise.
     */
    private boolean testModbusConnection(String ipAddress) {
        TCPMasterConnection connection = null;
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            connection = new TCPMasterConnection(address);
            connection.setPort(MODBUS_PORT);
            connection.setTimeout(1000); // Korte timeout voor snelle scan

            connection.connect();

            if (connection.isConnected()) {
                ModbusTCPTransaction trans = new ModbusTCPTransaction(connection);
                trans.setRetries(0); // Geen retries voor snelle scan

                ReadInputRegistersRequest req = new ReadInputRegistersRequest(0, 1);
                req.setUnitID(1); // Modbus slave ID, vaak 1 voor de eerste/enige slave

                trans.setRequest(req);
                trans.execute();

                ReadInputRegistersResponse res = (ReadInputRegistersResponse) trans.getResponse();
                if (res != null) {
                    logger.debug("Gevonden Modbus apparaat op: {} - Responded to query.", ipAddress);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.debug("Geen Modbus apparaat (of fout) op {}: {}", ipAddress, e.getMessage());
        } finally {
            if (connection != null && connection.isConnected()) {
                connection.close();
            }
        }
        return false;
    }

    // @PreDestroy methode kan hier worden toegevoegd om de executorService af te sluiten
    // wanneer de Spring context wordt afgesloten.
    // @PreDestroy
    // public void shutdown() {
    //     executorService.shutdown();
    //     try {
    //         if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
    //             executorService.shutdownNow();
    //         }
    //     } catch (InterruptedException e) {
    //         executorService.shutdownNow();
    //         Thread.currentThread().interrupt();
    //     }
    // }
}