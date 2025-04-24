package org.openremote.modbus;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;

import java.util.concurrent.TimeUnit;

public class conn_master {
    public static ModbusTCPMaster master;

    public static void start() {
        try {
            master = new ModbusTCPMaster("127.0.0.1", 5020);
            master.connect();
        }
        catch (Exception e) {
            System.out.println("Cannot connect to slave: " + e.getMessage());
        }

        try {
            boolean coil = master.readCoils(0, 1).getBit(0);
            System.out.println(coil);
            
            TimeUnit.SECONDS.sleep(1);

            master.writeCoil(0, (!coil));
            System.out.println(master.readCoils(0, 1).getBit(0));
        }
        catch (Exception e) {
            System.out.println("Error in communication: " + e.getMessage());
        }
    }
}
