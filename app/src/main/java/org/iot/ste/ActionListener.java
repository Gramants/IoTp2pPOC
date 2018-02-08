package org.iot.ste;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import android.content.Context;


class ActionListener implements IMqttActionListener {


    enum Action {
        CONNECT,
        DISCONNECT,
        SUBSCRIBE,
        NOACTION, PUBLISH
    }

    private Action action;

    private String[] additionalArgs;

    private String clientHandle;

    private Context context;


    public ActionListener(Context context, Action action,
                          String clientHandle, String... additionalArgs) {
        this.context = context;
        this.action = action;
        this.clientHandle = clientHandle;
        this.additionalArgs = additionalArgs;
    }


    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        switch (action) {
            case CONNECT:
                connect();
                break;
            case DISCONNECT:
                disconnect();
                break;
            case NOACTION:
                noaction();
                break;
            case SUBSCRIBE:
                subscribe();
                break;
            case PUBLISH:
                publish();
                break;
        }

    }

    private void noaction() {
    }

    private void subscribe() {

    }

    private void publish() {
    }

    private void disconnect() {
        Connection c = Connections.getInstance(context).getConnection(clientHandle);
        c.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTED);

    }

    private void connect() {
        Connection c = Connections.getInstance(context).getConnection(clientHandle);
        c.changeConnectionStatus(Connection.ConnectionStatus.CONNECTED);

    }

    @Override
    public void onFailure(IMqttToken token, Throwable exception) {


    }


}