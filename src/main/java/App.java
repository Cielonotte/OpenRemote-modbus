import org.openremote.modbus.conn_slave;
import org.openremote.modbus.connector.tcp_connector;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) throws Exception {
        // System.out.println(devices.init());
        // conn_slave.start();
        // conn_master.start();
        boolean coil = tcp_connector.readcoil("10.0.0.103", 4196, 2);
        System.out.println(coil);
        TimeUnit.SECONDS.sleep(1);
        tcp_connector.writecoil("10.0.0.103", 4196, 2, (!coil));
        System.out.println(tcp_connector.readcoil("10.0.0.103", 4196, 2));
        TimeUnit.SECONDS.sleep(1);
        conn_slave.close();
        // System.out.println(devices.get());
    }
}