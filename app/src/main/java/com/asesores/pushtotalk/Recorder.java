
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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import com.asesores.pushtotalk.settings.CommSettings;
import com.asesores.pushtotalk.util.AudioParams;
import com.asesores.pushtotalk.util.PhoneIPs;
import com.asesores.pushtotalk.util.SendServer;
import com.asesores.pushtotalk.util.SendServerUnicast;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class Recorder extends Thread {

	/******* VARIABLES SOCKET RECORDER *********/
	private AudioRecord recorder;
	private byte[] encodedFrame;
//	private short[] pcmFrame = new short[AudioParams.FRAME_SIZE];
	private static String TAG = "RECORDER";
	private DatagramSocket socket;
	private DatagramPacket packet;
	/*******************************************/
	
	

	/******* VARIABLES AUXILIARES **************/
	private final int SO_TIMEOUT = 0;
	private boolean isRunning = false;
	private boolean isFinishing = false;
	private final String messageIPENVIO = "IPENVIO";
	private final String messageEND = "server:Finish";
	public static String cast_type = "";
	/*******************************************/
	
	public static boolean canalAbierto = false;
	
	Timer timer;
	 MyTimerTask myTimerTask;
	
	public void run() {
		
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		while (!isFinishing()) {
			init();
			while (isRunning()) {				

				try {
					
				/*	if (AudioSettings.useSpeex()) {
						recorder.read(pcmFrame, 0, AudioParams.FRAME_SIZE);
						Speex.encode(pcmFrame, encodedFrame);
					} else { */
						recorder.read(encodedFrame, 0, AudioParams.FRAME_SIZE_IN_BYTES);
				//	}
					socket.send(packet);
				} catch (IOException e) {
					Log.d(TAG, "CATCH:" + e.toString());
				}
			}

			release();


			synchronized (this) {
				try {
					if (!isFinishing())
						this.wait();
				} catch (InterruptedException e) {
					Log.d(TAG, "CATCH:" + e.toString());
				}
			}
		}
	}

	
	private void init(){
		try {
			
			PhoneIPs.load();
			socket = new DatagramSocket(CommSettings.PORT_CLIENT_SND);
			socket.setSoTimeout(SO_TIMEOUT);
	
			InetAddress addr = InetAddress.getByName(AudioParams.SERVER_IP);
			int port = 0;
			
			
			/******** BROADCAST *********************/
			if(AudioParams.CAST_TYPE.contains("null")){
				
				port = AudioParams.PORT_SERVER_BRODCAST; 
			
				/***** ASYNCTASK PARA ENVIAR IPENVIO Y ID  DEL TELEFONO AL SERVER *********/
				 SendServer  sendserver = new SendServer(messageIPENVIO);
				 sendserver.execute(socket);
			}
			/********* UNICAST ***********************/
			
			
			else{		
			
				port = AudioParams.MY_UNICAST_PORT;
			
				/***** ASYNCTASK PARA ENVIAR IPENVIO UNICAST Y ID  DEL TELEFONO AL SERVER *********/
				 SendServerUnicast  sendserverUnicast = new SendServerUnicast(messageIPENVIO,port);
				 sendserverUnicast.execute(socket);
			}

		/*	if (AudioSettings.useSpeex()) {
				encodedFrame = new byte[Speex.getEncodedSize(AudioSettings.getSpeexQuality())];
			} else {*/
				encodedFrame = new byte[AudioParams.FRAME_SIZE_IN_BYTES];
		//	}

			packet = new DatagramPacket(encodedFrame, encodedFrame.length,addr, port);

			recorder = new AudioRecord(AudioSource.MIC,
					AudioParams.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
					AudioParams.ENCODING_PCM_NUM_BITS,
					AudioParams.RECORD_BUFFER_SIZE);

			recorder.startRecording();
		} catch (SocketException e) {
			Log.d(TAG, "CATCH:" + e.toString());
		} catch (UnknownHostException e) {
			Log.d(TAG, "CATCH:" + e.toString());
		}

	}
	
	

	private void release() {
		if (recorder != null) {
			socket.close();
			recorder.stop();
			recorder.release();
		}
	}

	public synchronized boolean isRunning() {
		return isRunning;
	}

	public synchronized void resumeAudio() {
		isRunning = true;
		this.notify();
		
		timer = new Timer();
	    myTimerTask = new MyTimerTask();
	    timer.schedule(myTimerTask, 2000);
	}
	
	
	
	class MyTimerTask extends TimerTask {

		  public void run() {
			  Main.activarVoz = 0;
			  timer.cancel();
			 
		  }
		  
	}

	public synchronized void pauseAudio() {
		
		if(!canalAbierto){
		/******** BROADCAST ********************/
		if(AudioParams.CAST_TYPE.equals("null")){
		
			/********* ASYNCTASK PARA ENVIAR EL FINISH *******/
			SendServer  sendserver = new SendServer(messageEND);
			sendserver.execute(socket);
			
		}
		/******** UNICAST *********************/
		else{
	
			/********* ASYNCTASK PARA ENVIAR EL FINISH UNICAST*******/
			SendServerUnicast  sendserver = new SendServerUnicast(messageEND,AudioParams.PORT_SERVER_UNICAST);
			sendserver.execute(socket);
		}
		isRunning = false;
		
		}

	}

	public synchronized boolean isFinishing() {
		return isFinishing;
	}

	public synchronized void finish(){
		pauseAudio();
		isFinishing = true;
		this.notify();
	}
	
	
}
