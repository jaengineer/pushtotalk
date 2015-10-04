package com.asesores.pushtotalk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;


/********** ENCARGADO DE RECIBIR LOS MENSAJES, UTILIZAMOS EL TIPO WAKEBROADCASTRECIBER QUE SE ENCARGA DE ASEGURAR
 QUE EL DISPOSITIVO PERMANECERA DESPIERTO EL TIEMPO NECESARIO PARA QUE EL SERVICE QUE LANZAMOS PARA PROCESAR LOS MENSAJES
 TERMINE SU SERVICIO ******/
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver{
	
	private static String TAG = "BROADCASTRECEIVER";
	
	 @Override
	    public void onReceive(Context context, Intent intent) {
		 
		 Log.d(TAG, "TEXT: " + "ENTRA EN GCMBROADCASTRECEIVER: " + intent.getExtras().getString("message") );
		 
	        // Explicitly specify that GcmIntentService will handle the intent.
	        ComponentName comp = new ComponentName(context.getPackageName(), GCMIntentService.class.getName());
	        
	        // Start the service, keeping the device awake while it is launching.
	        
	        /****** CON startWakefulService INICIAMOS LA LLAMADA A NUESTRO SERVICIO DE MENSAJES GCMIntentService, con el metodo 
	        setComponent le decimos que servicio queremos que ejecute ***********/
	        startWakefulService(context, (intent.setComponent(comp)));
	        setResultCode(Activity.RESULT_OK);
	    }

	

}
