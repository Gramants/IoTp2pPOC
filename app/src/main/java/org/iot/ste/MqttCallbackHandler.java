package org.iot.ste;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;


public class MqttCallbackHandler implements MqttCallback {


    private Context context;
    private String clientHandle;

    public MqttCallbackHandler(Context context, String clientHandle) {
        this.context = context;
        this.clientHandle = clientHandle;
    }


    @Override
    public void connectionLost(Throwable cause) {

        if (cause != null) {
            Connection c = Connections.getInstance(context).getConnection(clientHandle);
            c.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTED);
            Intent intent = new Intent(ActivityConstants.INTENT_FILTER);
            intent.putExtra(ActivityConstants.STATUS_KEY, ActivityConstants.STATUS_LOST);
            intent.putExtra(ActivityConstants.PAYLOAD_KEY, ActivityConstants.STATUS_LOST);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }


    }


    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Connection c = Connections.getInstance(context).getConnection(clientHandle);
        Intent intent = new Intent(ActivityConstants.INTENT_FILTER);
        intent.putExtra(ActivityConstants.STATUS_KEY, ActivityConstants.STATUS_ARRIVED);
        intent.putExtra(ActivityConstants.PAYLOAD_KEY, new String(message.getPayload()).toString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Do nothing
    }

}
