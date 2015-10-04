package com.asesores.pushtotalk;

import static com.asesores.pushtotalk.util.AudioParams.SERVER_URL;


import java.io.IOException;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.asesores.pushtotalk.chat.ChatAdapter;
import com.asesores.pushtotalk.chat.ChatMessage;
import com.asesores.pushtotalk.R;
import com.asesores.pushtotalk.LoginActivity.sendValues;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class ChatActivity extends FragmentActivity{
	
	String destino = "";
	String lastMsg = "";
	private ListView contenedorMensajes;
	private EditText EditMensaje = null;
	private ChatAdapter adapter;
	public static final String EXTRA_MODE = "mode";
	public static final String EXTRA_TEXTO = "com.asesores.pushtotalk.EXTRA_TEXTO";
	private static final String TAG = "CHATACTIVITY";
	private static ChatActivity instance;
	TextView nombreDestino;
	
 	
	/*** DEVUELVE LA INSTANCIA **********/
    public static ChatActivity getInstance() {
        return instance;
    }
	
    
	public static void start(Context context, Bundle bundle) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
        Log.d(TAG, "TEXT: " + "ENTRA EN START");
    }
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		Bundle bundle = this.getIntent().getExtras();
	
	    destino = bundle.getString("usuario");
        destino = "broadcast";
	    init();

	    instance = this;
	    
	    /************ A�ADE MENSAJES A LA LISTA ************/
        String message = getIntent().getStringExtra("message");
        String destino = getIntent().getStringExtra("destino");
        if (message != null) {
            retrieveMessage(message,destino);
        }
	}
	
	
	/****** MUESTRA MENSAJE RECIBIDO ****************************/
    public void retrieveMessage(final String message,String destino) {
    	showMessage(new ChatMessage(message, Calendar.getInstance().getTime(), true));
    	this.destino = destino;
    	nombreDestino.setText(destino);
    }
	
	
	private void init(){
		
		contenedorMensajes = (ListView) findViewById(R.id.contenedorMensajes);
		EditMensaje = (EditText) findViewById(R.id.messageEdit);
		Button sendButton = (Button) findViewById(R.id.chatSendButton);
		TextView user = (TextView) findViewById(R.id.meLabel);
	    nombreDestino = (TextView) findViewById(R.id.nombreDestino);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        
        user.setText(Main.usuario);
        nombreDestino.setText(destino);
        
        adapter = new ChatAdapter(this, new ArrayList<ChatMessage>());
        contenedorMensajes.setAdapter(adapter);
        
      
        
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                 lastMsg = EditMensaje.getText().toString();
                if (TextUtils.isEmpty(lastMsg)) {
                    return;
                }

                EditMensaje.setText("");
            
                showMessage(new ChatMessage(lastMsg, Calendar.getInstance().getTime(), false));
                new sendMessage().execute(destino, lastMsg);
            }
        });
	}
	
	
	
	public void showMessage(ChatMessage message) {
		adapter.add(message);
		adapter.notifyDataSetChanged();
		scrollDown();
		
		 }
	
	public void showMessage(List<ChatMessage> messages) {
        adapter.add(messages);
        adapter.notifyDataSetChanged();
        scrollDown();
    }
	

   private void scrollDown() {
		contenedorMensajes.setSelection(contenedorMensajes.getCount() - 1);
	}
   
  
		
	
	/******* MANDAMOS EL MENSAJE EN SEGUNDO PLANO ************/
	 class sendMessage extends AsyncTask<String, Void, Integer> {
		 
		 @Override
         protected Integer doInBackground(String... n) {
			 int result = 0;
			 String serverUrl = SERVER_URL + "/chat";
		        Map<String, String> params = new HashMap<String, String>();
		        params.put("user", n[0]);
		        params.put("mensaje", n[1]);
		        params.put("origen",Main.usuario);
		        
		        try {
		             result = post(serverUrl, params);
		        } catch (IOException e) {
		        	return -1;
		        }
		        
		        return result;
         }
		 
		 /*** INTERPPRETA LOS RESULTADOS DEL METODO ANTERIOR *******/
		 @Override
         protected void onPostExecute(Integer res) {
			 
		 }
		 
	 }
	
	
	public static int post(String endpoint, Map<String, String> params)
            throws IOException {
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
   //     Log.v(TAG, "Posting '" + body + "' to " + url);
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
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            
            // handle the response
            status = conn.getResponseCode();
          
            
         //    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
           
        //   String res =  conn.getRequestProperty();
        //    String param = conn.getRequestMethod();
             
        //   Log.d(TAG, "TEXT: " + "STATUS RESPUESTA: " + status + " " + br.readLine());
           // if (status != 200) {
             // throw new IOException("Post failed with error code " + status);
           // }
        } finally {
            if (conn != null) {
                conn.disconnect();
                
              
            }
        }
        
        return status;
      }
	
	

}
