package com.example.marcinbuczkowski.bluetoothchat;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothClientThread {
    private BluetoothSocket btSocket;
    private BluetoothDevice btDevice;

    public BluetoothClientThread() {

    }

    public void connect(BluetoothDevice device, UUID serverId) {
        BluetoothSocket tmp = null;
        btDevice = device;
        try {
            tmp = device.createRfcommSocketToServiceRecord(serverId);
        } catch (IOException e) { }
        btSocket = tmp;
    }

    public void sendMessage(String message, String receiver) {
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        bta.cancelDiscovery();

        try {
            btSocket.connect();
            OutputStream os = btSocket.getOutputStream();
            byte[] byteMessage = this.messageToBuffer(message, receiver);
            os.write(byteMessage);
            btSocket.close();
        } catch (IOException connectException) {
            try {
                btSocket.close();
            } catch (IOException closeException) { }
            return;
        }

    }

    /**
     * Message format as described in BluetoothServerThread
     */
    private byte[] messageToBuffer(String content, String receiver) {
        String message = "";
        message += "SENDER: " + BluetoothAdapter.getDefaultAdapter().getAddress() + System.getProperty("line.separator");
        message += "RECEIVER: " + receiver + System.getProperty("line.separator");
        message += "TYPE: chat" + System.getProperty("line.separator");
        message += content + System.getProperty("line.separator");

        return message.getBytes(Charset.forName("UTF-8"));
    }
}
