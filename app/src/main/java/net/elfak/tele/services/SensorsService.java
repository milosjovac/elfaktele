package net.elfak.tele.services;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import net.elfak.tele.R;
import net.elfak.tele.data.GlobalBank;
import net.elfak.tele.data.preferences.PrefManager;
import net.elfak.tele.data.presenter.SimplePresenterObserver;
import net.elfak.tele.mqtt.MqttHandler;
import net.elfak.tele.mqtt.MqttSimpleListener;
import net.elfak.tele.ui.activities.ElfakActivity;
import net.elfak.tele.utils.Constants;
import net.elfak.tele.utils.Utils;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by milosjovac on 6/13/16.
 */
public class SensorsService extends Service {

    public static final String STARTFOREGROUND_ACTION = "STARTFOREGROUND_ACTION";
    public static final String MAIN_ACTION = "MAIN_ACTION";
    public static final String STOPFOREGROUND_ACTION = "STOPFOREGROUND_ACTION";
    public static final int FOREGROUND_SERVICE = 101;

    private GlobalBank gb = GlobalBank.getInstance();
    private MqttHandler mqttHandler;
    private Handler handler;
    private NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;
    BroadcastReceiver networkReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
        if (gb == null || gb.presenter == null) {
            onDestroy();
            return;
        }

        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(android.net.ConnectivityManager.CONNECTIVITY_ACTION)) {
                    NetworkInfo activeNetwork = ((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                    if (activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                        gb.presenter.onInternetConnection();
                    else
                        gb.presenter.onMqttConnectionChanged(Constants.MQTT_CONNECTION_STATUS_DISCONNECTED);
                }
            }
        };
        registerReceiver(networkReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        gb.presenter.addObserver(observer);
        gb.presenter.setServiceLive(true);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        startMQTT();

        handler = new Handler();
    }


    private void startMQTT() {
        // don't start mqtt if service is in kill process
        if (!gb.presenter.isServiceLive())
            return;

        mqttHandler = MqttHandler.getInstance(this);
        mqttHandler.setListener(mqttListener);
        mqttHandler.connect(gb.presenter.mqttServer);
    }

    @Override
    public void onDestroy() {
        gb.presenter.setServiceLive(false);
        gb.presenter.removeObserver(observer);
        mqttHandler.disconnect();
        mqttHandler.setListener(null);
        mqttHandler = null;
        mqttListener = null;
        unregisterReceiver(networkReceiver);
        networkReceiver = null;
        observer = null;
        MqttHandler.removeInstance();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null || intent.getAction().equals(STARTFOREGROUND_ACTION)) {

            Intent notificationIntent = new Intent(this, ElfakActivity.class);
            notificationIntent.setAction(MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Intent stopIntent = new Intent(this, SensorsService.class);
            stopIntent.setAction(STOPFOREGROUND_ACTION);
            PendingIntent pStopIntent = PendingIntent.getService(this, 0,
                    stopIntent, 0);


            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.elfaklogo);

            notificationBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle("MQTT servis")
                    .setTicker("SIOT Sensors service")
                    .setContentText(gb.presenter.currentConnectionStatus)
                    .setSmallIcon(R.drawable.elfaklogo)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .addAction(new android.support.v4.app.NotificationCompat.Action(R.drawable.ic_cancel_black_24dp, "Zaustavi servis", pStopIntent))
                    .setOngoing(true);
            startForeground(FOREGROUND_SERVICE, notificationBuilder.build());


        } else if (intent.getAction().equals(STOPFOREGROUND_ACTION)) {
            PrefManager.getInstance().serviceStoppedManually().put(true);
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private SimplePresenterObserver observer = new SimplePresenterObserver() {


        @Override
        public void onInternetConnection() {
            gb.presenter.onMqttConnectionChanged(Constants.MQTT_CONNECTION_STATUS_CONNECTING);
            updateNotification(Constants.MQTT_CONNECTION_STATUS_CONNECTING);
            startMQTT();
        }
    };


    private MqttSimpleListener mqttListener = new MqttSimpleListener() {
        @Override
        public void onMqttClientConnected() {
            gb.presenter.onMqttConnectionChanged(Constants.MQTT_CONNECTION_STATUS_CONNECTED);
            updateNotification(Constants.MQTT_CONNECTION_STATUS_CONNECTED);
            mqttHandler.subscribe(Constants.topic, 0);
        }

        @Override
        public void onMqttClientDisconnected(boolean disconnectedByUser) {
            handler.removeCallbacks(null);
            gb.presenter.onMqttConnectionChanged(Constants.MQTT_CONNECTION_STATUS_DISCONNECTED);
            updateNotification(Constants.MQTT_CONNECTION_STATUS_DISCONNECTED);
            if (!disconnectedByUser && Utils.isNetworkAvailable(SensorsService.this))
                startMQTT();
        }

        @Override
        public void onMqttClientConnectError(Throwable exception) {
            boolean network = Utils.isNetworkAvailable(SensorsService.this);
            gb.presenter.onMqttConnectionChanged(network ? Constants.MQTT_CONNECTION_STATUS_ERROR : Constants.MQTT_CONNECTION_STATUS_DISCONNECTED);
            updateNotification(network ? Constants.MQTT_CONNECTION_STATUS_ERROR : Constants.MQTT_CONNECTION_STATUS_DISCONNECTED);
        }

        @Override
        public void onMqttClientAlreadyConnected() {
            gb.presenter.onMqttConnectionChanged(Constants.MQTT_CONNECTION_STATUS_CONNECTED);
            updateNotification(Constants.MQTT_CONNECTION_STATUS_CONNECTED);
        }

        @Override
        public void onMessageArrived(String topic, MqttMessage mqttMessage) {
            if (mqttMessage.isDuplicate()) {
                return;
            }
            gb.presenter.onSensorValueChanged(mqttMessage.toString());
        }
    };

    private void updateNotification(String status) {
        if (notificationBuilder != null) {
            notificationBuilder.setContentText(status);
            notificationManager.notify(FOREGROUND_SERVICE, notificationBuilder.build());
        }
    }
}
