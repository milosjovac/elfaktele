package net.elfak.tele.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by milosjovac on 6/23/16.
 */
public class MqttSimpleListener implements MqttListener {
    @Override
    public void onDeliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    @Override
    public void onMessageArrived(String topic, MqttMessage mqttMessage) {

    }

    @Override
    public void onMqttClientNotConnected(MqttHandler.MqttRepeatActionCallback mqttRepeatActionCallback) {

    }

    @Override
    public void onPublishError(Throwable exception) {

    }

    @Override
    public void onPublishSuccess() {

    }

    @Override
    public void onUnsubscribeError(Throwable exception) {

    }

    @Override
    public void onUnsubscribeSuccess() {

    }

    @Override
    public void onSubscribeError(Throwable exception) {

    }

    @Override
    public void onSubscribeSuccess() {

    }

    @Override
    public void onMqttClientDisconnectError(Throwable exception) {

    }

    @Override
    public void onMqttClientDisconnected(boolean disconnectedByUser) {

    }

    @Override
    public void onMqttClientConnectError(Throwable exception) {

    }

    @Override
    public void onMqttClientConnected() {

    }

    @Override
    public void onMqttClientAlreadyConnected() {

    }
}
