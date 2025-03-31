package org.openremote.modbus.db;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class devices {
    public static JSONArray devices;
    public static String path = "db/devices.json";

    public static String file = """
[
{
"id": 1,
"name": "RIO.A1",
"link": "tcp",
"address": "192.168.0.10",
"registers": [
{
"type": "coil",
"number": 1,
"refresh": 5,
"state": false,
"updated": 1742901744
},
{
"type": "register",
"number": 1,
"refresh": 2,
"state": 12,
"updated": 1742901746
}
]
},
{
"id": 2,
"name": "RIO.A2",
"link": "rtu",
"unit": 1,
"registers": [
{
"type": "register",
"number": 1,
"refresh": null,
"state": 8,
"updated": 1742901323
},
{
"type": "holder",
"number": 1,
"refresh": 2,
"state": 4,
"updated": 1742901747
}
]
}
]
""";

    public static JSONArray init() {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(file);
            devices = (JSONArray) obj;
            return devices;
        }
        catch (Exception e) {
            System.out.println("Error in communication: " + e.getMessage());
            return null;
        }
    }

    public static JSONArray get() {
        return devices;
    }

    public static JSONArray update(JSONArray attr) {
        try {
            new FileWriter(path).write(devices.toJSONString());
        }
        catch (IOException e) {
            return null;
        }
    }
}