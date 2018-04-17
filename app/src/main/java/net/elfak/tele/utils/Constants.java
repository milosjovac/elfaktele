package net.elfak.tele.utils;

import net.elfak.tele.BuildConfig;

/**
 * Created by milosjovac on 6/13/16.
 */
public class Constants {

    public static final String APPLICATION_VERSION = BuildConfig.VERSION_NAME;

    // TYPE OF MQTT PATH FOR PUBLISHING DATA, MANIFEST, CONFIGURATION, COMANDS
    public static final String MQTT_DAT = "DAT/";
    public static final String MQTT_MNF = "MNF/";
    public static final String MQTT_STA = "STA/";
    public static final String MQTT_CNF = "CNF/";
    public static final String MQTT_CMD = "CMD/";

    // MQTT SERVICE ONLINE STATUS
    public static final String MQTT_CONNECTION_STATUS_CONNECTED = "Konektovan";
    public static final String MQTT_CONNECTION_STATUS_DISCONNECTED = "Diskonektovan";
    public static final String MQTT_CONNECTION_STATUS_CONNECTING = "Konektovanje u toku";
    public static final String MQTT_CONNECTION_STATUS_ERROR = "Konektovanje neuspešno";

    // APP CONFIG DATA
    public static String topic = "siot/DAT/EEC3-070D-AEC6-4295-AA7B-1872-78E6-12AA/130e785d-4c83-15f8-ed7a-8965f7fe18cc";
}
