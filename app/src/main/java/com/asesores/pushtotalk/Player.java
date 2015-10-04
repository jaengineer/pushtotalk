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


import com.asesores.pushtotalk.codecs.Speex;
import com.asesores.pushtotalk.settings.AudioSettings;
import com.asesores.pushtotalk.settings.CommSettings;
import com.asesores.pushtotalk.util.AudioParams;
import com.asesores.pushtotalk.util.SendServer;
import com.asesores.pushtotalk.util.SendServerUnicast;
import com.asesores.pushtotalk.util.Utils;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class Player extends Thread {

	/********* VARIABLES SOCKET PLAYER *************/
	public static DatagramSocket socket;
	private DatagramPacket packet;
	private static String TAG = "PLAYER";
	private final String messageIPRECIBO = "IPRECIBO";
	
	
	
	private final String messageGOODBYE = "ADIOS";
	/***********************************************/
	
	/********* VARIABLES PLAYER MEDIA **************/
	private AudioTrack player;
	private byte[] encodedFrame = new byte[AudioParams.FRAME_SIZE_IN_BYTES];
	private short[] pcmFrame = new short[AudioParams.FRAME_SIZE];
	/***********************************************/
	
	/********* VARIABLES AUXILIARES ****************/
	private boolean isRunning = true;
	private boolean isFinishing = false;
	private Handler mHandler = new Handler();
	private int progress = 0;
	/***********************************************/

	
	
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		while (!isFinishing()) {
			init();
			while (isRunning()) {

				try {

					socket.receive(packet);
					
			//	if (AudioSettings.useSpeex()){
					
			//			Speex.decode(encodedFrame, encodedFrame.length,pcmFrame);
			//			player.write(pcmFrame, 0, AudioParams.FRAME_SIZE);

				//	} else {
						player.write(encodedFrame, 0,AudioParams.FRAME_SIZE_IN_BYTES);
				//	}
						makeProgress();

					
				} catch (IOException e) {
					Log.d(TAG, "CATCH: " + e.toString());
				}	
				
			}

			release();
			synchronized (this) {
				try {
					if (!isFinishing())
						this.wait();
				} catch (InterruptedException e) {
					Log.d(TAG, "CATCH: " + e.toString());
				}
			}
		}
	}

	
	/******** SE ARRANCA AL CREAR EL THREAD DEL PLAYER, REPRODUCE EL AUDIO ENTRANTE *******/
	private void init() {
		try {
			socket = new DatagramSocket(CommSettings.PORT_CLIENT_RCV);

			player = new AudioTrack(AudioManager.STREAM_MUSIC,
					AudioParams.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
					AudioParams.ENCODING_PCM_NUM_BITS,
					AudioParams.TRACK_BUFFER_SIZE, AudioTrack.MODE_STREAM);


			mHandler.postDelayed(mMuestraMensaje, 10000);
			
			/***** ASYNCTASK PARA ENVIAR IPRECIBO Y EL ID AL SERVER ********************/
			SendServer  sendserver = new SendServer(AudioParams.regId + messageIPRECIBO);
		    sendserver.execute(socket);
		  
		    
		    /***** ASYNCTASK PARA ENVIAR IPRECIBO_UNICAST Y EL ID AL SERVER ********************/
			SendServerUnicast  sendserverUnicast = new SendServerUnicast(messageIPRECIBO,AudioParams.PORT_SERVER_UNICAST);
		    sendserverUnicast.execute(socket);
		
			packet = new DatagramPacket(encodedFrame, encodedFrame.length);
			player.play();

		} catch (IOException e) {
			Log.d(TAG, "CATCH: " + e.toString());
		}
	}
	/******************************************************************************************/
	
	
	private void release() {
		if (player != null) {
			socket.close();
			player.stop();
			player.release();
		}
	}

	
	private synchronized void makeProgress() {
		progress++;
	}

	public synchronized int getProgress() {

		return progress;
	}

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
		Log.i(TAG, "FINISH AUDIO");
		return isFinishing;
	}

	public synchronized void finish() {

		sayGoodbye();
		isRunning = false;
		isFinishing = true;
		this.notify();
	}
	

	public  synchronized void sayGoodbye() {		
		
		/**** ARRANCA UN ASYNCTASK PARA COMUNICAR AL SERVER DE QUE UN USUARIO SE VA **********/
		SendServer sendserver = new SendServer(messageGOODBYE);
		sendserver.execute(socket);
		
		/******** ASYNCTASK PARA ENVIAR AL SERVER IPRECIBO Y EL ID DEL TELEFONO ****/
		SendServerUnicast  sendserverUnicast = new SendServerUnicast(messageGOODBYE,AudioParams.PORT_SERVER_UNICAST);
		sendserverUnicast.execute(socket);
	}
	
	
	/*********** HILO PARA ENVIAR IPRECIBO AL SERVIDOR BROADCAST Y UNICAST CADA 5 SEGUNDOS *********/
	private Runnable mMuestraMensaje = new Runnable() {
		public void run() {
			if (!socket.isClosed()) {
				
				
				/******** ASYNCTASK PARA ENVIAR AL SERVER IPRECIBO Y EL ID DEL TELEFONO ****/
				SendServer  sendserver = new SendServer(AudioParams.regId + messageIPRECIBO + AudioParams.Latitud + "ENDLATITUD" + AudioParams.Longitud+"END");
				sendserver.execute(socket);
				
				/******** ASYNCTASK PARA ENVIAR AL SERVER IPRECIBO Y EL ID DEL TELEFONO ****/
				SendServerUnicast  sendserverUnicast = new SendServerUnicast(messageIPRECIBO,AudioParams.PORT_SERVER_UNICAST);
				sendserverUnicast.execute(socket);
				
				
			}

			mHandler.removeCallbacks(mMuestraMensaje);
		//	mHandler.postDelayed(this, 4000);
			mHandler.postDelayed(this, 20000);
		}
	};
	
	


}
