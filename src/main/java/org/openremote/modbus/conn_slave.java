package org.openremote.modbus;

import com.ghgande.j2mod.modbus.procimg.SimpleDigitalOut;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;

public class conn_slave {
    public static void start() throws Exception {
        SimpleProcessImage spi = new SimpleProcessImage();
        spi.addDigitalOut(new SimpleDigitalOut(false));

        ModbusSlave slave = ModbusSlaveFactory.createTCPSlave(5020, 5);

        slave.addProcessImage(1, spi);
        slave.open();
    }

    public static void close() {
        ModbusSlaveFactory.close();
    }
}