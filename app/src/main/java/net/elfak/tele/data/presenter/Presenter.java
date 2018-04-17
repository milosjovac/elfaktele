package net.elfak.tele.data.presenter;

import net.elfak.tele.data.GlobalBank;
import net.elfak.tele.data.preferences.PrefManager;
import net.elfak.tele.utils.Constants;

import java.util.ArrayList;

/**
 * Created by milosjovac on 6/13/16.
 */
public class Presenter {
    private static Presenter INSTANCE;
    private ArrayList<PresenterObserver> observers;
    private boolean serviceLive;
    public String mqttServer = "siot.net";
    public String currentConnectionStatus = Constants.MQTT_CONNECTION_STATUS_DISCONNECTED;

    private Presenter(GlobalBank gb) {
        this.observers = new ArrayList<>();
    }

    /* Call this to skip recursion call problem when calling from the constructor */
    public void init() {
    }

    public static Presenter createInstance(GlobalBank globalBank) {
        if (INSTANCE == null)
            INSTANCE = new Presenter(globalBank);

        return INSTANCE;
    }

    public void addObserver(PresenterObserver observer) {
        if (!observers.contains(observer))
            observers.add(observer);
    }

    public void removeObserver(PresenterObserver observer) {
        if (observers.contains(observer))
            observers.remove(observer);
    }

    public void setServiceLive(boolean serviceLive) {
        this.serviceLive = serviceLive;
        if (!serviceLive) {
            PrefManager.getInstance().serviceStoppedManually().put(false).commit();
            for (int i = 0; i < observers.size(); i++) {
                observers.get(i).onServiceStopped();
            }
        }
    }

    public boolean isServiceLive() {
        return serviceLive;
    }

    public void onMqttConnectionChanged(String status) {
        currentConnectionStatus = status;
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).onMqttConnectionChanged(status);
        }
    }

    public void onInternetConnection() {
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).onInternetConnection();
        }
    }

    public void onSensorValueChanged(String value) {
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).onSensorValueChanged(value);
        }
    }
}
