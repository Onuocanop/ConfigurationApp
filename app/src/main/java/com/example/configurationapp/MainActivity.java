
    package com.example.configurationapp;

    import android.app.Activity;
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
    import android.widget.EditText;
    import android.widget.TextView;

    import com.felhr.usbserial.UsbSerialDevice; // Initilisation de la bibliothèque de felhr (appareil)
    import com.felhr.usbserial.UsbSerialInterface; // Initialisation de la bibliothèque de felhr (interface)

    import java.io.UnsupportedEncodingException;
    import java.util.HashMap;
    import java.util.Map;

    /**
     * La MainActivity sert à créer une connection entre une arduino et un appareil android grâce à l'utilisation de la bibliothèque de felHR
     *
     *@author Kadioglu & Cheng
     *@version 1.0
     */

    public class MainActivity extends Activity {
        public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";
        Button startButton, sendButton, clearButton, stopButton, motorButton, eyesButton, RightHandUButton, RightHandDButton, LeftHandUButton, LeftHandDButton, HeadUButton, HeadDButton, HeadLButton, HeadRButton; // Initialisation des boutons
        TextView textView;
        EditText editText;
        UsbManager usbManager;// Initialisation du management de l'USB
        UsbDevice device;  // Initialisation de l'appareil
        UsbSerialDevice serialPort; // Initialisation du port USB
        UsbDeviceConnection connection; // Initialisation de la connexion

        UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
            @Override
            public void onReceivedData(byte[] arg0) { // Permet de transformer les data reçus en Byte en une chaine de charactère compréhensible
                String data = null;
                try {
                    data = new String(arg0, "UTF-8");
                    data.concat("/n");
                    tvAppend(textView, data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }
        };

        private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ACTION_USB_PERMISSION)) { // Liaison de la connexion pour pouvoir demander la permission de connexion
                    boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED); // Savoir si la connexion est établie ou non
                    if (granted) { // Si la connexion est établie
                        connection = usbManager.openDevice(device); // Accéder à l'appareil
                        serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection); // Accéder à la connexion et la confirmer
                        if (serialPort != null) {
                            if (serialPort.open()) { //Set Serial Connection Parameters.
                                setUiEnabled(true);
                                serialPort.setBaudRate(9600);
                                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8); //Paramètrer les data envoyés par les deux appareils en Bits
                                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1); //Paramètrer les data reçus par les deux appareils en Bits
                                serialPort.setParity(UsbSerialInterface.PARITY_NONE); // Set la Parité
                                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF); // Controler la connexion
                                serialPort.read(mCallback);
                                tvAppend(textView,"Serial Connection Opened!\n"); // Message indiquant que l'accès a l'arduino est bien réalisée

                            } else {
                                Log.d("SERIAL", "PORT NOT OPEN"); // Message indiquant que le port n'est pas ouvert
                            }
                        } else {
                            Log.d("SERIAL", "PORT IS NULL"); // Message indiquant que le port n'est pas indiqué
                        }
                    } else {
                        Log.d("SERIAL", "PERM NOT GRANTED"); // Message indiquant que la permission de se connecté à été refusée
                    }
                } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                    onClickStart(startButton);
                } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                    onClickStop(stopButton);

                }
            }
        };






        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main); // Initlisation du layout pour pouvoir l'utiliser dans le MainActivity
            usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
            startButton = (Button) findViewById(R.id.buttonStart);
            sendButton = (Button) findViewById(R.id.buttonSend);
            clearButton = (Button) findViewById(R.id.buttonClear);
            stopButton = (Button) findViewById(R.id.buttonStop);
            motorButton = (Button) findViewById(R.id.buttonMotor);
            HeadUButton = (Button) findViewById(R.id.buttonHeadPV);
            HeadDButton = (Button) findViewById(R.id.buttonHeadMV);
            HeadRButton = (Button) findViewById(R.id.buttonHeadPH);
            HeadLButton = (Button) findViewById(R.id.buttonHeadMH);
            LeftHandUButton = (Button) findViewById(R.id.buttonBrasPG);
            LeftHandDButton = (Button) findViewById(R.id.buttonBrasMG);
            RightHandUButton = (Button) findViewById(R.id.buttonBrasPD);
            RightHandDButton = (Button) findViewById(R.id.buttonBrasMD);
            eyesButton = (Button) findViewById(R.id.buttonYeux);
            editText = (EditText) findViewById(R.id.editText);
            textView = (TextView) findViewById(R.id.textView);



            setUiEnabled(false);
            IntentFilter filter = new IntentFilter(); //Initialisation du Filtre
            filter.addAction(ACTION_USB_PERMISSION); // Filtrer pour pouvoir demander la permission de connexion entre les deux appareils
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED); // Filtre les appareils lors de la connexion de l'apapreil
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED); // Filtre les appareils lors de la déconnexion de l'apapreil
            registerReceiver(broadcastReceiver,filter); // Filtre de la liaison de la connexion
        }

        // fonction qui désactive les boutons si l'arduino n'est pas connecté
        public void setUiEnabled(boolean bool) {
            startButton.setEnabled(!bool);
            sendButton.setEnabled(bool);
            stopButton.setEnabled(bool);
            eyesButton.setEnabled(bool);
            motorButton.setEnabled(bool);
            HeadLButton.setEnabled(bool);
            HeadRButton.setEnabled(bool);
            HeadUButton.setEnabled(bool);
            HeadDButton.setEnabled(bool);
            RightHandUButton.setEnabled(bool);
            RightHandDButton.setEnabled(bool);
            LeftHandUButton.setEnabled(bool);
            LeftHandDButton.setEnabled(bool);


            textView.setEnabled(bool);

        }

        // Fonction qui permet d'ouvrir la communication entre l'appareil Android et Arduino
        public void onClickStart(View view) {
            HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList(); //Permet de recevoir la liste des appareils autorisés/compatible
            if (!usbDevices.isEmpty()) {
                boolean keep = true;
                for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                    device = entry.getValue();
                    int deviceVID = device.getVendorId();
                    if (deviceVID == 0x2341)//Arduino Vendor ID
                    {
                        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                        usbManager.requestPermission(device, pi);
                        keep = false;
                    } else {
                        connection = null;
                        device = null;
                    }
                    if (!keep)
                        break;
                }
            }


        }

        // fonction qui envoie le message qu'on a écrit dans le editTexte
        public void onClickSend(View view) {

            String string = editText.getText().toString();
            serialPort.write(string.getBytes());
            tvAppend(textView, "\nData Sent : " + string + "\n");

        }

        // fonction qui ferme la communication entre l'appareil Android et Arduino
        public void onClickStop(View view) {
            setUiEnabled(false);
            serialPort.close(); // Permet de fermer la connexion
            tvAppend(textView,"\nSerial Connection Closed! \n");

        }

        public void onClickClear(View view) {
            textView.setText(" ");
        }


        // Permet de mettre les messages envoyés et reçus de l'arduino vers le textview
        private void tvAppend(TextView tv, CharSequence text) {
            final TextView ftv = tv;
            final CharSequence ftext = text;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ftv.append(ftext);
                }
            });
        }

        // permet de voir si les motors des roux marches.
        public void onClickMotor(View view) {
            String string = "kekstra";
            serialPort.write(string.getBytes()); // envoie une imformation à l'arduino
            tvAppend(textView, "\nData Sent : " + string + "\n"); //Affiche l'information envoyé sur le textview dans l'affichage graphique
        }

        // Fonction qui de permet voir si la tete incline vers la droite
        public void onClickHeadRight(View view) {
            String string = "H+0050";
            serialPort.write(string.getBytes()); // envoie une imformation à l'arduino
            tvAppend(textView, "\nData Sent : " + string + "\n"); //Affiche l'information envoyé sur le textview dans l'affichage graphique
        }

        // Fonction qui de permet voir si la tete incline vers la gauche
        public void onClickHeadLeft(View view) {
            String string = "H-0050";
            serialPort.write(string.getBytes()); // envoie une imformation à l'arduino
            tvAppend(textView, "\nData Sent : " + string + "\n"); //Affiche l'information envoyé sur le textview dans l'affichage graphique
        }

        // Fonction qui de permet voir si la tete incline vers le haut
        public void onClickHeadUp(View view) {
            String string = "V+0050";
            serialPort.write(string.getBytes()); // envoie une imformation à l'arduino
            tvAppend(textView, "\nData Sent : " + string + "\n"); //Affiche l'information envoyé sur le textview dans l'affichage graphique
        }

        // Fonction qui de permet voir si la tete incline vers le bas
        public void onClickHeadDown(View view) {
            String string = "V-0050";
            serialPort.write(string.getBytes()); // envoie une imformation à l'arduino
            tvAppend(textView, "\nData Sent : " + string + "\n"); //Affiche l'information envoyé sur le textview dans l'affichage graphique
        }

        // Fonction qui permet de voir si la main gauche descend
        public void onClickLeftHandDown(View view) {
            String string = "B-0050";
            serialPort.write(string.getBytes()); // envoie une imformation à l'arduino
            tvAppend(textView, "\nData Sent : " + string + "\n"); //Affiche l'information envoyé sur le textview dans l'affichage graphique
        }

        // Fonction qui permet de voir si la main gauche monte
        public void onClickLeftHandUp(View view) {
            String string = "B+0050";
            serialPort.write(string.getBytes()); // envoie une imformation à l'arduino
            tvAppend(textView, "\nData Sent : " + string + "\n"); //Affiche l'information envoyé sur le textview dans l'affichage graphique
        }

        // Fonction qui permet de voir si la main droite descend
        public void onClickRightHandDown(View view) {
            String string = "K-0050";
            serialPort.write(string.getBytes()); // envoie une imformation à l'arduino
            tvAppend(textView, "\nData Sent : " + string + "\n"); //Affiche l'information envoyé sur le textview dans l'affichage graphique
        }

        // Fonction qui permer de voir si la main droite monte
        public void onClickRightHandUp(View view) {
            String string = "K+0050";
            serialPort.write(string.getBytes()); // envoie une imformation à l'arduino
            tvAppend(textView, "\nData Sent : " + string + "\n"); //Affiche l'information envoyé sur le textview dans l'affichage graphique
        }

        // Fonction qui permet de voir si les yeux marchent
        public void onClickEyes(View view) {
            String string = "Y12";
            serialPort.write(string.getBytes()); // envoie une imformation à l'arduino
            tvAppend(textView, "\nData Sent : " + string + "\n"); //Affiche l'information envoyé sur le textview dans l'affichage graphique
        }

    }