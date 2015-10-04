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

package com.asesores.pushtotalk.util;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

public class Utils {
	
	private static String TAG = "UTILS";
	AsyncTask<Void, Void, Void> send;
	
	static boolean server_status = false;
	 
	
	/************* FUNCION QUE COMPRUEBA SI EL SERVIDOR ESTA ACTIVO *************************/
	// Return server_status;
	// Regression test for issue 1018003: DatagramSocket ignored a set timeout.
	@LargeTest
	public static boolean testUnicastPortAvailable() throws Exception {

		boolean server_status = false;

		DatagramSocket sock = null;

		String question = new String("PORTAVAILABLE");	
		String s1 = new String("User");
		String s2 = new String(AudioParams.CAST_TYPE);
		String s3 = new String("End");
		
		String salida = question+s1+s2+s3;
		
		byte[] call = new String(salida).getBytes();

		DatagramPacket packet = null;
		byte[] encodedFrame;
		String cadena = null;

		int timeout = 1500;
		long start = System.currentTimeMillis();

		InetAddress addr = null;
		int port = 9001;

		try {
			addr = InetAddress.getByName(AudioParams.SERVER_IP);

		} catch (UnknownHostException e) {
			Log.d(TAG, "CATCH: " + e.toString());
		}

		try {
			sock = new DatagramSocket();
			DatagramPacket p = new DatagramPacket(call, call.length, addr, port);

			sock.setSoTimeout(timeout);
			sock.send(p);

			encodedFrame = new byte[AudioParams.FRAME_SIZE_IN_BYTES];
			packet = new DatagramPacket(encodedFrame, encodedFrame.length);
			sock.receive(packet);

			cadena = new String(encodedFrame);

			if (cadena.contains("PUERTOUNICAST")) {
				

				Pattern p9 = Pattern.compile("PUERTOUNICAST(.*?)End");
				Matcher m9 = p9.matcher(cadena);
				m9.find();
				
				
				if(Integer.parseInt(m9.group(1))>0){
					server_status = true;
					AudioParams.MY_UNICAST_PORT = Integer.parseInt(m9.group(1));
					
					
				}
				else{
					server_status = false;
				}
				
				Log.d(TAG, "TEXT PUERTO UNICAST: " + m9.group(1));
				
			}
			
		} catch (SocketTimeoutException e) {
			// expected
			long delay = System.currentTimeMillis() - start;
			if (Math.abs(delay - timeout) > 1000) {

				Log.d(TAG, "timeout was not accurate. expected: " + timeout
						+ " actual: " + delay + " miliseconds.");

			}
		} finally {
			if (sock != null) {
				sock.close();
			}
		}

		return server_status;

	}
	
	
	/************* FUNCION QUE COMPRUEBA SI HAY CONEXION (3G Y WIFI) *************************/

	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isAvailable()
				&& cm.getActiveNetworkInfo().isConnected()) {
			return true;
		} else {
			return false;
		}

	}

	
	 /******************* MENSAJE DE ALERTA *****************************/

	public static void MensajeError(Context context, String titulo,
			String mensaje) {

		final AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle(titulo);
		alert.setMessage(mensaje);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

			}

		});

		alert.show();

	}
	
	

}


