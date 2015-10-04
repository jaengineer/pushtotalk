/* Copyright 2013 Asesores Locales Consultoria S.A.
 
This file is part of Push2Talk.
 
Push2Talk is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
 
Push2Talk is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
 
You should have received a copy of the GNU General Public License
along with Push2Talk.  If not, see <http://www.gnu.org/licenses/>. */

package com.asesores.pushtotalk;


import static com.asesores.pushtotalk.util.AudioParams.SENDER_ID;
import static com.asesores.pushtotalk.util.AudioParams.SERVER_URL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.asesores.pushtotalk.codecs.Speex;
import com.asesores.pushtotalk.settings.AudioSettings;
import com.asesores.pushtotalk.settings.CommSettings;
import com.asesores.pushtotalk.util.AudioParams;
import com.asesores.pushtotalk.util.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothDevice;


public class Main extends Activity implements OnTouchListener {
	
	/***********************  ALERTAS DE SONIDO AL HABLAR  ****************************************/
	
	private SoundPool sp;
	private SoundPool ep;
	
	private int flujodemusica;
	private int errorrecording;
	
	/***********************************************/
	

	public static boolean Activo = true;

	/******* VARIABLES LISTA USUARIOS *********/
	public static String listaUsuarios = null;
	public static String usersConnected = null;
	public static String usuario = "";
	private static String private_mode = "Privado";
	public static String UNICAST_USER = "";
	/******************************************/

	/****** VARIABLES INTERFAZ *****************/
	private ImageView microphoneImage;
	private TextView user_speaking;
	private TextView user_name;
	private TextView private_user;
	public static final int MIC_STATE_NORMAL = 0;
	public static final int MIC_STATE_PRESSED = 1;
	public static final int MIC_STATE_DISABLED = 2;
	private static int microphoneState = MIC_STATE_NORMAL;
	/***********************************************/
	
	
	/********** VARIABLES THREAD *************/
	static Player player;
	
	static Recorder recorder;
	public static PlayerDatos playerdatos;
	/*****************************************/
	
	
	/******* VARIABLES PARA CAMBIAR COLOR DEL BOTON CUANDO RECIBE EL AUDIO *****/
	private static Handler handlerButtonState = new Handler();
	private static Handler handlerListUsersOnline = new Handler();
	private static Runnable runnableButtonState,runnableListUsersOnline;
	private static int storedProgress = 0;
	private static final int PROGRESS_CHECK_PERIOD = 100;
	/****************************************************************************/
	
	
	/*********** VARIABLES AUXILIARES **********/
	private static String TAG = "MAIN";
	private static boolean isStarting = true;
	private AudioManager audioManager = null;
	public static boolean bluetoothOn = false;
	private static final int PROGRESS_CHECK_PERIOD_LIST = 3000;
	/*******************************************/

	/************ VARIABLES SABOX ***************/
   public static final int MESSAGE_STATE_CHANGE = 1;
   public static final int MESSAGE_READ         = 2;
   public static final int MESSAGE_DEVICE_NAME  = 4;
   public static final int MESSAGE_TOAST        = 5;
   

   public static final String DEVICE_NAME = "device_name";
   public static final String TOAST = "toast";
   
// Name of the connected device
   private String mConnectedDeviceName = null;
	 // Local Bluetooth adapter
   private BluetoothAdapter mBluetoothAdapter = null;
// Member object for the PTT services
   private SavoxPTTService mPTTService = null;
   
   private static final int REQUEST_CONNECT_DEVICE_INSECURE = 1;
   private static final int REQUEST_ENABLE_BT = 2;
	/********************************************/
	
	public static String[] arrayUsersConnected = null;
	public static String[] arrayLatitudes = null;
	public static String[] arrayLongitudes = null;
	String regId="";
	
	public static String mensaje = "";
	
	/**** NUEVAS VARIABLES GCM ************/
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private Context context;
    private GoogleCloudMessaging gcm;
    private String regid;
    /***************************************/
    
    
    public boolean UnicastAvailable = false;
    
    /******** VARIABLES GPS ************/
	private LocationListener locListener;
	LocationManager locManager;
    /***********************************/
	
	/****** VARIABLES CANAL PERMANENTE**********/
	public static int activarVoz = 0;
	 boolean canalAbierto = false;
	/***************************************/
	 
	
	

	 /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
        	
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
            
        } catch (PackageManager.NameNotFoundException e) {
           
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
	/**************************************/
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		
		
		/************* GPS ************/
		mHandler.postDelayed(getPosicion, 10000);
		/******************************/
		
		
		/******** GCM NUEVA VERSIOM ****/
		context = getApplicationContext();
		//int version = getAppVersion(context);
		// Log.d(TAG, "TEXT: " + "Version de la App: " + version);
		
		// Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = getRegistrationId(context);
            Log.d(TAG, "TEXT: " + "regid : " + regId);
            
            AudioParams.regId = regId;
            
            if (regId.isEmpty()) {
            	Log.d(TAG, "TEXT: " + "regid vacio: " + regId);
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
		/********************************/
		
		 
		/********* BLUETOOTH SAVOX ******************/
		
		/********* Get local Bluetooth adapter *******/
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /***** SI es nulo el dispositivo no tiene bluetooth ********/
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
		/***********************************************/

		

		/******* MANTIENE LA PANTALLA ACTIVA ************************/
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		/************************************************************/

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		user_speaking = (TextView) findViewById(R.id.userSpeaks);
		user_name = (TextView) findViewById(R.id.userName);
		private_user = (TextView) findViewById(R.id.UserPrivado);
		microphoneImage = (ImageView) findViewById(R.id.microphone_image);
		microphoneImage.setOnTouchListener(this);
		
		
		/***********************  inicializar ALERTAS DE SONIDO AL HABLAR  ****************************************/
		sp = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
		ep = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
    	flujodemusica= sp.load(this,R.raw.phonebeep,1);
    	errorrecording = ep.load(this,R.raw.errorsignal,1);
		
		/******* USUARIO ACTUAL ****************/
    	usuario = recuperarNombrePref();
		user_name.setText(usuario);
		
		/***************************************/
		
		if (isStarting) {
			Log.i(TAG,"App isStarting");
			 init();		
		}		
		
	}
	
	
	
	/******** RECUPERAMOS NOMBRE DEL USUARIO **************************************************/
	public String recuperarNombrePref(){
		
		SharedPreferences prefs =    getSharedPreferences("GessanPTTPrefs", Context.MODE_PRIVATE);
		String nombre = prefs.getString("nombre", "nombre_por_defecto");
		
		return  nombre;
	}
	
	public void runnableHandlerButtonState() {

		runnableButtonState = new Runnable() {

			public void run() {			
				

				int currentProgress = player.getProgress();

				if (currentProgress != storedProgress) {

					if (getMicrophoneState() != MIC_STATE_DISABLED) {

						setMicrophoneState(MIC_STATE_DISABLED);
						
					}
				} else {
					if (getMicrophoneState() == MIC_STATE_DISABLED) {
						setMicrophoneState(MIC_STATE_NORMAL);
					}
				}
				storedProgress = currentProgress;
				handlerButtonState.postDelayed(this, PROGRESS_CHECK_PERIOD);
			}
		};

		handlerButtonState.removeCallbacks(runnableButtonState);
		handlerButtonState.postDelayed(runnableButtonState, PROGRESS_CHECK_PERIOD);
	}
	
	/****************** ACTUALIZA LA LISTA DE USUARIOS Y EL BLUETOOTH **************/
	public void runnableHandlerListUsersOnline() {

		runnableListUsersOnline = new Runnable() {

			public void run() {
				
				handlerListUsersOnline.postDelayed(this, PROGRESS_CHECK_PERIOD_LIST);
			}
		};
		
		handlerListUsersOnline.removeCallbacks(runnableListUsersOnline);
		handlerListUsersOnline.postDelayed(runnableListUsersOnline, PROGRESS_CHECK_PERIOD_LIST);
	}
	/*****************************************************************************************/

	public void init() {

		CommSettings.getSettings(this);
		AudioSettings.getSettings(this);

	/*	if (AudioSettings.useSpeex()) {
			Speex.open(AudioSettings.getSpeexQuality());
		}*/

		player = new Player();
		playerdatos = new PlayerDatos();
		recorder = new Recorder();
		user_name.setText(usuario);
		runnableHandlerButtonState();
	    runnableHandlerListUsersOnline();

		player.start();
		playerdatos.start();
		recorder.start();
		
		isStarting = false;

	}
	
  
	@Override
	public void onStart() {
		super.onStart();
		Activo = true;
		Log.d(TAG, "TEXT: " + "ON START");
	}

	
	@Override
	public void onResume() {
		super.onResume();
		
		/**** GCM NUEVA VERSIOM *********/
		checkPlayServices();
		/*******************************/
		
		Log.d(TAG, "TEXT: " + "ON RESUME");
		
		// Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mPTTService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mPTTService.getState() == SavoxPTTService.STATE_NONE) {
              // Start the Bluetooth chat services
              mPTTService.start();
            }
        }
        
       
	}
	
	// Called implicitly when device is about to sleep or application is backgrounded
	@Override
	protected void onPause(){
	    //super.onPause();
	    //isFinishing();	   
	    Log.i(TAG, "TEXT: " + "ON PAUSE");
	    Log.i(TAG, "TEXT: " + isFinishing());
	    super.onPause();
	}

	
	private void setupPTT() {

        // Initialize the BluetoothChatService to perform bluetooth connections
        mPTTService = new SavoxPTTService(this, mHandler);
    }

	@Override
	public void onStop() {		
		
		super.onStop();
		Activo = false;
		Log.i(TAG, "TEXT: " + "ON STOP");
	}


	@Override
	public void onDestroy() {
		
		Log.d(TAG, "TEXT: " + "ON DESTROY");	
		
		Player player = new Player();
		player.sayGoodbye();
		
		
		SharedPreferences prefs =  getSharedPreferences("GessanPTTPrefs", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("estado", "offline");		
		editor.commit();
		
		
		// Stop the Bluetooth chat services
        if (mPTTService != null) {
        	mPTTService.stop();
        }
        
   
        super.onDestroy();
	}

	@Override
	public void onBackPressed() {
	
		Intent backtoHome = new Intent(Intent.ACTION_MAIN);
		backtoHome.addCategory(Intent.CATEGORY_HOME);
		backtoHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(backtoHome);
		
	}
	
	/********* HANDLER SAVOX *****************/
	// The Handler that gets information back from the SavoxPTTService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case SavoxPTTService.STATE_CONNECTED:
                break;
                
                case SavoxPTTService.STATE_CONNECTING:
                break;
                
                case SavoxPTTService.STATE_LISTEN:
                case SavoxPTTService.STATE_NONE:
             
                break;
                }
                break;
            case MESSAGE_READ:
            	
            	//read PTT status message
                byte[] readBuf = (byte[]) msg.obj;
                
                if(getSavoxPTT(readBuf)){
                	Recorder.canalAbierto = false;
                	setMicrophoneState(MIC_STATE_PRESSED);
                	if (Utils.isOnline(Main.this)) {
    					try {
    						
    						/*******para enviar la posicion*******/
    						activarVoz++;
    						
    						/******** ACTIVAR RECONOCIMEINTO DE VOZ *************/
    						if(activarVoz == 3){
    							 if(AudioParams.Latitud.equals("sin_datos")){
    								 Utils.MensajeError(Main.this, "Atenci�n","Posici�n no disponible");
    							 }
    							 else{
    								 /****** ARRANCAR EL TEXT TO SEECH ***************/
//    								 Intent intentVoz=new Intent(context, TTS.class);
//                                     intentVoz.putExtra("mensaje", "me gustas tio");
//                                     context.startService(intentVoz);
    								 /************************************************/	
    							
    								
    							 Recorder.canalAbierto=true;
    						
    							 Intent intent = new Intent(Main.this, SendPosicion.class);
   							
    							 startActivity(intent);
    							 }
    							 activarVoz = 0;
    						}
    						/**************************************************/
    						
    						
    						/********** BROADCAST ***********************/
    						if (AudioParams.CAST_TYPE.contains("null")) { 
    							
    							setMicrophoneState(MIC_STATE_PRESSED);
    							player.pauseAudio();
    							recorder.resumeAudio();
    							play_sp();
    							
    						} else {
    							
    							/****** UNICAST *************************/
    							SendServerAvailable  sendserver = new SendServerAvailable();
    						    sendserver.execute();

    						}
    					} catch (Exception error) {
    						Log.d(TAG, "CATCH: " + error.toString());
    					}
    				} else {
    					play_wrong();
    					//Utils.MensajeError(Main.this, TAG, "NO HAY CONEXION");
    				}
                }else{
                	
                	setMicrophoneState(MIC_STATE_NORMAL);
                	recorder.pauseAudio();
    				AudioParams.BROADCAST = "null";
    				player.resumeAudio();
               
                }
        	//	mPTTButton.setPressed(getSavoxPTT(readBuf));
                break;
            case MESSAGE_DEVICE_NAME:
            	
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connectado " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
              
                break;
            case MESSAGE_TOAST:
            	
            //	setOffBluetooth();
            	
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
             
                if(msg.getData().getString(TOAST).equals("SE HA DESCONECTADO EL DISPOSITIVO"))
                	setOffBluetooth();
                
                break;
                
            }
        }
    };
	/*****************************************/
    
   
  

    /********** MENU DE OPCIONES: CONFIGURACION DEL AUDIO Y SALIR DE LA APP *************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.option_menu, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;

		switch (item.getItemId()) {
		
		  case R.id.connect_scan:
			  
			  // If BT is not on, request that it be enabled.
		        // setupChat() will then be called during onActivityResult
				
				/***** SI BLUETOOTH NO ESTA ACTIVO PREGUNTA PARA HABILITARLO ***********/
		        if (!mBluetoothAdapter.isEnabled()) {
		            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		        
		            // Otherwise, setup the chat session
		        } else {
		            if (mPTTService == null)
		            	setupPTT();
		        }
		      
	            // Launch the DeviceListActivity to see devices and do scan
	            i = new Intent(this, DeviceListActivity.class);
	            startActivityForResult(i, REQUEST_CONNECT_DEVICE_INSECURE);
	           
	            return true;
	            
		  case R.id.usuarios:
			  listaDispositivos("Buscando dispositivo ",usersConnected);
			  return true;
			  
		  case R.id.mensajes:
			  Mensajes(usersConnected);
			  return true;

		  case R.id.settings_audio:
			i = new Intent(this, AudioSettings.class);
			startActivityForResult(i, 0);
			return true;
			
		 /* case R.id.posicionusuario:
			  PosicionUsuarios(usersConnected);
				return true;*/
				
		  case R.id.enviarposicion:
			  EnviarPosicion(usersConnected);
				return true;

			
		  /*case R.id.mapa:
				Mapa();
				return true;*/

		case R.id.exit_app:
			return closeApp();
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	/**********************************************************************************/

	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		CommSettings.getSettings(this);
		AudioSettings.getSettings(this);

	/*	if (AudioSettings.useSpeex()) {
			Speex.open(AudioSettings.getSpeexQuality());
		} else { */
			Speex.close();
		//}
		
		switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
            	  
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupPTT();
            } else {
                // User did not enable Bluetooth or an error occured
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
             //   finish();
            }
        }
		
		
	}
	
	
	// =============== Savox ===================== //
    // get PTT status from Savox BTH-101 or BTR-155
    // PTT is signaled with the AT command
    // AT+PTT=P, when PTT is pressed and
    // AT+PTT=R, when PTT is released
    // Host app sees these as messages
    // +PTT=P, when PTT is pressed and
    // +PTT=R, when PTT is released
	private Boolean getSavoxPTT(byte [] data) {
    	Boolean status = false;
    	// Check, if PTT is pressed, i.e. AT+PTT=P
    	if (data[5] == 0x50) // 'P' in ASCII value
    		status = true;
    	
    	// check, if PTT is released, i.e. AT+PTT=R
    	if (data[5] == 0x52) // 'R' in ASCII value
    		status = false;
    	
    	return status;
    }
    // =============== Savox ===================== //
	
    private void connectDevice(Intent data, boolean secure) {
    	
    	
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        
        // Attempt to connect to the device
        mPTTService.connect(device); 
      
        
			setOnBluetooth();
        
    }

	/********* BOTON DE AUDIO: DOWN PARA HABLAR Y UP PARA ESCUCHAR ********/
	public boolean onTouch(View v, MotionEvent e) {
		if (getMicrophoneState() != MIC_STATE_DISABLED) {
			switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (Utils.isOnline(Main.this)) {
				Log.e(TAG,"ENTRA EN EL ACTION DOWN");
				
					try {
						
						/********** BROADCAST ***********************/
						Log.e(TAG,"MUESTRA CAST_TYPE -> "+AudioParams.CAST_TYPE );
						if (AudioParams.CAST_TYPE.contains("null")){ 
							setMicrophoneState(MIC_STATE_PRESSED);
							player.pauseAudio();
							recorder.resumeAudio();
							play_sp();
						} else {
							
							/****** UNICAST *************************/
							SendServerAvailable  sendserver = new SendServerAvailable();
						    sendserver.execute();
						    
						}
					} catch (Exception error) {
						Log.d(TAG, "CATCH: " + error.toString());
					}
				} else {
					play_wrong();
					//Utils.MensajeError(Main.this, TAG, "NO HAY CONEXION");
					
				}
				break;
			case MotionEvent.ACTION_UP:
				UnicastAvailable = false;
				setMicrophoneState(MIC_STATE_NORMAL);
				recorder.pauseAudio();
				AudioParams.BROADCAST = "null";
				player.resumeAudio();
				break;
			}
		}
		return true;
	}
	
	
	/*********************** FUNCIONES PARA REPRODUCIR ALERTAS DE SONIDO AL HABLAR  ****************************************/
	
	private void play_sp() {
		// TODO Auto-generated method stub
		sp.play(flujodemusica, 0.1f, 0.1f, 0, 0, 1);
	}
	private void play_wrong() {
		// TODO Auto-generated method stub
		ep.play(errorrecording, 0.1f, 0.1f, 0, 0, 1);
	}
	
	/********************************************************************************/

	
	
	public synchronized void setMicrophoneState(int state) {
		switch (state) {
		case MIC_STATE_NORMAL:
			microphoneState = MIC_STATE_NORMAL;
			microphoneImage
					.setImageResource(R.drawable.microphone_normal_image);
			user_speaking.setText("");
			break;
		case MIC_STATE_PRESSED:
			microphoneState = MIC_STATE_PRESSED;
			microphoneImage.setImageResource(R.drawable.microphone_pressed_image);
			
			if(!AudioParams.CAST_TYPE.equals("null")){
				private_user.setText(private_mode + " a " + UNICAST_USER);
			}
			else{
				private_user.setText("");
			}
			
			user_speaking.setText("Enviando ... ");
					break;
		case MIC_STATE_DISABLED:
			microphoneState = MIC_STATE_DISABLED;
			microphoneImage.setImageResource(R.drawable.microphone_disabled_image);
			runOnUiThread(new Runnable() {
				public void run() {

					if (!AudioParams.CAST_TYPE.equals("null")) {
						private_user.setText(private_mode + " de " + PlayerDatos.USER_SPEAKS);
						user_speaking.setText("Recibiendo de " + PlayerDatos.USER_SPEAKS);
						Log.i(TAG, "TEXT: RECIBIENDO DE: " +PlayerDatos.USER_SPEAKS + " (UNICAST)");
					} else {
						private_user.setText("");						
						user_speaking.setText("Recibiendo de " + PlayerDatos.USER_SPEAKS );
						Log.i(TAG, "TEXT: RECIBIENDO DE: " +PlayerDatos.USER_SPEAKS + " (BROADCAST)");
					}

				}
			});
			break;
		}
	}

	public synchronized int getMicrophoneState() {
		return microphoneState;
	}

	private boolean closeApp() {
	
		new AlertDialog.Builder(this).setTitle("Salir")
				.setMessage("�Realmente quiere salir?")
				.setNegativeButton(android.R.string.no, null)
				.setPositiveButton(android.R.string.yes, new OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
						
						

						handlerButtonState.removeCallbacks(runnableButtonState);
						handlerListUsersOnline.removeCallbacks(runnableListUsersOnline);

						player.finish();
						playerdatos.finish();
						player = null;
						playerdatos = null;
						recorder = null;
						isStarting = false;
						
						//GPS
					//	locManager.removeUpdates(locListener);

						if(GCMIntentService.notificationState)
							GCMIntentService.notificationManager.cancel(0);
						
						if(GCMIntentService.notificationStateChat)
							GCMIntentService.notificationManagerChat.cancel(0);
						
						System.exit(0);
					}
				}).create().show();

		return true;
	}

	
	
	
/************** METODOS PARA BLUETOOTH **********************/
	
	public void setOnBluetooth(){
		
		 audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		 audioManager.setMode(0);
		 audioManager.setBluetoothScoOn(true);
		 audioManager.startBluetoothSco();
		 audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		 bluetoothOn = true;
	}
	
	public void setOffBluetooth(){
		
		 audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		 audioManager.setBluetoothScoOn(false);
		 audioManager.stopBluetoothSco();
		 audioManager.setMode(AudioManager.MODE_NORMAL);
		 bluetoothOn = false;
	}
	
	/************************************************************/

	
	
	 void listaDispositivos(String texto,String usersConnected){	

	//	arrayUsersConnected = PlayerDatos.conectados.split(",");
		 arrayUsersConnected = AudioParams.conectados.split(",");

		
		
		 Log.d(TAG, "TEXT:LISTA ONLINE "+ arrayUsersConnected.toString());  
		
		AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
		     
		dialogo.setTitle("Lista de usuarios");
		
		dialogo.setItems(arrayUsersConnected, new OnClickListener() {
		   public void onClick(DialogInterface dialog, int item) {
	if ( arrayUsersConnected[item].equals("broadcast")) {				
					
					AudioParams.CAST_TYPE = "null";
					private_user.setText("");

				} else {
					
					AudioParams.CAST_TYPE =  arrayUsersConnected[item];
					private_user.setText(private_mode + " a " +  arrayUsersConnected[item]);
					UNICAST_USER = arrayUsersConnected[item];
					
					Log.d(TAG, "TEXT: USER UNICAST" + UNICAST_USER);
					
				

				}
		   //   Toast.makeText(this, arrayUsersConnected[item], Toast.LENGTH_LONG).show();
			   
		   }
		});
		     
		dialogo.create();
		dialogo.show();

	}
	 
	 void EnviarPosicion(String usersConnected){
		 
		 if(AudioParams.Latitud.equals("sin_datos")){
			 Utils.MensajeError(Main.this, "Atenci�n","Posici�n no disponible");
		 }
		 else{
		 Intent intent = new Intent(Main.this, SendPosicion.class);
		 startActivity(intent);
		 }
	 }
	 
	 
	 void Mensajes(String usersConnected){	
		 
		 arrayUsersConnected = AudioParams.conectados.split(",");
		 AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
		 dialogo.setTitle("Destino");
		 
		 dialogo.setItems(arrayUsersConnected, new OnClickListener() {
			   public void onClick(DialogInterface dialog, int item) {
		
					
						String user = arrayUsersConnected[item];
				    	Bundle b = new Bundle();
						Intent intent = new Intent(Main.this, ChatActivity.class);
						b.putString("usuario",user);
						intent.putExtras(b);
						startActivity(intent);
						   
			   }
			});
			     
			dialogo.create();
			dialogo.show();
		 
	
	 }
	 
	 
	
	/****************GCM NUEVA VERSIOM************************/
	 
	 /**
	     * Check the device to make sure it has the Google Play Services APK. If
	     * it doesn't, display a dialog that allows users to download the APK from
	     * the Google Play Store or enable it in the device's system settings.
	     */
	    private boolean checkPlayServices() {
	        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	        if (resultCode != ConnectionResult.SUCCESS) {
	            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
	            } else {
	                
	                Log.d(TAG, "TEXT: " + "este dispositivo no soporta google play service");
	                finish();
	            }
	            return false;
	        }
	        return true;
	    }
	    
	    
	    
	    /**
	     * Gets the current registration ID for application on GCM service.
	     * <p/>
	     * If result is empty, the app needs to register.
	     *
	     * @return registration ID, or empty string if there is no existing
	     * registration ID.
	     */
	    private String getRegistrationId(Context context) {
	    	
	        final SharedPreferences prefs = getGCMPreferences(context);
	        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	        if (registrationId.isEmpty()) {
	            Log.d(TAG, "TEXT: " + "Registro Id no encontrado");
	            return "";
	        }
	        // Check if app was updated; if so, it must clear the registration ID
	        // since the existing regID is not guaranteed to work with the new
	        // app version.
	        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	        int currentVersion = getAppVersion(context);
	        if (registeredVersion != currentVersion) {
	          //  Log.i(TAG, "App version changed.");
	            Log.d(TAG, "TEXT: " + "Version de la App cambiada");
	            return "";
	        }
	        
	        return registrationId;
	    }
	    
	    
	    /**
	     * @return Application's {@code SharedPreferences}.
	     */
	    private SharedPreferences getGCMPreferences(Context context) {
	        // This sample app persists the registration ID in shared preferences, but
	        // how you store the regID in your app is up to you.
	        return getSharedPreferences(Main.class.getSimpleName(),
	                Context.MODE_PRIVATE);
	    }
	 /**************************************/


	    /**
	     * Registers the application with GCM servers asynchronously.
	     * <p/>
	     * Stores the registration ID and app versionCode in the application's
	     * shared preferences.
	     */
	    private void registerInBackground() {
	   
	       new AsyncTask<Void, Void, String>() {
	            @Override
	            protected String doInBackground(Void... params) {
	                String msg = "";
	                try {
	                    if (gcm == null) {
	                        gcm = GoogleCloudMessaging.getInstance(context);
	                    }
	                    regid = gcm.register(SENDER_ID);
	                    AudioParams.regId = regid;
	                    msg = "Device registered, registration ID=" + regid;
	                    
	                    
	                    boolean registered = ServerUtilities.register(context,regid);
	                    // You should send the registration ID to your server over HTTP, so it
	                    // can use GCM/HTTP or CCS to send messages to your app.
	                    Handler h = new Handler(getMainLooper());
	                    h.post(new Runnable() {
	                        public void run() {
	                      //      subscribeToPushNotifications(regid);
	                        }
	                    });

	                    // For this demo: we don't need to send it because the device will send
	                    // upstream messages to a server that echo back the message using the
	                    // 'from' address in the message.

	                    // Persist the regID - no need to register again.
	                    storeRegistrationId(context, regid);
	                } catch (IOException ex) {
	                    msg = "Error :" + ex.getMessage();
	                    // If there is an error, don't just keep trying to register.
	                    // Require the user to click a button again, or perform
	                    // exponential back-off.
	                }
	                return msg;
	            }

	            @Override
	            protected void onPostExecute(String msg) {
	                Log.i(TAG, msg + "\n");
	            }
	        }.execute(null, null, null);
	    }
	    
	    /************************/
	    
	    /**
	     * Stores the registration ID and app versionCode in the application's
	     * {@code SharedPreferences}.
	     *
	     * @param context application's context.
	     * @param regId   registration ID
	     */
	    private void storeRegistrationId(Context context, String regId) {
	        final SharedPreferences prefs = getGCMPreferences(context);
	        int appVersion = getAppVersion(context);
	        Log.i(TAG, "Saving regId on app version " + appVersion);
	        SharedPreferences.Editor editor = prefs.edit();
	        editor.putString(PROPERTY_REG_ID, regId);
	        editor.putInt(PROPERTY_APP_VERSION, appVersion);
	        editor.commit();
	    }
	    
	    
	    /******************* PORT UNICAST AVAILABLE ********************/
	    class SendServerAvailable  extends AsyncTask<String, Void, Integer> {
	    	
	    	
	    	 /******* TAREA QUE VA A EJECUTARSE EN SEGUNDO PLANO QUE TOMA COMA PARAMETRO EL ARRAY DE PARAMETROS DE ENTRADA DE NUESTRA CLASE *******/
	        @Override
	        protected Integer doInBackground(String... n) {
	    		
	    		 int result = 0;
	    		
	    		String serverUrl = SERVER_URL + "/portAvailable";
	    		
	    		 Map<String, String> parametros = new HashMap<String, String>();
	    	     parametros.put("port", "PORTAVAILABLE");
	    	     parametros.put("user",AudioParams.CAST_TYPE);
	    	     
	    	     result = post(serverUrl, parametros);
	    	     
	    		return result;
	    		/*********/
	    		
	    		
	    	}
	    	
	    	  protected void onPostExecute(Integer res) {
	    		  
	    		
	     
	                switch(res){
	                
	                case -1:
	             	   Utils.MensajeError(Main.this, "Atenci�n", "Error de red");
	             	   break;
	             	   
	                case 200:
	                	setMicrophoneState(MIC_STATE_PRESSED);
						player.pauseAudio();
						recorder.resumeAudio();
						play_sp();
	                	
	             	   break;
	             	   
	                case 300:
	                	
	                	Log.e(TAG,"PLAY_WRONG");
						play_wrong();
						Utils.MensajeError(Main.this, "Atenci�n","EL usuario est� ocupado");
	             	    break;
	             	   
	                case 350:
	                	
	                	Log.e(TAG,"PLAY_WRONG");
						play_wrong();
						Utils.MensajeError(Main.this, "Atenci�n","No hay puertos disponibles");
	             	    break;
	             	    
	                case 500:
	                	Log.e(TAG,"PLAY_WRONG");
						play_wrong();
						Utils.MensajeError(Main.this, "Atenci�n","No se ha encontrado el destino");
	                	break;
	                	
	                }
	    	  }
	    	  
	    	  
	    	  /********* PETICION POST HTTP AL SERVIDOR LOGIN *************/
	          public int post(String endpoint, Map<String, String> params)
	    	            {
	    	        URL url;
	    	        int status = -1;
	    	        String cadena = null;
	    	       
	    	       
	    	        
	    	        try {
	    	            url = new URL(endpoint);
	    	        } catch (MalformedURLException e) {
	    	            throw new IllegalArgumentException("invalid url: " + endpoint);
	    	        }
	    	        StringBuilder bodyBuilder = new StringBuilder();
	    	        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
	    	        
	    	        // constructs the POST body using the parameters
	    	        while (iterator.hasNext()) {
	    	            Entry<String, String> param = iterator.next();
	    	            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
	    	            if (iterator.hasNext()) {
	    	                bodyBuilder.append('&');
	    	            }
	    	        }
	    	        
	    	        String body = bodyBuilder.toString();
	    	        byte[] bytes = body.getBytes();
	    	        HttpURLConnection conn = null;
	    	        try {
	    	            conn = (HttpURLConnection) url.openConnection();
	    	            conn.setDoOutput(true);
	    	            conn.setUseCaches(false);
	    	            conn.setFixedLengthStreamingMode(bytes.length);
	    	            conn.setRequestMethod("POST");
	    	            conn.setRequestProperty("Content-Type",
	    	                    "application/x-www-form-urlencoded;charset=UTF-8");
	    	            
	    	            /*** PETICION POST AL SERVIDOR **********/
	    	            OutputStream out = conn.getOutputStream();
	    	            out.write(bytes);
	    	            out.close();
	    	            
	    	            /******** RESPUESTA ***********/
	    	            status = conn.getResponseCode();
	    	          
	    	             final BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    	             
	    	             cadena = new String(br.readLine());
	    	             Pattern p = Pattern.compile("PUERTOUNICAST(.*?)End");
	    				 final Matcher m = p.matcher(cadena);
	    				 m.find();
	    				 
	    				
	    					if(Integer.parseInt(m.group(1))>0){
	    						
	    						 new Runnable(){
	    			            	 public void run(){
	    			            		 
	    			            		 AudioParams.MY_UNICAST_PORT = Integer.parseInt(m.group(1));
	    			            		 
	    			            	 }
	    			             };
	    					}
	    					AudioParams.MY_UNICAST_PORT = Integer.parseInt(m.group(1));
	    					
	    	             Log.d(TAG, "TEXT: " + "STATUS RESPUESTA: " +Integer.parseInt(m.group(1)) + " " +  status );
	    	      
	    	          
	    	        } catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
	    	            if (conn != null) {
	    	                conn.disconnect();
	    	            }
	    	        }
	    	        
	    	        return status;
	    	      }
	          /*******************************************************************************/

	    }
	    /***************************************************************/
	    
	    
	    /**************** GPS **********************/
	    private void comenzarLocalizacion(){
	    	
	    	//OBTENEMOS UNA REFERENCIA AL LOCATIONMANAGER
	    	locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    	
	    	
	    	//OBTENEMOS LA ULTIMA POSICION CONOCIDA
	    	Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    	
	    	//Mostramos la �ltima posici�n conocida
	    	mostrarPosicion(loc);
	    	
	    //	private_user.setText("Lat " +AudioParams.Latitud+ "Long " +  AudioParams.Longitud );
	    	
	    	//Nos registramos para recibir actualizaciones de la posici�n
	    	locListener = new LocationListener() {
		    	public void onLocationChanged(Location location) {
		    		mostrarPosicion(location);
		    	}
		    	public void onProviderDisabled(String provider){
		    	//	lblEstado.setText("Provider OFF");
		    	}
		    	public void onProviderEnabled(String provider){
		    	//	lblEstado.setText("Provider ON ");
		    	}
		    	public void onStatusChanged(String provider, int status, Bundle extras){
		    		Log.i("", "Provider Status: " + status);
		    	//	lblEstado.setText("Provider Status: " + status);
		    	}
	    	};
	    	
	    	locManager.requestLocationUpdates(
	    			LocationManager.GPS_PROVIDER, 30000, 0, locListener);
	    }
	    
	    private void mostrarPosicion(Location loc) {
	    	if(loc != null){
	    		
	    		Log.d(TAG, "TEXT: "+ "Latitud: " + String.valueOf(loc.getLatitude()));
	    		Log.d(TAG, "TEXT: "+ "Longitud: " + String.valueOf(loc.getLongitude()));
	    		Log.d(TAG, "TEXT: "+ "Precision: " + String.valueOf(loc.getAccuracy()));
	    		
	    		AudioParams.Latitud = String.valueOf(loc.getLatitude());
	    		AudioParams.Longitud = String.valueOf(loc.getLongitude());
	    		AudioParams.Precision = String.valueOf(loc.getAccuracy());
	    		
	    		
	    	
	    	}
	    	else{
	    		Log.d(TAG, "TEXT: "+ "Latitud: (sin_datos)");
	    		Log.d(TAG, "TEXT: "+ "Longitud: (sin_datos)");
	    		Log.d(TAG, "TEXT: "+ "Precision: (sin_datos)");
	    		
	    		AudioParams.Latitud ="sin_datos";
	    		AudioParams.Longitud = "sin_datos";
	    		AudioParams.Precision = "sin_datos";
	    	}
	    }
	    
	    /*********** HILO PARA ENVIAR IPRECIBO AL SERVIDOR BROADCAST Y UNICAST CADA 5 SEGUNDOS *********/
		private Runnable getPosicion = new Runnable() {
			public void run() {
				
				comenzarLocalizacion();
				mHandler.removeCallbacks(getPosicion);
				mHandler.postDelayed(this, 20000);
			}
		};
	    
	    /******************************************/
	    
	  
	
}