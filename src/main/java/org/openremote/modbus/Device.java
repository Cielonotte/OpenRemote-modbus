package org.openremote.modbus;

public class Device {
    private int id;
    private String name;
    private String address;
    private String connectionLink; //TCP of RTU

    public Device(){
    }


    //Constructor
    public Device(int id, String name, String address, String connectionLink) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.connectionLink = connectionLink;
    }


    //Getters
    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getaddress(){ 
        return address;
    }

    public String getConnectionLink() {
        return connectionLink;
    }


    //Setters
    public void setId(int id) {this.id = id; }
    public void setName(String name) {this.name = name;}
    public void setAddress(String address) {this.address = address;}
    public void setConnectionLink(String connectionLink) {this.connectionLink = connectionLink;}
}