package net.elfak.tele.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;


/**
 * Created by milosjovac on 6/23/16.
 */
public interface MqttListener {

    void onDeliveryComplete(IMqttDeliveryToken iMqttDeliveryToken);

    void onMessageArrived(String topic, MqttMessage mqttMessage);

    void onMqttClientNotConnected(MqttHandler.MqttRepeatActionCallback mqttRepeatActionCallback);

    void onPublishError(Throwable exception);

    void onPublishSuccess();

    void onUnsubscribeError(Throwable exception);

    void onUnsubscribeSuccess();

    void onSubscribeError(Throwable exception);

    void onSubscribeSuccess();

    void onMqttClientDisconnectError(Throwable exception);

    void onMqttClientDisconnected(boolean disconnectedByUser);

    void onMqttClientConnectError(Throwable exception);

    void onMqttClientConnected();

    void onMqttClientAlreadyConnected();
}
