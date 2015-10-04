package com.asesores.pushtotalk;

import static com.asesores.pushtotalk.util.AudioParams.SERVER_URL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.asesores.pushtotalk.chat.ChatMessage;
import com.asesores.pushtotalk.Main.SendServerAvailable;
import com.asesores.pushtotalk.util.AudioParams;
import com.asesores.pushtotalk.util.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class SendPosicion extends android.support.v4.app.FragmentActivity{


	private GoogleMap mapa = null;
	private int vista = 0;
	private static SendPosicion instance;
	private String lat = null;
	private String lon = null;
	private String user = null;
	private static String TAG = "SENDPOSICION";
	private boolean flag= false;
	public Vibrator vibrator;
	
	
	/*** DEVUELVE LA INSTANCIA **********/
    public static SendPosicion getInstance() {
        return instance;
    }
    
    public static void start(Context context, Bundle bundle) {
        Intent intent = new Intent(context, SendPosicion.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
      //  Log.d(TAG, "TEXT: " + "ENTRA EN START");
    }
    
    
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sendposicion);
    
    //TODAS LAS OPERACIONES LAS HACEMOS DE FORMA DIRECTA SOBRE UN OBJETO GOOGLEMAP QUE HEMOS CREADO, QUE ACCEDEMOS MEDIANTE EL METODO GETMAP()
    mapa = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.sendposicion)).getMap();
    
  
    lat = getIntent().getStringExtra("latitud");
    lon = getIntent().getStringExtra("longitud");
    user = getIntent().getStringExtra("destino");
        
    if(lat!=null){
    	
    		CameraUpdateFactory.zoomTo(19);
    		CameraUpdate camUpd2 = CameraUpdateFactory.newLatLngZoom(new LatLng( Double.parseDouble(lat),Double.parseDouble(lon)), 16F);
			mapa.animateCamera(camUpd2);
			mostrarMarcador(Double.parseDouble(lat),Double.parseDouble(lon));
    }
    else{
    	if(!AudioParams.Latitud.equals("sin_datos")){
    		user = Main.usuario;
    		
    	CameraUpdateFactory.zoomTo(19);
    	CameraUpdate camUpd2 = CameraUpdateFactory.newLatLngZoom(new LatLng( Double.parseDouble(AudioParams.Latitud),Double.parseDouble(AudioParams.Longitud)), 16F);
    	mapa.animateCamera(camUpd2);
   		mostrarMarcador(Double.parseDouble(AudioParams.Latitud),Double.parseDouble(AudioParams.Longitud));
    			
    	SendUbicacionServer  sendserver = new SendUbicacionServer();
   	    sendserver.execute();
    	
    	}
    }
  
    
    }
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.sendposicion, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		
		/*	case R.id.menu_vista:
				alternarVista();
				break;
			case R.id.menu_mover:
				//Centramos el mapa en España
				CameraUpdate camUpd1 = 
			//		CameraUpdateFactory.newLatLng(new LatLng(40.41, -3.69));
						CameraUpdateFactory.newLatLng(new LatLng( Double.parseDouble(lat),Double.parseDouble(lon)));
				mapa.moveCamera(camUpd1);
				mostrarMarcador(Double.parseDouble(lat),Double.parseDouble(lon));
				break;*/
		/*	case R.id.menu_animar:
				//Centramos el mapa en España y con nivel de zoom 5
				
				//	CameraUpdateFactory.newLatLngZoom(new LatLng(40.41, -3.69), 5F);
						if(lat!=null){
							CameraUpdate camUpd2 = 	CameraUpdateFactory.newLatLngZoom(new LatLng( Double.parseDouble(lat),Double.parseDouble(lon)), 5F);
				mapa.animateCamera(camUpd2);
				mostrarMarcador(Double.parseDouble(lat),Double.parseDouble(lon));
						}
						else{
							CameraUpdate camUpd2 = 	CameraUpdateFactory.newLatLngZoom(new LatLng( Double.parseDouble(AudioParams.Latitud),Double.parseDouble(AudioParams.Longitud)), 5F);
							mapa.animateCamera(camUpd2);
							mostrarMarcador(Double.parseDouble(AudioParams.Latitud),Double.parseDouble(AudioParams.Longitud));
						}
				break;*/
			case R.id.menu_3d:
				LatLng madrid;
			//	LatLng madrid = new LatLng(40.417325, -3.683081);
				if(lat!=null){
			 madrid =new LatLng( Double.parseDouble(lat),Double.parseDouble(lon));
				}
				else{
				madrid =new LatLng( Double.parseDouble(AudioParams.Latitud),Double.parseDouble(AudioParams.Longitud));
				}
				CameraPosition camPos = new CameraPosition.Builder()
					    .target(madrid)   //Centramos el mapa en Madrid
					    .zoom(19)         //Establecemos el zoom en 19
					    .bearing(45)      //Establecemos la orientaci�n con el noreste arriba
					    .tilt(70)         //Bajamos el punto de vista de la c�mara 70 grados
					    .build();

				CameraUpdate camUpd3 = 
						CameraUpdateFactory.newCameraPosition(camPos);

				mapa.animateCamera(camUpd3);
				
				if(lat!=null)
				mostrarMarcador(Double.parseDouble(lat),Double.parseDouble(lon));
				else
					mostrarMarcador(Double.parseDouble(AudioParams.Latitud),Double.parseDouble(AudioParams.Longitud));
					
				break;
			/*case R.id.menu_posicion:
				CameraPosition camPos2 = mapa.getCameraPosition();
				LatLng pos = camPos2.target;
				Toast.makeText(SendPosicion.this, 
						"Lat: " + pos.latitude + " - Lng: " + pos.longitude, 
						Toast.LENGTH_LONG).show();
				

				break;*/
				
			case R.id.sendposicion:
				SendUbicacionServer  sendserver = new SendUbicacionServer();
			    sendserver.execute();
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	
	private void mostrarMarcador(double lat, double lng){
	/*	mapa.addMarker(new MarkerOptions()
        .position(new LatLng(lat, lng))
        .title("Usuario: " + user));*/
		mapa.addMarker(new MarkerOptions().position(new LatLng(lat, lng)) .title("Usuario: " + user)).isVisible();
	}
	
			
	
	private void alternarVista(){
	    vista = (vista + 1) % 4;
	 
	    switch(vista)
	    {
	        case 0:
	            mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	            break;
	        case 1:
	            mapa.setMapType(GoogleMap.MAP_TYPE_HYBRID);
	            break;
	        case 2:
	            mapa.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
	            break;
	        case 3:
	            mapa.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
	        case 4:
	            break;
	    }
	}
	
	
	  /******************* ENVIO DE UBICACION AL SERVIDOR ********************/
    class SendUbicacionServer  extends AsyncTask<String, Void, Integer> {
    	
    
    	 /******* TAREA QUE VA A EJECUTARSE EN SEGUNDO PLANO QUE TOMA COMA PARAMETRO EL ARRAY DE PARAMETROS DE ENTRADA DE NUESTRA CLASE *******/
        @Override
        protected Integer doInBackground(String... n) {
        	
        	int result = 0;
    		String serverUrl = SERVER_URL + "/sendPosicion";
    		
    		 Map<String, String> parametros = new HashMap<String, String>();
    		 parametros.put("user", Main.usuario);
    	     parametros.put("latitud", AudioParams.Latitud);
    	     parametros.put("longitud",AudioParams.Longitud);
    	     
    	     result = post(serverUrl, parametros);
        
    		return result;
    		/*********/
    		
    		
    	}
    	
    	  protected void onPostExecute(Integer res) {
    		  
    		
                switch(res){
                
                case -1:
             	   Utils.MensajeError(SendPosicion.this, "Atenci�n", "Error de red");
             	   break;
             	   
                case 200:
                	
                	  Toast.makeText(SendPosicion.this, "Posicion enviada correctamente", Toast.LENGTH_LONG).show();
                	  vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                	  vibrator.vibrate(3000);
                	  break;
                }
    	  }
    }
    	  
	
	 /********* PETICION POST HTTP AL SERVIDOR SEND POSICION *************/
    public int post(String endpoint, Map<String, String> params)
	            {
	        URL url;
	        int status = -1;
	    
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
