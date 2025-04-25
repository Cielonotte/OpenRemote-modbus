import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class socket {
    public static byte[] checksum(byte[] data) {
        int crc = 0xFFFF;
        int polynomial = 0xA001;

        int x = 0;
        for (byte b : data) {
            if (x < (data.length - 2)){
                crc ^= (b & 0xFF);
                for (int i = 0; i < 8; i++) {
                    if ((crc & 0x0001) != 0) {
                        crc >>= 1;
                        crc ^= polynomial;
                    }
                    else {
                        crc >>= 1;
                    }
                }
            x++;
            }
        }
        int result = ((crc & 0xFF) << 8) | ((crc >> 8) & 0xFF);
        data[(data.length - 2)] = (byte)(result / 256);
        data[(data.length - 1)] = (byte)(result % 256);
        return data;
    }

    public static void connect(String ipaddress, int port, byte[] packet) {
        try (Socket socket = new Socket(ipaddress, port);
            OutputStream outputStream = socket.getOutputStream()) {

            outputStream.write(packet);
            outputStream.flush();
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void send(String ipaddress, int port, String protocol, int address, int value) {
        if (protocol == "tcp") {
            byte[] packet = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x01, (byte)0x05, (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x00};
            packet[9] = (byte)address;
            packet[10] = (byte)(value / 256);
            packet[11] = (byte)(value % 256);
            connect(ipaddress, port, packet);
        }
        else if (protocol == "rtu") {
            byte[] packet = {(byte)0x01, (byte)0x05, (byte)0x00, (byte)0x01, (byte)0x55, (byte)0x00, (byte)0x00, (byte)0x00};
            packet[3] = (byte)address;
            packet[4] = (byte)(value / 256);
            packet[5] = (byte)(value % 256);
            connect(ipaddress, port, checksum(packet));
        }
        else {
            System.out.println("Unsuppored protocol given to socket.send");
        }
    }
    
    public static void main(String[] args) {
        send("10.0.0.103", 502, "tcp", 0, 65280);
        try {
            Thread.sleep(500);
        }
        catch(InterruptedException e) {
            System.out.println("got interrupted!");
        }
        send("10.0.0.103", 502, "tcp", 0, 0);
    }
}