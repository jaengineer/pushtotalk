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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.asesores.pushtotalk.util.AudioParams;
import com.asesores.pushtotalk.util.SendServer;
import com.asesores.pushtotalk.util.SendServerUnicast;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class PlayerDatos extends Thread {

	
	/********* VARIABLES SOCKET DATOS ********/
	public static DatagramSocket socket;
	private DatagramPacket packet;
	private static String TAG = "PLAYER_DATOS";
	private final String messageIPRECIBO_DATOS = "IPDATOS";
	/*****************************************/
	
	/******** VARIABLES PARA DATOS ALFANUMERICOS ****/
	private byte[] encodedFrame = new byte[AudioParams.FRAME_SIZE_IN_BYTES];
	public static String USER_SPEAKS = "";
	private static String cadena = null;
	public static String conectados = "null";
	/************************************************/
	
	
	/******** VARIABLES AUXILIARES ******************/
	private boolean isRunning = true;
	private boolean isFinishing = false;
	private Handler mHandler = new Handler();
	/************************************************/
	

	public void run() {
	
			init();
			while (isRunning()) {

				try {
					
					socket.receive(packet);
					cadena = new String(encodedFrame);
					
					
					/******** RECIBE LISTA DE USURIOS ONLINE ***********************/
					if (cadena.contains("conectados")) {
						
						Pattern p = Pattern.compile("conectados(.*?)CONECTADOS");
						Matcher m = p.matcher(cadena);
						m.find();
						AudioParams.conectados = m.group(1);
						
						
					}
					
					/***************************************************************/
					
					
					/********** RECIBE EL TIPO DE COMUNICACION Y EL USUARIO QUE INICIA LLAMADA ***/
					if (cadena.indexOf("ocupado") >= 0) {
						
						if(cadena.indexOf("BROADCAST") >= 0){
							int fin = cadena.indexOf("BROADCAST");
							USER_SPEAKS =  cadena.substring(7, fin);
							AudioParams.CAST_TYPE = "null";
							AudioParams.BROADCAST = "broadcast";
							
							Log.d(TAG, "TEXT: BROADCAST " + cadena.substring(7, fin));
						}
						else{
							int fin = cadena.indexOf("UNICAST");
							USER_SPEAKS =  cadena.substring(7, fin);
							AudioParams.CAST_TYPE = cadena.substring(7, fin);
							Main.UNICAST_USER = cadena.substring(7, fin);
						}

					}
					/*********************************************************************************/
					
					
					/********** SE LE ASIGNA UN PUERTO DISPONIBLE DESDE EL SERVIDOR PARA MODO UNICAST ******/
					if (cadena.contains("PUERTOUNICAST")) {

						Pattern p9 = Pattern.compile("PUERTOUNICAST(.*?)End");
						Matcher m9 = p9.matcher(cadena);
						m9.find();
						AudioParams.MY_UNICAST_PORT = Integer.parseInt(m9.group(1));
					}
					/**************************************************************************************/
					
					
				} catch (IOException e) {
					Log.d(TAG, "CATCH: " + e.toString());
				}
			}

			synchronized (this) {
				try {
					if (!isFinishing())
						this.wait();
				} catch (InterruptedException e) {
					Log.d(TAG, "CATCH: " + e.toString());
				}
			}
	
	}

	
	/******* METODO QUE SE CREA AL INICIAR EL THREAD, CREA SOCKET DATOS, Y ENVIAR IPDATOS CADA 9 SEGUNDOS ******/ 
	private void init() {
		try {

			socket = new DatagramSocket(AudioParams.PORT_CLIENT_DATOS);
			mHandler.postDelayed(mMuestraMensaje, 18000);
			
			/*********** ASYNCTASK PARA ENVIAR IPDATOS AL SERVER *******/
			SendServer sendserver = new SendServer(messageIPRECIBO_DATOS);
			sendserver.execute(socket);
			
			/*********** ASYNCTASK PARA UNICAST *******/
			SendServerUnicast sendserverUnicast = new SendServerUnicast(messageIPRECIBO_DATOS,AudioParams.PORT_SERVER_UNICAST);
			sendserverUnicast.execute(socket);
			
			packet = new DatagramPacket(encodedFrame, encodedFrame.length);

		} catch (IOException e) {
			Log.d(TAG, "CATCH: " + e.toString());
		}
	}
	/**********************************************************************************************************/
	

	
	public synchronized boolean isRunning() {
		return isRunning;
	}

	public synchronized void resumeAudio() {
		isRunning = true;
		this.notify();
	}

	public synchronized void pauseAudio() {
		isRunning = false;

	}

	public synchronized boolean isFinishing() {
		return isFinishing;
	}

	public synchronized void finish() {
	
		isRunning = false;
		isFinishing = true;
		this.notify();
	}

	

	private Runnable mMuestraMensaje = new Runnable() {
		public void run() {
			if (!socket.isClosed()) {
			
				/*********** ASYNCTASK PARA ENVIAR IPDATOS AL SERVER *******/
				
				/******** UNICAST ***********/
				SendServerUnicast sendserverUnicast = new SendServerUnicast(messageIPRECIBO_DATOS,AudioParams.PORT_SERVER_UNICAST);
				sendserverUnicast.execute(socket);
				
				/******** BROADCAST *********/
				SendServer sendserver = new SendServer(messageIPRECIBO_DATOS);
				sendserver.execute(socket);
			}

			mHandler.removeCallbacks(mMuestraMensaje);
			mHandler.postDelayed(this, 8000);
		}
	};
	
	
}
