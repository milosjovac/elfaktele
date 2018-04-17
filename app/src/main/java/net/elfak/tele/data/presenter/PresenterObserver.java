package net.elfak.tele.data.presenter;

/**
 * Created by milosjovac on 6/13/16.
 */
public interface PresenterObserver {

    void onServiceStopped();

    void onMqttConnectionChanged(String status);

    void onInternetConnection();

    void onSensorValueChanged(String value);
}
