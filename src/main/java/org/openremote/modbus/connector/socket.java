package org.openremote.modbus.connector;

import java.io.OutputStream;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;
import org.openremote.DeviceDataController;

import org.openremote.modbus.DeviceDataController;

import java.util.Arrays;

public class socket {
    public static Socket socket;
    public static OutputStream outputStream;
    public static InputStream inputStream;
    public static String protocol;

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

    public static void connect(String ipaddress, int port, String proto) {
        try {
            socket = new Socket(ipaddress, port);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            protocol = proto;
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static byte[] send(byte[] packet) {
        try {
            outputStream.write(packet);

            outputStream.flush();

            byte[] responseBuffer = new byte[12];
            inputStream.read(responseBuffer);

            return responseBuffer;
        }
        catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static boolean writecoil(int address, int value) {
        if (protocol == "tcp") {
            byte[] packet = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x01, (byte)0x05, (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x00};
            packet[9] = (byte)address;
            packet[10] = (byte)(value / 256);
            packet[11] = (byte)(value % 256);

            byte[] response = send(packet);

            if (Arrays.equals(packet, response)) {
                return true;
            }

            return false;
        }
        else if (protocol == "rtu") {
            byte[] packet = {(byte)0x01, (byte)0x05, (byte)0x00, (byte)0x01, (byte)0x55, (byte)0x00, (byte)0x00, (byte)0x00};
            packet[3] = (byte)address;
            packet[4] = (byte)(value / 256);
            packet[5] = (byte)(value % 256);
            byte[] response = send(checksum(packet));
            // System.out.println(response);
            return true;
        }
        else {
            System.out.println("Unsuppored protocol given to socket.send");
        }
        return false;
    }

    public static boolean readcoil(int address) {
        if (protocol == "tcp") {
            byte[] packet = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x01, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01};
            packet[8] = (byte)(address / 256);
            packet[9] = (byte)(address % 256);

            byte[] response = send(packet);

            if (response[9] == 1) {
                return true;
            }

            return false;
        }
        else {
            System.out.println("Unsuppored protocol given to socket.send");
        }
        return false;
    }

    public static boolean readinput(int address) {
        if (protocol == "tcp") {
            byte[] packet = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x01, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01};
            packet[8] = (byte)(address / 256);
            packet[9] = (byte)(address % 256);

            byte[] response = send(packet);

            if (response[9] == 1) {
                return true;
            }

            return false;
        }
        else {
            System.out.println("Unsuppored protocol given to socket.send");
        }
        return false;
    }
    
    public static void main(String[] args) {
        // DeviceDataController.main(new String[0]);
        // boolean run = true;
        // Scanner userInput = new Scanner(System.in);
        // connect("10.0.0.103", 502, "tcp");
        // boolean oldInput = readinput(0);
        // while(run == true) {
        //     boolean input = readinput("10.0.0.103", 502, "tcp", 0);
        //     if (input != oldInput) {
        //         writecoil("10.0.0.103", 502, "tcp", 0, 21760);
        //     }
        //     oldInput = input;
        // }
        
    }
}