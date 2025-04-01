package org.openremote.modbus;

public class Register {
    private int id;
    private int deviceId;
    private String type;
    private int number;
    private int refreshRate;

    public Register(){
    }

    public Register(int id, int deviceId, String type, int number, int refreshRate) {
        this.id = id;
        this.deviceId = deviceId;
        this.type = type;
        this.number = number;
        this.refreshRate = refreshRate;
    }

    // Getters
    public int getId() { 
        return id; }

    public int getDeviceId() { 
        return deviceId; }

    public String getType() { 
        return type; }

    public int getNumber() { 
        return number; }
        
    public int getRefreshRate() { 
        return refreshRate; }

    // Setters
    public void setId(int id) 
    { this.id = id; }

    public void setDeviceId(int deviceId) 
    { this.deviceId = deviceId; }

    public void setType(String type) 
    { this.type = type; }

    public void setNumber(int number) 
    { this.number = number; }
    
    public void setRefreshRate(int refreshRate)
     { this.refreshRate = refreshRate; }
}