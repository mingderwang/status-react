package com.statusim.geth.service;


import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

public class GethConnector extends ServiceConnector {

    private static final String TAG = "GethConnector";

    public static final String CALLBACK_IDENTIFIER = "callbackIdentifier";

    public GethConnector(Context context, Class serviceClass) {

        super(context, serviceClass);
    }

    public void startNode(String callbackIdentifier) {

        if (checkBound()) {
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_START_NODE, null);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(startNode) to service: ", e);
            }
        }
    }

    public void stopNode(String callbackIdentifier) {

        if (checkBound()) {
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_STOP_NODE, null);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(stopNode) to service: ", e);
            }
        }
    }

    public void login(String callbackIdentifier, String address, String password) {

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("address", address);
            data.putString("password", password);
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_LOGIN, data);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(unlockAccount) to service: ", e);
            }
        }
    }

    public void createAccount(String callbackIdentifier, String password) {

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("password", password);
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_CREATE_ACCOUNT, data);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(createAccount) to service: ", e);
            }
        }
    }

    public void addAccount(String callbackIdentifier, String privateKey) {

        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("privateKey", privateKey);
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_ADD_ACCOUNT, data);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(addAccount) to service: ", e);
            }
        }
    }

    public void addWhisperFilter(String callbackIdentifier, String filter) {
        if (checkBound()) {
            Bundle data = new Bundle();
            data.putString("filter", filter);
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_ADD_WHISPER_FILTER, data);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(addAccount) to service: ", e);
            }
        }
    }

    public void removeWhisperFilter(String callbackIdentifier, int idFilter) {
        if (checkBound()) {
            Bundle data = new Bundle();
            data.putInt("idFilter", idFilter);
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_REMOVE_WHISPER_FILTER, data);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(addAccount) to service: ", e);
            }
        }
    }

    public void clearWhisperFilters(String callbackIdentifier) {
        if (checkBound()) {
            Message msg = createMessage(callbackIdentifier, GethMessages.MSG_CLEAR_WHISPER_FILTERS, null);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Exception sending message(addAccount) to service: ", e);
            }
        }
    }


    protected boolean checkBound() {

        if (!isBound) {
            Log.d(TAG, "GethConnector not bound!");
            return false;
        }
        return true;
    }

    protected Message createMessage(String callbackIdentifier, int idMessage, Bundle data) {

        Log.d(TAG, "Client messenger: " + clientMessenger.toString());
        Message msg = Message.obtain(null, idMessage, 0, 0);
        msg.replyTo = clientMessenger;
        if (data == null) {
            data = new Bundle();
        }
        data.putString(CALLBACK_IDENTIFIER, callbackIdentifier);
        msg.setData(data);
        return msg;
    }
}
