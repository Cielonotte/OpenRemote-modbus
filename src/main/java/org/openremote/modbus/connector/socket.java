import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class socket {
    public static String serverAddress = "10.0.0.103";
    public static int port = 4196;

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

    public static void send(byte[] array) {
        try (Socket socket = new Socket(serverAddress, port);
            OutputStream outputStream = socket.getOutputStream()) {

            outputStream.write(checksum(array));
            outputStream.flush();
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void auto() {
        byte[] on = {(byte)0x01, (byte)0x05, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00};
        byte[] off = {(byte)0x01, (byte)0x05, (byte)0x00, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};

        send(on);

        try {
            Thread.sleep(500);
        }
        catch(InterruptedException e) {
            System.out.println("got interrupted!");
        }

        send(off);

        try {
            Thread.sleep(500);
        }
        catch(InterruptedException e) {
            System.out.println("got interrupted!");
        }
    }
    
    public static void main(String[] args) {
        byte[] toggle = {(byte)0x01, (byte)0x05, (byte)0x00, (byte)0x01, (byte)0x55, (byte)0x00, (byte)0x00, (byte)0x00};

        boolean run = true;
        Scanner userInput = new Scanner(System.in);
        while(run == true) {
            System.out.println("Enter relay number: ");
            int input = Integer.parseInt(userInput.nextLine()) - 1;
            if(input == 8) {
                auto();
            }
            else {
                toggle[3] = (byte)input;
                send(toggle);
            }
        }
        userInput.close();
    }
}