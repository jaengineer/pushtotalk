package com.asesores.pushtotalk.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import com.asesores.pushtotalk.Main;
import android.os.AsyncTask;
import android.util.Log;

public  class SendServer  extends AsyncTask<DatagramSocket, Integer, String>{
	
	private static String TAG = "UTILS";
	String mensaje = "";
	int port = 9000;

	public SendServer(String mensaje){
		this.mensaje = mensaje;
		}
	
	@Override
	protected String doInBackground(DatagramSocket... params) {
		
		DatagramSocket socket = params[0];
		String s1 = new String();
	
		if (mensaje.equals("server:Finish")) {
			s1 = mensaje;
		} else {
			s1 = "server:Welcome";
		}

		String s2 = new String(Main.usuario);
		String s3 = new String("End");
		String s4 = new String("RegId");
		String s5 = new String(mensaje);
	
		String salida = s1 + s2 + s3 + s4 + s5;

		DatagramPacket p = null;
		byte[] call = new String(salida).getBytes();

		InetAddress address = null;	
		
	
		if (mensaje.contains("IPRECIBO")) {
		    port = AudioParams.PORT_SERVER_BRODCAST;
		}
		
		try {
			address = InetAddress.getByName(AudioParams.SERVER_IP);
			
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		}

		p = new DatagramPacket(call, call.length,address, port);
	
		try {
			socket.send(p);
			
		} catch (IOException e) {
			Log.d(TAG, "CATCH: " + e.toString());
		}

		return null;
	}
}
