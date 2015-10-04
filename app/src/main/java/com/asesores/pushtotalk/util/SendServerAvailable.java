package com.asesores.pushtotalk.util;

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

import android.os.AsyncTask;
import android.util.Log;


class SendServerAvailable  extends AsyncTask<String, Void, Integer> {
	private static String TAG = "SERVER UNICAST";
	
	
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
		 
	  }
	  
	  
	  /********* PETICION POST HTTP AL SERVIDOR LOGIN *************/
      public int post(String endpoint, Map<String, String> params) {
	        URL url;
	        int status = 0;
	        String cadena = null;
	        boolean server_status = false;
	        int PuertoUnicast = 9001;
	        
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
						server_status = true;
						
						 new Runnable(){
			            	 public void run(){
			            		 
			            		 AudioParams.MY_UNICAST_PORT = Integer.parseInt(m.group(1));
			            		 
			            	 }
			             };
					}
					else{
						server_status = false;
					}
				  
	             Log.d(TAG, "TEXT: " + "STATUS RESPUESTA: " + m.group(1));
	      
	          
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


/****************************/