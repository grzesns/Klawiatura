package com.example.klawiatura;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MyInputMethodService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private int version = 0;
    KeyboardView keyboardView;
    Keyboard keyboard;
    BluetoothDevice[] devices;

    @Override
    public View onCreateInputView() {
        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboard = new Keyboard(this, R.xml.number_pad);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);
        return keyboardView;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        final MediaPlayer mp2 = MediaPlayer.create(this, R.raw.stapler_374581609);
        mp2.start();

        KeyboardView keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        switch (primaryCode) {
            case 1:
                if (version == 2){
                   String textBlue = "Hello I'm custom bluetooth keyboard";
                   sendFile(textBlue);
                }else
                   enterTextOrCheckNFC(ic);
                break;
            case 2:
                if (version == 2){
                    String textBlue = "616000010000123";
                    sendFile(textBlue);
                }else
                   playSoundOrSetNFC(keyboardView);
                break;
            case 3:
                if (version == 2){
                    String textBlue = "616000010000124";
                    sendFile(textBlue);
                }else
                startCameraOrStartBrowser(keyboardView);
                break;
            case 4:
                saveToFileOrDelete(ic);
                break;
            case 5:
                showToast(keyboardView);
                break;
            case 6:
                changeVersion(keyboardView);
                break;
            default:
        }
    }

    @Override
    public void onPress(int primaryCode) { }

    @Override
    public void onRelease(int primaryCode) { }

    @Override
    public void onText(CharSequence text) { }

    @Override
    public void swipeLeft() { }

    @Override
    public void swipeRight() { }

    @Override
    public void swipeDown() { }

    @Override
    public void swipeUp() { }

    public void enterTextOrCheckNFC(InputConnection ic) {

        if (version == 0) {
            ic.commitText("Hello World!", 1);
        }else {
            Context context = getApplicationContext();

            NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
            NfcAdapter adapter = manager.getDefaultAdapter();
            if (adapter != null && adapter.isEnabled()) {
                CharSequence text2 = "NFC włączony!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text2, duration);
                toast.show();
            }else{
                CharSequence text2 = "NFC wyłączony lub niedostępny!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text2, duration);
                toast.show();
            }
        }
    }

    public void playSoundOrSetNFC(View view) {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.zapsplat_animals_cat_kitten_meow_004_30180);
        if(version == 0){
            mp.start();
        }else{
            Context context = getApplicationContext();

            NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
            NfcAdapter adapter = manager.getDefaultAdapter();
            if (adapter != null && adapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Wyłącz NFC!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }else if (adapter != null){
                Toast.makeText(getApplicationContext(), "Włącz NFC!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    public void startCameraOrStartBrowser(View view) {
        if(version == 0){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else{
            String url = "http://www.google.pl";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

    public void saveToFileOrDelete(InputConnection ic) {
        if (version == 0){
            File file = new File(this.getExternalFilesDir(null),"TestFile.txt");
            try {
                if (!file.exists())
                    file.createNewFile();
                String text = ic.getSelectedText(0).toString();
                if (!(TextUtils.isEmpty(text))) {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(text.getBytes());
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (version == 1){
            String text = ic.getSelectedText(0).toString();
            if (TextUtils.isEmpty(text)){
                ic.deleteSurroundingText(1, 0);
            }else
               ic.commitText("", 1);
        }else{
            InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
            imeManager.showInputMethodPicker();
        }
    }

    public void showToast(View view) {

        if (version == 0){
            Context context = getApplicationContext();
            CharSequence text = "Jestem Toast'em!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }else if(version == 1) {
            //Ustaw połączenie Bluetooth
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Context context = getApplicationContext();
                CharSequence text = "Bluetooth nie dostępny!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else{
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(enableBtIntent);
                }
            }
            if(bluetoothAdapter.isEnabled()){
                version = 2;
                keyboard = new Keyboard (this, R.xml.number_pad3);
                keyboardView.setKeyboard(keyboard);
                keyboardView.invalidateAllKeys();

                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                devices = new BluetoothDevice[pairedDevices.size()];
                pairedDevices.toArray(devices);
                /*
                String deviceName = "";
                String deviceHardwareAddress = "";
                if (devices.length > 0) {
                    for (int i = 0; i<devices.length; i++) {
                        deviceName = devices[i].getName();
                        deviceHardwareAddress = devices[i].getAddress(); // MAC address
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file_to_transfer) );
                        startActivity(intent);
                    }
                }
                 */
            }

            //
        }else{
            version = 0;
            keyboard = new Keyboard(this, R.xml.number_pad);
            keyboardView.setKeyboard(keyboard);
            keyboardView.invalidateAllKeys();
        }

    }

    public void changeVersion(View view) {

        if(version == 0) {
            version = 1;
            keyboard = new Keyboard(this, R.xml.number_pad2);
        }else {
            version = 0;
            keyboard = new Keyboard(this, R.xml.number_pad);
        }
        keyboardView.setKeyboard(keyboard);
        keyboardView.invalidateAllKeys();
    }

    public void sendFile(String text){
        File file = new File("BluetoothMessage.txt");
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(text.getBytes());
            stream.close();
        } catch (IOException e) {

        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file) );

        PackageManager pm = getPackageManager();
        List<ResolveInfo> appsList = pm.queryIntentActivities( intent, 0);

        if(appsList.size() > 0)
        {
            String packageName = null;
            String className = null;
            boolean found = false;

            for(ResolveInfo info: appsList){
                packageName = info.activityInfo.packageName;
                if( packageName.equals("com.android.bluetooth")){
                    className = info.activityInfo.name;
                    found = true;
                    break;// found
                }
            }
            if(! found){
                Toast.makeText(this, "Bluetooth not found!",
                        Toast.LENGTH_SHORT).show();
                // exit
            }else{
                intent.setClassName(packageName, className);
                startActivity(intent);
            }

        }
    }
}
