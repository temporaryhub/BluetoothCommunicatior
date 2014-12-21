package com.example.marcinbuczkowski.bluetoothchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.*;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothServerSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class BluetoothServerThread extends Thread {
    private BluetoothServerSocket btSocket;
    private BluetoothAdapter btAdapter;

    private Object services;


    public BluetoothServerThread(String name, UUID id,Object services) {
        BluetoothServerSocket tmp = null;
        this.btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.services=services;

        try {
            tmp = this.btAdapter.listenUsingRfcommWithServiceRecord(name, id);
        } catch (IOException e) {
            this.btAdapter.getAddress();
            //todo exit from app with information about error
        }
        this.btSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                socket = this.btSocket.accept();
            } catch (Exception e) {
                break;
            }

            if (socket != null) {
                this.manageReceivedMessage(socket);
                /*try {
                    this.btSocket.close();
                } catch (IOException e) {
                }*/
            }
            //break;
        }
    }

    /**
     * Manages received message. Checks headers and sends the message contents to appropriate method.
     *
     * Message has a form of:
     *
     * SENDER: device_address
     * RECEIVER: device_address
     * TYPE: message_type
     * message_content
     *
     * If sender or receiver address doesn't match the message won't be accepted. Content is
     * processed according to its type. Message content must end with a newline sign, otherwise
     * the last line won't be processed.
     *
     * @param socket
     */
    private void manageReceivedMessage(BluetoothSocket socket) {
        InputStream is = null;

        try {
            is = socket.getInputStream();
        } catch (IOException e) {
            return;
        }

        BluetoothDevice sender = socket.getRemoteDevice();
        String message = null;

        try {
            message = this.inputStreamToString(is);
        } catch (Exception e) {
            return;
        }

        String[] parsedMessage = this.messageParse(message);

        if (!parsedMessage[0].equals(sender.getAddress()) ||
            !parsedMessage[1].equals(this.btAdapter.getAddress())) {
            this.btAdapter.getAddress();
            return;
        }

        if (parsedMessage[2].equals("chat")) {

            this.saveChatMessage(parsedMessage[0], parsedMessage[1], parsedMessage[3]);
        }
        else if(parsedMessage[2].equals("pos"))
        {
            this.saveDistance(parsedMessage[3]);
        }
        else if(parsedMessage[2].equals("inf"))
        {
            this.savePosition(parsedMessage[0],parsedMessage[3]);
        }
    }

    private void saveDistance(String dis)
    {
        try{
            SettingsActivity.loc=dis;
        }
        catch (Exception e){}
    }

    private void savePosition(String sender, String position)
    {
        //Najpierw sprawdzić czy device 1 nie jest puste
        try{
            if(SettingsActivity.device1 != null) {
                String message = String.valueOf(SettingsActivity.device1.getLongitude()) + "\n" + String.valueOf(SettingsActivity.device1.getLatitude());
                BluetoothClientThread btc = new BluetoothClientThread();
                BluetoothDevice bd = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(sender);
                //todo replace UUID as described in MainActivity
                btc.connect(bd, UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d"));
                btc.sendPosition(message,sender);
                //SettingsActivity.loc = position;
            }

        }
        catch (Exception e){}
    }

    private void saveChatMessage(String sender, String receiver, String message) {
        try {
            ChatMessageDatabase cmd = ChatMessageDatabase.getInstance(null);
            cmd.save(sender, receiver, message);
        } catch (Exception e) { }
    }

    private String[] messageParse(String message) {
        String[] lines = message.split(System.getProperty("line.separator"));

        String messageSender = lines[0];
        messageSender = messageSender.substring(8);

        String messageReceiver = lines[1];
        messageReceiver = messageReceiver.substring(10);

        String messageType = lines[2];
        messageType = messageType.substring(6);

        String messageContent = "";

        for (int i = 3; i < lines.length; i++) {
            messageContent += lines[i];
            if (i != (lines.length - 1)) {
                messageContent += System.getProperty("line.separator");
            }
        }

        return new String[] {messageSender.trim(), messageReceiver.trim(), messageType.trim(), messageContent.trim()};
    }

    private String inputStreamToString(InputStream is) throws UnsupportedEncodingException, IOException {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        StringBuilder inputStringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line = bufferedReader.readLine();
        while(line != null) {
            inputStringBuilder.append(line);
            inputStringBuilder.append('\n');
            try {
                line = bufferedReader.readLine();
            } catch (IOException e) {
                line = null;
            }
        }
        return inputStringBuilder.toString();
    }
}
