package org.iot.ste;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


public class Persistence extends SQLiteOpenHelper implements BaseColumns {


    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "connections.db";
    public static final String TABLE_CONNECTIONS = "connections";
    public static final String COLUMN_HOST = "host";
    public static final String COLUMN_client_ID = "clientID";
    public static final String COLUMN_port = "port";
    public static final String COLUMN_ssl = "ssl";
    public static final String COLUMN_TIME_OUT = "timeout";
    public static final String COLUMN_KEEP_ALIVE = "keepalive";
    public static final String COLUMN_USER_NAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_CLEAN_SESSION = "cleanSession";

    public static final String COLUMN_TOPIC = "topic";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_QOS = "qos";
    public static final String COLUMN_RETAINED = "retained";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";


    private static final String SQL_CREATE_ENTRIES =

            "CREATE TABLE " + TABLE_CONNECTIONS + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_HOST + TEXT_TYPE + COMMA_SEP +
                    COLUMN_client_ID + TEXT_TYPE + COMMA_SEP +
                    COLUMN_port + INT_TYPE + COMMA_SEP +
                    COLUMN_ssl + INT_TYPE + COMMA_SEP +
                    COLUMN_TIME_OUT + INT_TYPE + COMMA_SEP +
                    COLUMN_KEEP_ALIVE + INT_TYPE + COMMA_SEP +
                    COLUMN_USER_NAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_PASSWORD + TEXT_TYPE + COMMA_SEP +
                    COLUMN_CLEAN_SESSION + INT_TYPE + COMMA_SEP +
                    COLUMN_TOPIC + TEXT_TYPE + COMMA_SEP +
                    COLUMN_MESSAGE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_QOS + INT_TYPE + COMMA_SEP +
                    COLUMN_RETAINED + " INTEGER);";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_CONNECTIONS;

    public Persistence(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void persistConnection(Connection connection) throws PersistenceException {

        MqttConnectOptions conOpts = connection.getConnectionOptions();
        MqttMessage lastWill = conOpts.getWillMessage();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_HOST, connection.getHostName());
        values.put(COLUMN_port, connection.getPort());
        values.put(COLUMN_client_ID, connection.getId());
        values.put(COLUMN_ssl, connection.isSSL());

        values.put(COLUMN_KEEP_ALIVE, conOpts.getKeepAliveInterval());
        values.put(COLUMN_TIME_OUT, conOpts.getConnectionTimeout());
        values.put(COLUMN_USER_NAME, conOpts.getUserName());
        values.put(COLUMN_TOPIC, conOpts.getWillDestination());


        char[] password = conOpts.getPassword();
        values.put(COLUMN_CLEAN_SESSION, conOpts.isCleanSession() ? 1 : 0);
        values.put(COLUMN_PASSWORD, password != null ? String.valueOf(password) : null);
        values.put(COLUMN_MESSAGE, lastWill != null ? new String(lastWill.getPayload()) : null);
        values.put(COLUMN_QOS, lastWill != null ? lastWill.getQos() : 0);

        if (lastWill == null) {
            values.put(COLUMN_RETAINED, 0);
        } else {
            values.put(COLUMN_RETAINED, lastWill.isRetained() ? 1 : 0); //convert from boolean to int
        }


        long newRowId = db.insert(TABLE_CONNECTIONS, null, values);

        db.close();

        if (newRowId == -1) {
            throw new PersistenceException("Failed to persist connection: " + connection.handle());
        } else {
            connection.assignPersistenceId(newRowId);
        }
    }


    public List<Connection> restoreConnections(Context context) throws PersistenceException {

        String[] connectionColumns = {
                COLUMN_HOST,
                COLUMN_port,
                COLUMN_client_ID,
                COLUMN_ssl,
                COLUMN_KEEP_ALIVE,
                COLUMN_CLEAN_SESSION,
                COLUMN_TIME_OUT,
                COLUMN_USER_NAME,
                COLUMN_PASSWORD,
                COLUMN_TOPIC,
                COLUMN_MESSAGE,
                COLUMN_RETAINED,
                COLUMN_QOS,
                _ID

        };


        String sort = COLUMN_HOST;

        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query(TABLE_CONNECTIONS, connectionColumns, null, null, null, null, sort);
        ArrayList<Connection> list = new ArrayList<Connection>(c.getCount());
        Connection connection = null;
        for (int i = 0; i < c.getCount(); i++) {
            if (!c.moveToNext()) {
                throw new PersistenceException("Failed restoring connection - count: " + c.getCount() + "loop iteration: " + i);
            }

            Long id = c.getLong(c.getColumnIndexOrThrow(_ID));
            String host = c.getString(c.getColumnIndexOrThrow(COLUMN_HOST));
            String clientID = c.getString(c.getColumnIndexOrThrow(COLUMN_client_ID));
            int port = c.getInt(c.getColumnIndexOrThrow(COLUMN_port));


            String username = c.getString(c.getColumnIndexOrThrow(COLUMN_USER_NAME));
            String password = c.getString(c.getColumnIndexOrThrow(COLUMN_PASSWORD));
            String topic = c.getString(c.getColumnIndexOrThrow(COLUMN_TOPIC));
            String message = c.getString(c.getColumnIndexOrThrow(COLUMN_MESSAGE));


            int qos = c.getInt(c.getColumnIndexOrThrow(COLUMN_QOS));
            int keepAlive = c.getInt(c.getColumnIndexOrThrow(COLUMN_KEEP_ALIVE));
            int timeout = c.getInt(c.getColumnIndexOrThrow(COLUMN_TIME_OUT));

            boolean cleanSession = c.getInt(c.getColumnIndexOrThrow(COLUMN_CLEAN_SESSION)) == 1 ? true : false;
            boolean retained = c.getInt(c.getColumnIndexOrThrow(COLUMN_RETAINED)) == 1 ? true : false;
            boolean ssl = c.getInt(c.getColumnIndexOrThrow(COLUMN_ssl)) == 1 ? true : false;

            MqttConnectOptions opts = new MqttConnectOptions();
            opts.setCleanSession(cleanSession);
            opts.setKeepAliveInterval(keepAlive);
            opts.setConnectionTimeout(timeout);

            opts.setPassword(password != null ? password.toCharArray() : null);
            opts.setUserName(username);

            if (topic != null) {
                opts.setWill(topic, message.getBytes(), qos, retained);
            }

            connection = Connection.createConnection(clientID, host, port, context, ssl);
            connection.addConnectionOptions(opts);
            connection.assignPersistenceId(id);
            list.add(connection);

        }

        c.close();
        db.close();
        return list;

    }


}
