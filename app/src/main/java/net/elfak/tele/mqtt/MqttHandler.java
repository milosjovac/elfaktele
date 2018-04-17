package net.elfak.tele.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.UUID;

/**
 * Created by milosjovac on 6/23/16.
 */
public class MqttHandler implements MqttCallback {

    private static MqttHandler INSTANCE;

    private MqttAndroidClient client;
    private Context context;
    private MqttListener listener;


    public interface MqttRepeatActionCallback {
        void repeatAction(boolean repeat);
    }

    private MqttHandler(Context context) {
        this.context = context;
    }

    public static MqttHandler getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new MqttHandler(context);
        }

        return INSTANCE;
    }

    public static void removeInstance() {
        INSTANCE = null;
    }

    public void setListener(MqttListener listener) {
        this.listener = listener;
    }

    public void connect(String serverHost) {
        connect(serverHost, MqttConstants.PORT);
    }

    public void connect(final String serverHost, final int port) {
        // check if client is already connected
        if (!isMqttConnected()) {
            String connectionUri = "tcp://" + serverHost + ":" + port;
            //String connectionUri = serverHost + ":" + port;
            if (client == null)
                client = new MqttAndroidClient(context, connectionUri, "Mqtt - " + UUID.randomUUID().toString());//clientId

            client.setCallback(this);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setMaxInflight(300);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(10);

            try {
                // connect
                client.connect(options, context, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        if (listener != null) listener.onMqttClientConnected();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        if (listener != null) listener.onMqttClientConnectError(exception);
                        exception.printStackTrace();
                    }
                });
            } catch (MqttException e) {
                if (listener != null) listener.onMqttClientConnectError(e);
            }
        } else {
            if (listener != null) listener.onMqttClientAlreadyConnected();
        }
    }

    /**
     * Disconnect MqttAndroidClient from the MQTT server
     */
    public void disconnect() {
        // check if client is actually connected
        if (isMqttConnected()) {
            try {
                // disconnect
                client.unregisterResources();
                client.disconnect(context, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        if (listener != null) listener.onMqttClientDisconnected(true);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        if (listener != null) listener.onMqttClientDisconnectError(exception);
                    }
                });
            } catch (MqttException e) {
                if (listener != null) listener.onMqttClientDisconnectError(e);
            }
        }
    }

    /**
     * Subscribe MqttAndroidClient to a topic
     *
     * @param topic to subscribe to
     * @param qos   to subscribe with
     */
    public void subscribe(final String topic, final int qos) {
        // check if client is connected
        if (isMqttConnected()) {
            try {
                // create ActionListener to handle subscription results
                client.subscribe(topic, qos, context, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        if (listener != null) listener.onSubscribeSuccess();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        if (listener != null) listener.onSubscribeError(exception);
                    }
                });
            } catch (MqttException e) {
            }
        } else {
            if (listener != null) listener.onMqttClientNotConnected(new MqttRepeatActionCallback() {
                @Override
                public void repeatAction(boolean repeat) {
                    subscribe(topic, qos);
                }
            });
        }
    }

    /**
     * Unsubscribe MqttAndroidClient from a topic
     *
     * @param topic to unsubscribe from
     */
    public void unsubscribe(final String topic) {
        // check if client is connected
        if (isMqttConnected()) {
            try {
                // create ActionListener to handle unsubscription results
                client.unsubscribe(topic, context, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        if (listener != null) listener.onUnsubscribeSuccess();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        if (listener != null) listener.onUnsubscribeError(exception);
                    }
                });
            } catch (MqttException e) {
                if (listener != null) listener.onUnsubscribeError(e);
            }
        } else {
            if (listener != null) listener.onMqttClientNotConnected(new MqttRepeatActionCallback() {
                @Override
                public void repeatAction(boolean repeat) {
                    unsubscribe(topic);
                }
            });
        }
    }

    /**
     * Publish message to a topic
     *
     * @param topic    to publish the message to
     * @param message  JSON object representation as a string
     * @param retained true if retained flag is requred
     * @param qos      quality of service (0, 1, 2)
     *                 At most once (0)
     *                 At least once (1)
     *                 Exactly once (2).
     */
    public void publish(final String topic, final String message, final boolean retained, final int qos, final boolean shouldAskForReinit) {

        // check if client is connected
        if (isMqttConnected()) {
            // create a new MqttMessage from the message string
            final MqttMessage mqttMsg = new MqttMessage(message.getBytes());
            // set retained flag
            mqttMsg.setRetained(retained);
            // set quality of service
            mqttMsg.setQos(qos);
            try {
                // create ActionListener to handle message published results
                client.publish(topic, mqttMsg, context, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        if (listener != null) listener.onPublishSuccess();
                        Log.i("MQTT", "Topic :" + topic + ", msg: " + message);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        if (listener != null) listener.onPublishError(exception);
                        Log.e("MQTT", "NotSent - topic: " + topic + ", msg: " + message + ", exc: " + exception.getMessage());
                        exception.printStackTrace();
                    }
                });
            } catch (Exception e) {
                if (listener != null) listener.onPublishError(e);
            }
        } else {
            if (shouldAskForReinit)
                if (listener != null)
                    listener.onMqttClientNotConnected(new MqttRepeatActionCallback() {
                        @Override
                        public void repeatAction(boolean repeat) {
                            publish(topic, message, retained, qos, shouldAskForReinit);
                        }
                    });
        }
    }

    /**
     * Process incoming messages to the MQTT client.
     *
     * @param topic       The topic the message was received on.
     * @param mqttMessage The message that was received
     * @throws Exception Exception that is thrown if the message is to be rejected.
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        if (listener != null) listener.onMessageArrived(topic, mqttMessage);
    }

    /**
     * Handle loss of connection from the MQTT server.
     *
     * @param throwable
     */
    @Override
    public void connectionLost(Throwable throwable) {
        if (listener != null) listener.onMqttClientDisconnected(false);
    }

    /**
     * Handle notification that message delivery completed successfully.
     *
     * @param iMqttDeliveryToken The token corresponding to the message which was delivered.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        if (listener != null) listener.onDeliveryComplete(iMqttDeliveryToken);
    }


    /**
     * Checks if the MQTT client has an active connection
     *
     * @return True if client is connected, false if not.
     */
    public boolean isMqttConnected() {
        boolean connected = false;
        try {
            if ((client != null) && (client.isConnected()))
                connected = true;
        } catch (Exception e) {
            // swallowing the exception as it means the client is not connected
        }
        return connected;
    }
}
