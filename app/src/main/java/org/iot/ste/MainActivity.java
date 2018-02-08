package org.iot.ste;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private String clientHandle = null;
    private String msgArrived = "";
    private Boolean isConnected = false;
    private String topicred = ActivityConstants.topicred;
    private String topicgreen = ActivityConstants.topicgreen;
    private final MainActivity mainActivity = this;
    private Connection connection = null;
    private ChangeListener changeListener = null;
    private Listener listener;
    private EditText topic;
    private Button btnred;
    private Button btngreen;
    private Button btn;
    private TextView subs;
    private TextView info;
    private ProgressBar progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clientHandle = getIntent().getStringExtra("handle");
        listener = new Listener(this, clientHandle);
        listener.reconnect();

        setContentView(R.layout.main_activity);


        topic = (EditText) findViewById(R.id.lastWill);
        btn = (Button) findViewById(R.id.btn);
        btnred = (Button) findViewById(R.id.buttonred);
        btngreen = (Button) findViewById(R.id.buttongreen);
        subs = (TextView) findViewById(R.id.subscriptions);
        info = (TextView) findViewById(R.id.info);
        progress = (ProgressBar) findViewById(R.id.progressBar);

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (btngreen.isEnabled()) {
                    listener.silentSendTopic(topicred, topic.getText().toString());
                } else {
                    listener.silentSendTopic(topicgreen, topic.getText().toString());
                }


            }
        });

        btngreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btngreen.setEnabled(false);
                btnred.setEnabled(true);
                btn.setText("Send to Red");
                btn.setVisibility(View.VISIBLE);
                topic.setVisibility(View.VISIBLE);
                subs.setText("Waiting for a message from Red");
                subs.setVisibility(View.VISIBLE);
                info.setText("I am Green and I am waiting a message from Red");
                listener.silentSubscribe(topicred);
                listener.unSubscribe(topicgreen);


            }
        });

        btnred.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btngreen.setEnabled(true);
                btnred.setEnabled(false);
                btn.setText("Send to Green");
                btn.setVisibility(View.VISIBLE);
                topic.setVisibility(View.VISIBLE);
                subs.setVisibility(View.VISIBLE);
                subs.setText("Waiting for a message from Green");
                info.setText("I am Red and I am waiting a message from Green");
                listener.silentSubscribe(topicgreen);
                listener.unSubscribe(topicred);
            }
        });

        connection = Connections.getInstance(this).getConnection(clientHandle);
        changeListener = new ChangeListener();
        connection.registerChangeListener(changeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.removeChangeListener(null);
        connection.getClient().unregisterResources();
        listener.disconnect();
    }


    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver,
                        new IntentFilter(ActivityConstants.INTENT_FILTER));

    }


    protected void onPause() {
        super.onPause();
        //onDestroy();
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String status = intent.getStringExtra(ActivityConstants.STATUS_KEY);
            String msg = intent.getStringExtra(ActivityConstants.PAYLOAD_KEY);

            if (status.equals(ActivityConstants.STATUS_ARRIVED)) {
                if (!msgArrived.equals(msg)) {
                    msgArrived = msg;
                    showEmoji(msg);
                }
            }


        }
    };


    private void showEmoji(String msg) {
        subs.setText(msg);
    }


    private class ChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(final PropertyChangeEvent event) {

            mainActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mainActivity.invalidateOptionsMenu();
                    changeUI();
                }
            });

        }
    }

    private void changeUI() {

        boolean connected = Connections.getInstance(this)
                .getConnection(clientHandle).isConnected();

        if (isConnected != connected) {
            isConnected = connected;

            if (isConnected) {
                if (btngreen.isEnabled()) {
                    listener.silentSubscribe(topicgreen);
                    listener.unSubscribe(topicred);
                } else {
                    listener.silentSubscribe(topicred);
                    listener.unSubscribe(topicgreen);
                }
                progress.setVisibility(View.GONE);
            } else {
                listener.reconnect();
                progress.setVisibility(View.VISIBLE);


            }


        }


    }

}
