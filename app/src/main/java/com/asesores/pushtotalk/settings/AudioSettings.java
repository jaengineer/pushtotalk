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

package com.asesores.pushtotalk.settings;

import com.asesores.pushtotalk.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class AudioSettings extends PreferenceActivity {
	
	private static boolean useSpeex;
	private static int speexQuality;
	private static boolean echoState;

	public static boolean USE_SPEEX = true;
	public static boolean ECHO_OFF = false;	
	
	public static boolean useBluetooth =false;
	public static boolean USE_BLUETOOTH = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_audio);		
	}	
	

	 //Update cache settings	 
	public static void getSettings(Context context) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Resources res = context.getResources();		
		
    	useSpeex = prefs.getBoolean("use_speex", USE_SPEEX);    		    		
    	speexQuality = Integer.parseInt(prefs.getString("speex_quality", res.getStringArray(R.array.speex_quality_values)[0]));
    	echoState = prefs.getBoolean("echo", ECHO_OFF);    	
    	
    	useBluetooth = prefs.getBoolean("use_bluetooth", USE_BLUETOOTH);
	}
	
	public static boolean useSpeex() {
		return useSpeex;
	}	

	public static int getSpeexQuality() {
		return speexQuality;
	}
	
	public static boolean getEchoState() {
		return echoState;
	}		
	
	/********* DEVUELVE EL ESTADO DEL BLUETOOTH: TRUE O FALSE *****/
	public static boolean useBluetooth() {
		return useBluetooth;
	}	
	/**************************************************************/

}
