package org.openremote.modbus.connector;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;

public class tcp_connector {
    public static ModbusTCPMaster master;

    public static boolean readcoil(String ip_address, int port, int coil) {
        try {
            master = new ModbusTCPMaster(ip_address, port);
            master.connect();
        }
        catch (Exception e) {
            System.out.println("Cannot connect to slave: " + e.getMessage());
            return false;
        }

        try {
            boolean result = master.readCoils(coil, 1).getBit(0);
            master.disconnect();
            return result;
        }
        catch (Exception e) {
            System.out.println("Error in communication: " + e.getMessage());
            return false;
        }
    }

    public static boolean writecoil(String ip_address, int port, int coil, boolean state) {
        try {
            master = new ModbusTCPMaster(ip_address, port);
            master.connect();
        }
        catch (Exception e) {
            System.out.println("Cannot connect to slave: " + e.getMessage());
            return false;
        }

        try {
            master.writeCoil(coil, state);
            master.disconnect();
            return true;
        }
        catch (Exception e) {
            System.out.println("Error in communication: " + e.getMessage());
            return false;
        }
    }
}
