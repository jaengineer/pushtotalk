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

package com.asesores.pushtotalk.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.LinkedList;

import android.util.Log;

public class PhoneIPs {
	
	private static String TAG = "PHONEIPS";
	
	static private LinkedList<InetAddress> inetAddresses = new LinkedList<InetAddress>(); 

	public static void load() {
		inetAddresses.clear();
		try {
			Enumeration<NetworkInterface> networkInterfaceEnum = NetworkInterface.getNetworkInterfaces();
			
			while(networkInterfaceEnum.hasMoreElements()) {								
				Enumeration<InetAddress> inetAddresseEnum = networkInterfaceEnum.nextElement().getInetAddresses();
				
				while(inetAddresseEnum.hasMoreElements()) {
					inetAddresses.add(inetAddresseEnum.nextElement());
				}
			}
		}
		catch(IOException e) {
			Log.d(TAG, "CATCH: " + e.toString());
		}
	}
	
	public static boolean contains(InetAddress addr) {
		return inetAddresses.contains(addr);
	}
	
}
