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

import com.asesores.pushtotalk.Main;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;

public abstract class AudioParams {
	
	/******** GPS *********/
	public static String Latitud = "sin_datos";
	public static String Longitud = "sin_datos";
	public static String Precision = "";
	
	public static String LatitudUsuarios = "sin_datos";
	public static String LongitudUsuarios = "sin_datos";
	public static String UsuariosPosicion = "";
	/**********************/
	
	public static final String USER_NAME = Main.usuario;
	public static String regId = "";
	public static String conectados = "null";
	
	public static final int PORT_CLIENT_RCV = 55555;
	public static final int PORT_CLIENT_SND = 55556;
	public static final int PORT_CLIENT_DATOS = 55558;
	
	public static final int PORT_SERVER_BRODCAST = 9005;
	public static final int PORT_SERVER_BRODCAST_DATOS_USER = 9005;
//	public static final int PORT_SERVER_BRODCAST = 8005;
//	public static final int PORT_SERVER_BRODCAST = 8000;
	public static int PORT_SERVER_UNICAST = 9001;
	public static int MY_UNICAST_PORT = 9003;
	
//	public static int PORT_SERVER_UNICAST = 8001;
//	public static int MY_UNICAST_PORT = 8003;
	 
	//public static final String SERVER_URL = "https://login-jaengineer.c9.io";
	//public static final String SERVER_IP = "login-jaengineer.c9.io";

	public static final String SERVER_URL = "http://10.111.143.2:4000";
	public static final String SERVER_IP = "10.111.143.2";
	
//	public static final String SERVER_URL = "http://192.168.252.22:4000";
//	public static final String SERVER_IP = "192.168.252.22";	  
	
	// Clave para GCM (Push Notification)
	 public static final String SENDER_ID = "78498436983"; // Jose Alberto		 
//	 public static final String SENDER_ID = "161184049347"; // Jose Manuel
	
	public static final int SAMPLE_RATE = 8000;
	public static final int FRAME_SIZE = 160;
//	public static final int FRAME_SIZE_IN_BYTES = 320;
	public static final int FRAME_SIZE_IN_BYTES = 1280;
	public static final int ENCODING_PCM_NUM_BITS = AudioFormat.ENCODING_PCM_16BIT;	
				
	public static String CAST_TYPE = "null";
	public static String BROADCAST = "null";
	public static int PORT_AVAIBLE = -1;

	public static final String DISPLAY_MESSAGE_ACTION = "com.asesores.pushtotalk.DISPLAY_MESSAGE";
	public static final String EXTRA_MESSAGE = "message";
	  
	 public static void displayMessage(Context context, String message) {
		 
	        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
	        intent.putExtra(EXTRA_MESSAGE, message);
	        context.sendBroadcast(intent);
	    }


	public static final int RECORD_BUFFER_SIZE = Math.max(
			SAMPLE_RATE, 
			ceil(AudioRecord.getMinBufferSize(
					SAMPLE_RATE, 
					AudioFormat.CHANNEL_IN_MONO, 
					ENCODING_PCM_NUM_BITS)));
	public static final int TRACK_BUFFER_SIZE = Math.max(
			FRAME_SIZE, 
			ceil(AudioTrack.getMinBufferSize(
					SAMPLE_RATE, 
					AudioFormat.CHANNEL_OUT_MONO, 
					ENCODING_PCM_NUM_BITS)));			
	
	private static int ceil(int size) {
		return (int) Math.ceil( ( (double) size / FRAME_SIZE )) * FRAME_SIZE;
	}
		
}
