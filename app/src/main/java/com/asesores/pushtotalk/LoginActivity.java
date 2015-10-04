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
import com.asesores.pushtotalk.util.AudioParams;
import com.asesores.pushtotalk.util.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import static com.asesores.pushtotalk.util.AudioParams.SERVER_URL;


public class LoginActivity extends Activity {

	private EditText EditUser = null;
	private EditText EditPassword = null;
	private Button botonEntrar;
	private String mensajeAlerta = "Atención";
	private String mensajeErrorCampos = "Rellene los campos. ";
	private String mensajeErrorServer = "No se pudo establecer conexión con el servidor. ";
	private String mensajeErrorConexion = "No hay conexión a internet. ";
	private static String TAG = "LOGIN";
	String valueUser = "";
	String valuePass = "";
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		EditUser = (EditText) findViewById(R.id.EditUser);
		EditPassword = (EditText) findViewById(R.id.EditPassword);
		botonEntrar = (Button) findViewById(R.id.BotonEnviar);
		
		progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");

		botonEntrar.setOnClickListener(new OnClickListener() {
				
			public void onClick(View v) {
				
				/***** COMPRUEBA QUE HAY CONEXION (WIFI O 3G) *****/
				if (Utils.isOnline(LoginActivity.this)) {
					
					progressDialog.show();

				    valueUser = EditUser.getText().toString();
				    valuePass = EditPassword.getText().toString();

					/******* COMPRUEBA QUE USER Y PASSWORD NO ESTAN VACIOS *********/
					if ((valueUser.length() != 0) && (valuePass.length() != 0)) {
						
						 new sendValues().execute( valueUser, valuePass);
				
					} else {
						
						Utils.MensajeError(LoginActivity.this, mensajeAlerta, mensajeErrorCampos);			
					}
				} else {
					Utils.MensajeError(LoginActivity.this, mensajeAlerta, mensajeErrorConexion);
				}

			}

		});

	}

	

	 public void saveLoginPreference(String name_value) {
		 
		SharedPreferences prefs =  getSharedPreferences("GessanPTTPrefs", Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("estado", "online");
		editor.putString("nombre", name_value);
		editor.commit();	
	 }
	 
	 
	 /****** TAREA ASINCRONA QUE EJECUTA UN HILO SECUNDARIO Y CUYO RESULTADO LO PUBLICA EN NUESTRA INTERFAZ DE USUARIO 
	  EL PRIMER PARAMETRO ES UN ARRAY DE VALORES DE ENTRADA ***********/
	 class sendValues extends AsyncTask<String, Void, Integer> {

		 /******* TAREA QUE VA A EJECUTARSE EN SEGUNDO PLANO QUE TOMA COMA PARAMETRO EL ARRAY DE PARAMETROS DE ENTRADA DE NUESTRA CLASE *******/
         @Override
         protected Integer doInBackground(String... n) {
        	 
        	 int result = 0;
 	    	
 	    	String serverUrl = SERVER_URL + "/login";
 	        Map<String, String> params = new HashMap<String, String>();
 	        params.put("user", n[0]);
 	        params.put("password", n[1]);
 	        result = post(serverUrl, params);
 	        return result;
         }
         	
         	
         /******** TOMA COMO PARAMETRO EL RESULTADO QUE DEVUELVE EL METODO ANTERIOR MOSTRANDO UNA ALERTA CON EL RESULTADO **********/
         @Override
         protected void onPostExecute(Integer res) {
        	 
        	 if (progressDialog != null) {
                 progressDialog.dismiss();
             }
        	 
        	 Intent intent = new Intent(LoginActivity.this, Main.class);

               switch(res){
               case -1:
            	   Utils.MensajeError(LoginActivity.this, mensajeAlerta, mensajeErrorServer);
            	   break;
               case 200:
            	   	startActivity(intent);
					saveLoginPreference(valueUser);
            	   break;
               case 300:
            	   Utils.MensajeError(LoginActivity.this, TAG, "INCORRECTO");
            	   break;
               case 400:
            		Utils.MensajeError(LoginActivity.this, mensajeAlerta, mensajeErrorConexion);
            	   break;
            
               }
         }
         
         
         /********* PETICION POST HTTP AL SERVIDOR LOGIN *************/
         public int post(String endpoint, Map<String, String> params) {
 	        URL url;
 	        int status = 0;
 	        
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
 	             
 	             new Runnable(){
 	            	 public void run(){
 	             try {
 					AudioParams.conectados = br.readLine();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	            	 }
 	             };
 	             
 	           Log.d(TAG, "TEXT: " + "STATUS RESPUESTA: " + status + " " + br.readLine());
 	          
 	        } catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
 	            if (conn != null) {
 	                conn.disconnect();
 	            }
 	        }
 	        
 	        return status;
 	      }
         /*******************************************************************************/

   }
	
	 
	
}
