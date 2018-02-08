package org.iot.ste;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import android.content.Context;
import android.util.Log;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;


public class Listener implements OnMenuItemClickListener {


    private String clientHandle = null;
    private MainActivity mainActivity = null;
    private Context context = null;


    public Listener(MainActivity mainActivity, String clientHandle) {
        this.mainActivity = mainActivity;
        this.clientHandle = clientHandle;
        context = mainActivity;

    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {


        return false;
    }


    public void reconnect() {

        Connections.getInstance(context).getConnection(clientHandle).changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);

        Connection c = Connections.getInstance(context).getConnection(clientHandle);
        try {
            c.getClient().connect(c.getConnectionOptions(), null, new ActionListener(context, ActionListener.Action.CONNECT, clientHandle, null));
        } catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to reconnect the client with the handle " + clientHandle, e);

        } catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to reconnect the client with the handle " + clientHandle, e);

        }

    }


    public void disconnect() {

        Connection c = Connections.getInstance(context).getConnection(clientHandle);

        if (!c.isConnected()) {
            return;
        }

        try {
            c.getClient().disconnect(null, new ActionListener(context, ActionListener.Action.DISCONNECT, clientHandle, null));
            c.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTING);
        } catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to disconnect the client with the handle " + clientHandle, e);

        }

    }


    public void silentSubscribe(String topic) {

        int qos = ActivityConstants.defaultQos;

        try {
            String[] topics = new String[1];
            topics[0] = topic;
            Connections.getInstance(context).getConnection(clientHandle).getClient()
                    .subscribe(topic, qos, null, new ActionListener(context, ActionListener.Action.SUBSCRIBE, clientHandle, topics));
        } catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + topic + " the client with the handle " + clientHandle, e);
        } catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + topic + " the client with the handle " + clientHandle, e);
        }
    }


    public void unSubscribe(String topic) {
        try {
            Connections.getInstance(context).getConnection(clientHandle).getClient()
                    .unsubscribe(topic);
        } catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + topic + " the client with the handle " + clientHandle, e);
        } catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + topic + " the client with the handle " + clientHandle, e);
        }
    }


    public void silentSendTopic(String topic, String message) {


        int qos = 0;
        boolean retained = true;

        String[] args = new String[2];
        args[0] = message;
        args[1] = topic + ";qos:" + qos + ";retained:" + retained;

        try {
            Connections.getInstance(context).getConnection(clientHandle).getClient()
                    .publish(topic, message.getBytes(), qos, retained, null, new ActionListener(context, ActionListener.Action.PUBLISH, clientHandle, args));
        } catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        } catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }

    }


}
