import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.openremote.modbus.connector.socket;
import org.json.*;

public class mqtt {
    public static void messageHandler(String message) {
        JSONObject messageObject = new JSONObject(message);

        JSONObject ref = messageObject.getJSONObject("ref");
        String id = ref.getString("id");
        String name = ref.getString("name");
        boolean value = messageObject.getBoolean("value");
        if (value) {
            socket.writecoil("10.0.0.103", 502, "tcp", 0, 65280);
        } else {
            socket.writecoil("10.0.0.103", 502, "tcp", 0, 0);
        }
    }

    public static void main(String[] args) {
        try {
            MqttClient client = new MqttClient("tcp://localhost:1883","modbus");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("master:modbus");
            options.setPassword("soj4eUpAwnZaNOnFgCem12xCLuaRTJIb".toCharArray()); 

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost: " + cause.getMessage());
                }
            
                @Override
                public void messageArrived(String topic, MqttMessage msg) throws Exception {
                    String message = new String(msg.getPayload());
                    System.out.println("\tMessage: " + message);
                    messageHandler(message);
                }
            
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            client.connect(options);
            client.subscribe("master/modbus/attribute/onOff/4Lcxrda1jLZ0VWCld5xbI7", 1);

            boolean oldInput = socket.readinput("10.0.0.103", 502, "tcp", 0);
            boolean input = oldInput;
            boolean run = true;
            while(run == true) {
                input = socket.readinput("10.0.0.103", 502, "tcp", 0);
                if (input != oldInput) {
                    String inputString = String.valueOf(input);
                    MqttMessage msg = new MqttMessage(inputString.getBytes());
                    msg.setQos(0);
                    msg.setRetained(true);
                    client.publish("master/modbus/writeattributevalue/DI1/4Lcxrda1jLZ0VWCld5xbI7",msg);
                }
                oldInput = input;
                Thread.sleep(100);
            }

            Thread.sleep(Long.MAX_VALUE);
            client.close();

        } catch(MqttException me) {
            System.out.println(me);
            me.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Application interrupted.");
        }
    }
}
