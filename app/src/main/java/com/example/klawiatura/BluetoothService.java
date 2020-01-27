package com.example.klawiatura;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothService {

    private static final UUID MY_UUID_SECURE =
            UUID.fromString("c668b4e0-fd73-4c72-aabf-07ab2ad6cd85");
    private final String appName = "Klawiatura";
    private final BluetoothAdapter bluetoothAdapter;
    Context mContext;
    private AcceptThread acceptThread;
    private ConnectThred connectThred;
    private ConnectedThread connectedThread;
    private BluetoothDevice bluetoothDevice;
    private UUID deviceUUID;
    ProgressDialog progressDialog;


    public BluetoothService(Context mContext) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mContext = mContext;
    }

    private class AcceptThread extends Thread {
        //Lokalny server soket
        private final BluetoothServerSocket bluetoothServerSocket;

        public AcceptThread (){
            BluetoothServerSocket tmp = null;

            //Tworzenie nowego nasłuchującego soketa
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_SECURE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothServerSocket = tmp;
        }
        public  void run() {
            BluetoothSocket socket = null;

            try {
                socket = bluetoothServerSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(socket != null) {
                connected(socket, bluetoothDevice);
            }
        }

        public void cancel() {
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ConnectThred extends Thread{
        private BluetoothSocket bluetoothSocket;

        public ConnectThred(BluetoothDevice device, UUID uuid) {
            bluetoothDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;

            //Get a BluetoothSocket for a connetiotn with the given BT Device
            try {
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

            bluetoothSocket =tmp;

            //anulujemy żeby nie spowalniało połączenia
            bluetoothAdapter.cancelDiscovery();

            //Tworzenie połączenia do BT Soketa
            try {
                bluetoothSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    bluetoothSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            connected (bluetoothSocket, bluetoothDevice);
        }

        public  void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public synchronized void start() {

        if (connectThred != null) {
            connectThred.cancel();
            connectThred = null;
        }
        if( acceptThread != null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public void startClient (BluetoothDevice device, UUID uuid) {

        progressDialog = ProgressDialog.show(mContext, "łączenie bluetooth", "zaczekaj", true);
        connectThred = new ConnectThred(device,uuid);
        connectThred.start();
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread (BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            progressDialog.dismiss();

            try {
                tmpIn = bluetoothSocket.getInputStream();
                tmpOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream =tmpIn;
            outputStream=tmpOut;
        }

        public void run() {
            byte [] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                String incomingMessage = new String(buffer, 0 , bytes);
            }
        }

        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());

            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void connected(BluetoothSocket bluetoothSocket, BluetoothDevice bluetoothDevice) {
        connectedThread = new ConnectedThread(bluetoothSocket);
        connectedThread.start();
    }

    public void write(byte [] out) {
        ConnectedThread ct;

        ct = connectedThread;

        ct.write(out);
    }

}
