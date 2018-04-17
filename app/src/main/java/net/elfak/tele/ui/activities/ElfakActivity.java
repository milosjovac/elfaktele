package net.elfak.tele.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import net.elfak.tele.R;
import net.elfak.tele.data.GlobalBank;
import net.elfak.tele.data.presenter.PresenterObserver;
import net.elfak.tele.data.presenter.SimplePresenterObserver;
import net.elfak.tele.services.SensorsService;
import net.elfak.tele.ui.RadarView;

import java.util.HashMap;

public class ElfakActivity extends AppCompatActivity {

    private GlobalBank gb = GlobalBank.getInstance();
    private TextView tvConnectionStatus;
    private boolean firstStart = true;
    private RadarView radarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        radarView = findViewById(R.id.radar_view);
        setConnectionStatus(gb.presenter.currentConnectionStatus);
    }

    private void setConnectionStatus(String status) {
        tvConnectionStatus.setText(Html.fromHtml("Status MQTT servisa: <b>" + status + "</b>"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!gb.presenter.isServiceLive() && !firstStart)
            showServiceNotLiveDialog();
        firstStart = false;
    }


    protected void onStart() {
        gb.presenter.addObserver(observer);
        super.onStart();
    }

    protected void onStop() {
        gb.presenter.removeObserver(observer);
        super.onStop();
    }


    void showServiceNotLiveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Da li želite da startujete MQTT servis?")
                .setCancelable(false)
                .setTitle("Konekcija nije uspostavljena")
                .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        Intent serviceIntent = new Intent(ElfakActivity.this, SensorsService.class);
                        serviceIntent.setAction(SensorsService.STARTFOREGROUND_ACTION);
                        startService(serviceIntent);
                    }
                })
                .setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private PresenterObserver observer = new SimplePresenterObserver() {
        @Override
        public void onServiceStopped() {
            showServiceNotLiveDialog();
        }

        @Override
        public void onMqttConnectionChanged(String status) {
            setConnectionStatus(status);
        }

        @Override
        public void onInternetConnection() {
            Toast.makeText(ElfakActivity.this, "Konekcija uspostavljena", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSensorValueChanged(String value) {
            if (value == null || value.equals("") || !value.startsWith("{") || value.startsWith("{\"") || radarView == null) {
                return;
            }
            for (String line : value.split("\n")) {
                String[] s = line.split(",");
                if (s.length > 1) {
                    String s1 = s[0].trim();
                    String s2 = s[1].trim();
                    try {
                        final int angle = Integer.parseInt(s1.substring(1, s1.length()));
                        final int distance = Integer.parseInt(s2.substring(0, s2.length() - 1));
                        radarView.setDataPoints(angle, new HashMap() {{
                            put("distance", distance);
                            put("time", System.currentTimeMillis());
                        }});
                    } catch (NumberFormatException nfe) {
                        //skip
                    }
                }
            }
        }
    };
}
