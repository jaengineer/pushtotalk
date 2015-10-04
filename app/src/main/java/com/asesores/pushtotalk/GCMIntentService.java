/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asesores.pushtotalk;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.asesores.pushtotalk.util.AudioParams;
import com.asesores.pushtotalk.util.SendServer;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * IntentService responsible for handling GCM messages.
 */

public class GCMIntentService extends IntentService {
	
	public GCMIntentService() {
		  super("GcmIntentService");
	}

	private final String messageIPRECIBO = "IPRECIBO";
	public static NotificationManager notificationManager, notificationManagerChat;
	public static boolean notificationState = false, notificationStateChat = false;
	private static String TAG = "GCMINTENTSERVICE";
	
	private enum Titulo{Registro,Push,Adios,Aviso,sendPosicion};
	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        
        /****** EL PARAMETRO INTENT CONTIENE LOS DATOS QUE RECIBE NUESTRO BROADCASTRECEIVER ********/
        String messageType = gcm.getMessageType(intent);
        
        Log.d(TAG, "TEXT: " + "Received: " + messageType);
        
        if (!extras.isEmpty()) {  
          
        	/******** MOSTRAMOS LOS MENSAJES FILTRANDO POR MESSAGE TYPE QUE CONTIENE LOS DATOS QUE ENVIA EL SERVIDOR *********/
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
          //      sendNotification(context,"Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
           //     sendNotification("Deleted messages on server: " +
            //            extras.toString());
              
            	/***** MENSAJE GCM ************/
            } else if (GoogleCloudMessaging. MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	
            	if (Main.player != null) {
        			if (!Player.socket.isClosed()) {
            	
            	
            	String usuario = intent.getExtras().getString("message");
				String titulo = intent.getExtras().getString("title");
            	String notif = "";
        		
            	Log.d(TAG, "TEXT: "+ "TIPO PUSH "+  usuario);
            	
            	if(titulo.contains("chat")) {
            		
            		int fin = titulo.indexOf("FIN");
        			String USER_SPEAKS =  titulo.substring(4, fin);
        			notif = "mensaje de " + USER_SPEAKS;
        			
        			if(!Main.usuario.contains(USER_SPEAKS))
        			sendNotification(usuario,USER_SPEAKS);
        			
            	}
            	else{
            		/******* MANDAMOS IPRECIBO Y REGID AL RECIBIR UN PUSH ******/
            	//	new send().execute(AudioParams.regId + messageIPRECIBO);
            		
            	//	SendServer  sendserver = new SendServer(AudioParams.regId + messageIPRECIBO);
            	//	sendserver.execute(Player.socket);
            		
            		
            		Titulo typeMessage = Titulo.valueOf(titulo);
            	switch(typeMessage){
            	
            	case Registro:
            		/********* NOTIFICACION PARA REGISTRO DE USUARIO *************/
            		if ((!Main.usuario.contains(usuario))&& (titulo.contains("Registro"))) {			
            			notif = usuario + " se ha conectado";
            			generateNotification(this, notif);
            		}
            		break;
            	case Push:
            		/******************* NOTIFICACION PARA UDP EN SEGUNDO PLANO Y ENVIO DE PUSH ***********/
            		if ((!Main.usuario.contains(usuario))&& (titulo.contains("Push"))) {        				
        				if (!Main.Activo) {
        					notif = "Recibiendo de: " + usuario;
        					generateNotification(this, notif);
        				}
            		}
            		
            		break;
            	case Adios:
            		/************* NOTIFICACION PARA MARCHA DE USUARIO ***********************/
            		if ((!Main.usuario.contains(usuario))&& (titulo.contains("Adios"))) {
    					notif = usuario + " se ha ido";
    					generateNotification(this,notif);
    				}
            		break;
            		
            	case Aviso:
            		Log.d(TAG, "TEXT: "+ "ENTRA EN LATIDUDES");
            		Pattern p = Pattern.compile("conectados(.*?)CONECTADOS");
					Matcher m = p.matcher(usuario);
					m.find();
					AudioParams.UsuariosPosicion = m.group(1);
					Log.d(TAG, "TEXT: "+ "CONECTADOS "+ AudioParams.UsuariosPosicion);
					
					Pattern p2 = Pattern.compile("CONECTADOS(.*?)LATITUD");
					Matcher m2 = p2.matcher(usuario);
					m2.find();
					AudioParams.LatitudUsuarios =  m2.group(1);
					Log.d(TAG, "TEXT: "+ "LATITUDES "+ AudioParams.LatitudUsuarios);
					
					Pattern p3 = Pattern.compile("LATITUD(.*?)LONGITUD");
					Matcher m3 = p3.matcher(usuario);
					m3.find();
					AudioParams.LongitudUsuarios =  m3.group(1);
					Log.d(TAG, "TEXT: "+ "LONGITUDES "+ AudioParams.LongitudUsuarios);
            		
            		//AudioParams.conectados = usuario;
            		//Log.d(TAG, "TEXT: "+ "USUARIOS PUSH "+ usuario);
            		break;
            		
            		case sendPosicion:
            			
            			Log.d(TAG, "TEXT: "+ "LLEGA UBICACION");
            		/************* NOTIFICACION PARA POSICION DE USUARIO ***********************/
            			Pattern p4 = Pattern.compile("USER(.*?)LATITUD");
    					Matcher m4 = p4.matcher(usuario);
    					m4.find();
    					String user = m4.group(1);
    					
    					Pattern p5 = Pattern.compile("LATITUD(.*?)LONGITUD");
    					Matcher m5 = p5.matcher(usuario);
    					m5.find();
    					String lat = m5.group(1);
    					
    					Pattern p6 = Pattern.compile("LONGITUD(.*?)END");
    					Matcher m6 = p6.matcher(usuario);
    					m6.find();
    					String lon = m6.group(1);
    					
            		if ((!Main.usuario.contains(user))&& (titulo.contains("sendPosicion"))) {
    					notif ="QRR " + usuario + " ha mandado su ubicacion";
    					NotificationPosicion(lat,lon,user);
    				}
            		break;
            	}
            	}
            	
            }
            	}
            
        }
        }
       
        /****** LIBERA  EL WAKE LOCK DEL  WakefulBroadcastReceiver *****/
        GcmBroadcastReceiver.completeWakefulIntent(intent);
		
	}
	
	public class send extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... params) {
			String s1 = new String();
		//String	message = AudioParams.regId + messageIPRECIBO;
			if (params[0].equals("server:Finish")) {
				s1 = params[0];
			} else {
				s1 = "server:Welcome";
			}

			String s2 = new String(Main.usuario);
			String s3 = new String("End");
			String s4 = new String("RegId");
			String s5 = new String(params[0]);
		//	String s5 = new String("RegId");
		//	String s6 = new String("EndRegId");

			String salida = s1 + s2 + s3 + s4 + s5;

			DatagramPacket p = null;
			byte[] call = new String(salida).getBytes();

			InetAddress address = null;	
			
		//int port = AudioParams.PORT_SERVER_BRODCAST;
			
			int port = AudioParams.PORT_SERVER_BRODCAST_DATOS_USER;
			
			 
			try {
				address = InetAddress.getByName(AudioParams.SERVER_IP);
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			p = new DatagramPacket(call, call.length,address, port);

			try {
			
				
				Player.socket.send(p);
				
			} catch (IOException e) {
				Log.d(TAG, "CATCH: " + e.toString());
			}

			return null;
		}
			
		}
	
	private void sendNotification(String msg,final String destino){
		notificationStateChat = true;
		 final int NOTIFICATION_ID = 1;
	/*	final int NOTIFICATION_ID = 1;
		 NotificationManager    mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);*/
		 
	//	 NotificationCompat.Builder builder;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("message", msg);
        intent.putExtra("destino", destino);
        
        
        /***********************/
        notificationManagerChat = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
        
       
        int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon,"mensaje de "+ destino, when);
		String title = this.getString(R.string.app_name);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
        /***********************/

   //     PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      
        /**************************/
		notification.setLatestEventInfo(this, title, "mensaje de "+ destino, contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.ledARGB = 0xff00ff00;
		notification.ledOnMS = 300;
		notification.ledOffMS = 1000;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		 
		 
		 notificationManagerChat.notify(NOTIFICATION_ID, notification);
		
        /***************************/
      /*  NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());*/

        // show message on text view
        if(ChatActivity.getInstance() != null){
            final String message = msg;
            ChatActivity.getInstance().runOnUiThread(new Runnable() {
                public void run() {
                	ChatActivity.getInstance().retrieveMessage(message,destino);
                }
            });
        }
        
        notificationStateChat = true;
    }
	
	private void NotificationPosicion(String lat,String lon,final String destino){
		//notificationStateChat = true;
		 final int NOTIFICATION_ID = 1;
	/*	final int NOTIFICATION_ID = 1;
		 NotificationManager    mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);*/
		 
	//	 NotificationCompat.Builder builder;

        Intent intent = new Intent(this, SendPosicion.class);
        intent.putExtra("latitud", lat);
        intent.putExtra("longitud", lon);
        intent.putExtra("destino", destino);
        
        
        /***********************/
        notificationManagerChat = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
        
       
        int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon,"QRR " + destino +" le ha mandado su posicion", when);
		String title = this.getString(R.string.app_name);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
        /***********************/

   //     PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      
        /****************************/
        notification.setLatestEventInfo(this, title,"QRR " + destino + " Le ha mandado su ubicación", contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.ledARGB = 0xff00ff00;
		notification.ledOnMS = 300;
		notification.ledOffMS = 1000;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		 
		 
		 notificationManagerChat.notify(NOTIFICATION_ID, notification);
		
        /***************************/
      /*  NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());*/

        // show message on text view
       /* if(SendPosicion.getInstance() != null){
            final String latitud = lat;
            final String longitud = lon;
            final String user = destino;
            SendPosicion.getInstance().runOnUiThread(new Runnable() {
                public void run() {
                	SendPosicion.getInstance().RecibirPosicion(latitud, longitud, user);
                }
            });
        }*/
        
       // notificationStateChat = true;
    }
	
	@SuppressWarnings("deprecation")
	private static void generateNotification(Context context, String message) {
		notificationState = true;
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);
		String title = context.getString(R.string.app_name);
		Intent notificationIntent = new Intent(context, Main.class);
		
		
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.ledARGB = 0xff00ff00;
		notification.ledOnMS = 300;
		notification.ledOffMS = 1000;
		// notification.defaults |= Notification.DEFAULT_VIBRATE;
		// notification.defaults |= Notification.DEFAULT_SOUND;
		// notification.defaults |= Notification.DEFAULT_LIGHTS;

		notificationManager.notify(0, notification);
		notificationState = true;
	}

	

}
