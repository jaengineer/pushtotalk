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

import java.net.InetAddress;
import java.net.UnknownHostException;
import com.asesores.pushtotalk.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;


public class CommSettings extends PreferenceActivity {
	
	private static InetAddress broadcastAddr;
	private static int broadcastPort;
	private static InetAddress unicastAddr;	
	private static int unicastPort;	
	public static final int PORT_CLIENT_RCV = 55555;
	public static final int PORT_CLIENT_SND = 55556;
	public static final int PORT_CLIENT_DATOS = 55558;
	private static String TAG = "COMMSETTINGS";
	private static ListPreference listPreference ;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_comm);			

		listPreference = (ListPreference) findPreference("list_users");
        setListPreferenceData(listPreference);
        listPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
           
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				int valor = Integer.valueOf(String.valueOf(newValue));			
				return true;
			}
        });
	
	}	
	 
	protected static void setListPreferenceData(ListPreference listPreference) {	
	}
	
	
	//Update cache settings
	public static void getSettings(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Resources res = context.getResources();
		
		try {
    		
    		broadcastAddr = InetAddress.getByName(prefs.getString("broadcast_addr", res.getString(R.string.broadcast_addr_default)));	
    		broadcastPort = Integer.parseInt(prefs.getString("broadcast_port", res.getString(R.string.broadcast_port_default)));    		
    		unicastAddr = InetAddress.getByName(prefs.getString("unicast_addr", res.getString(R.string.unicast_addr_default)));
    		unicastPort = Integer.parseInt(prefs.getString("unicast_port", res.getString(R.string.unicast_port_default)));
    		
		}
		catch(UnknownHostException e) {
			Log.d(TAG, "CATCH: " + e.toString());
		}
	}
	

		
	public static InetAddress getBroadcastAddr() {
		return broadcastAddr;
	}	
	
	public static int getBroadcastPort() {
		return broadcastPort;
	}	
	
	public static InetAddress getUnicastAddr() {
		return unicastAddr;
	}	
	
	public static int getUnicastPort() {
		return unicastPort;
	}	
	




}
