package com.example.configurationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;  // Initilisation de la bibliothèque de felhr (appareil)
import com.felhr.usbserial.UsbSerialInterface; // Initialisation de la bibliothèque de felhr (interface)

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
/** La MainActivity sert à créer une connection entre une arduino et un appareil android grâce à l'utilisation de la bibliothèque de felHR
*@author Kadioglu & Cheng
*@version 1.0
*/

public class MainActivity extends AppCompatActivity {

    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION"; // Commande permettant de demander la permission deconnection à l'appareil android lors du brachement

    UsbDevice device; // Initialisation de l'appareil
    UsbManager usbManager; // Initialisation du management de l'USB
    UsbSerialDevice serialPort; // Initialisation du port USB
    UsbDeviceConnection connection; // Initialisation de la connexion
    Button CntBtn, DcntBtn; // Initialisation du bouton de connexion et de déconnexion
    Switch Sw; // Initialisation du bouton switch


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Initlisation du layout pour pouvoir l'utiliser dans le MainActivity


        usbManager = (UsbManager)getSystemService(USB_SERVICE); // Attribution de variable pour le système de demande de la connexion USB
        CntBtn = (Button) findViewById(R.id.button1); //Attribution de variable pour lier le bouton connexion du layout au MainActivity
        DcntBtn = (Button) findViewById(R.id.button2);//Attribution de variable pour lier le bouton déconnexion du layout au MainActivity
        Sw = (Switch)findViewById(R.id.switch2); //Attribution de variable pour lier le bouton switch du layout au MainActivity

        IntentFilter filter = new IntentFilter(); //Initialisation du Filtre 
        filter.addAction(ACTION_USB_PERMISSION); // Filtrer pour pouvoir demander la permission de connexion entre les deux appareils
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED); // Filtre les appareils lors de la connexion de l'apapreil
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED); // Filtre les appareils lors de la déconnexion de l'apapreil

        registerReceiver(broadcastReceiver,filter); // Filtre de la liaison de la connexion
    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // Broadcast Receiver to automatically
        // start and stop the serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)){ // Liaison de la connexion pour pouvoir demander la permission de connexion
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED); // Savoir si la connexion est établie ou non 
                if (granted){ // Si la connexion est établie 
                    connection = usbManager.openDevice(device); // Accéder à l'appareil
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device,connection); // Accéder à la connexion et la confirmer 
                    if (serialPort != null){
                        if (serialPort.open()){ // Set serial conection parameters
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8); //Paramètrer les data envoyés par les deux appareils en Bits
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1); //Paramètrer les data reçus par les deux appareils en Bits
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE); // Set la Parité
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF); // Controler la connexion 
                            serialPort.read(mCallBack);
                            Toast.makeText(context, "Serial connection Opened!", Toast.LENGTH_SHORT).show(); // Message indiquant que l'accès a l'arduino est bien réalisée 
                        }else {
                            Log.d("SERIAL", "PORT NOT OPEN"); // Message indiquant que le port n'est pas ouvert 
                            Toast.makeText(context, "PORT NOT OPEN", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Log.d("SERIAL", "PORT IS NULL"); // Message indiquant que le port n'est pas indiqué 
                        Toast.makeText(context, "PORT IS NULL", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Log.d("SERIAL", "PERM NOT GRANTED"); // Message indiquant que la permission de se connecté à été refusée 
                    Toast.makeText(context, "PERM NOT GRANTED", Toast.LENGTH_SHORT).show();
                }
            }else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
                onCLickConnect(CntBtn);
            }else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
                onClickDisconnect(DcntBtn);
            }
        };
    };


    UsbSerialInterface.UsbReadCallback mCallBack = new UsbSerialInterface.UsbReadCallback(){
        // DEfining CallBack which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0){ // Permet de transformer les data reçus en Byte en une chaine de charactère compréhensible 
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("\n");
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }
    };




    public void onClickDisconnect(View view) {
        serialPort.close(); // Permet de fermer la connexion
        Toast.makeText(getApplicationContext(), "Serial connection Opened!", Toast.LENGTH_SHORT).show();
    }

    public void onCLickConnect(View view) {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList(); //Permet de recevoir la liste des appareils autorisés/compatible 
        if (!usbDevices.isEmpty()){ 
            boolean keep = true;
            for (Map.Entry<String, UsbDevice>entry:usbDevices.entrySet()){
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if(deviceVID == 0x2341)// Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this,0, new Intent(ACTION_USB_PERMISSION),0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                }else{
                    connection = null;
                    device = null;
                }
                if (!keep)
                    break;
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Nope", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickedSwitch(View view)
    {
        if (view.getId() == R.id.switch2){ // Recevoir l'information du layout pour le paramètre du switch
            if (Sw.isChecked()){
                String string ="L";
                serialPort.write(string.getBytes());
               if(serialPort.read(mCallBack) == 45)
               {
                   Toast.makeText(getApplicationContext(), "Led OPEN", Toast.LENGTH_SHORT).show();
               }
            }
            else{
                String string ="O";
                serialPort.write(string.getBytes());
            }
        }
    }
}
