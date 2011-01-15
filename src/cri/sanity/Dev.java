package cri.sanity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
//import android.content.ContentResolver;
//import android.location.LocationManager;


public final class Dev
{
	public static final int VOLUME_CALL  = AudioManager.STREAM_VOICE_CALL;
	public static final int VOLUME_MEDIA = AudioManager.STREAM_MUSIC;
	public static final int VOLUME_ALARM = AudioManager.STREAM_ALARM;
	public static final int VOLUME_RING  = AudioManager.STREAM_RING;
	public static final int VOLUME_DTMF  = AudioManager.STREAM_DTMF;
	public static final int VOLUME_SYS   = AudioManager.STREAM_SYSTEM;

	private static Object iTelMan;
	private static int    screenTimeout = -1;
	//private static int    brightness    = -1;

	private Dev() { }

	//---- getting devices

	public static Sensor sensor(int type) {
		SensorManager sm = A.sensorMan();
		return sm==null ? null : sm.getDefaultSensor(type);
	}
	public static Sensor sensorProxim() { return sensor(Sensor.TYPE_PROXIMITY     ); }
	/*public static Sensor sensorOrient() { return sensor(Sensor.TYPE_ORIENTATION   ); }
	public static Sensor sensorAccel () { return sensor(Sensor.TYPE_ACCELEROMETER ); }
	public static Sensor sensorLight () { return sensor(Sensor.TYPE_LIGHT         ); }
	public static Sensor sensorMagnet() { return sensor(Sensor.TYPE_MAGNETIC_FIELD); }
	public static Sensor sensorTemper() { return sensor(Sensor.TYPE_TEMPERATURE   ); }
	public static Sensor sensorPress () { return sensor(Sensor.TYPE_PRESSURE      ); }
	public static Sensor sensorGyro  () { return sensor(Sensor.TYPE_GYROSCOPE     ); }*/

	//---- having devices?
	
	public static boolean haveProxim() { return sensorProxim() != null; }
	public static boolean haveWifi()   { return A.wifiMan()    != null; }
	public static boolean haveBt()     { return A.btAdapter()  != null; }
	public static boolean haveTel()    { return A.telMan()     != null; }
	//public static boolean haveLoc()    { return A.locMan()     != null; }

	//public static boolean allowMobData() { return Settings.Secure.getInt(A.ctxRes(), "mobile_data", 1) == 1; }

	//---- check on/off device state
	
	public static boolean isSpeakerOn() { return A.audioMan().isSpeakerphoneOn(); }
	public static boolean isHeadsetOn() {
		AudioManager am = A.audioMan();
		return am.isWiredHeadsetOn() || am.isBluetoothA2dpOn() || am.isBluetoothScoOn();
	}

	public static boolean isMobDataOn() {
    if(Settings.Secure.getInt(A.ctnRes(), "mobile_data", 1) != 1) return false;
		final int ds = A.telMan().getDataState();
		return ds==TelephonyManager.DATA_CONNECTED || ds==TelephonyManager.DATA_CONNECTING;
	}
	public static boolean isWifiOn() { return haveWifi() && A.wifiMan().isWifiEnabled(); }
	public static boolean isBtOn()   { return haveBt() && A.btAdapter().isEnabled(); }
	//public static boolean isGpsOn()  { return haveLoc() && A.locMan().isProviderEnabled(LocationManager.GPS_PROVIDER); }

	//---- enable/disable devices

	public static boolean enableMobData(boolean enable) {
		try {
			// use undocumented android api
			Object i = iTelMan();
			return ((Boolean)i.getClass().getMethod(enable ? "enableDataConnectivity" : "disableDataConnectivity").invoke(i)).booleanValue();
		}
		catch(Exception e) {
			return false;
		}
	}

	public static boolean enableWifi(boolean enable) { return A.wifiMan().setWifiEnabled(enable); }

	public static boolean enableBt(boolean enable) {
		if(!haveBt()) return false;
		return enable ? A.btAdapter().enable() : A.btAdapter().disable();
	}
	
	/*public static boolean enableGps(boolean enable) {
		// this method is extracted from "Quick Settings" opensource project (http://code.google.com/p/quick-settings) by beworker
		final ContentResolver resolver = A.ctnRes();
		final String providers         = Settings.Secure.getString(resolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		final int i                    = providers.indexOf(LocationManager.GPS_PROVIDER);
		String newProviders            = null;
		if(enable) {
			// add provider to the list
			if(i < 0) {
				// GPS is off and we need to add GPS provider
				newProviders = providers;
				if(providers.length() != 0)
					newProviders += ',';
				newProviders += LocationManager.GPS_PROVIDER;
			} // else provider is already in the list
		}
		else {
			// remove provider from the list
			if(i >= 0) { // provider is in the list
				newProviders = providers.substring(0, i);
				int comma = providers.indexOf(',', i);
				if(comma >= 0)
					newProviders += providers.substring(comma + 1);
				if(newProviders.endsWith(","))
					newProviders = newProviders.substring(0, newProviders.length() - 1);
			}
		}
		if(newProviders == null) return true;
		try {
			Settings.Secure.putString(A.ctnRes(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newProviders);
			A.logd("enableGps: "+enable+", providers: "+newProviders);
			return true;
		} catch(SecurityException e) {
			A.logd("unable to set Secure Settings for GPS!");
			return false;
		}
	}*/

	public static void enableSpeaker(boolean enable) { A.audioMan().setSpeakerphoneOn(enable); }

	//---- volume

	public static int  getVolume   (int type) { return A.audioMan().getStreamVolume   (type); }
	public static int  getVolumeMax(int type) { return A.audioMan().getStreamMaxVolume(type); }

	public static void setVolume(int type, int vol) { A.audioMan().setStreamVolume(type, vol               , 0); }
	public static void setVolumeMax(int type)       { A.audioMan().setStreamVolume(type, getVolumeMax(type), 0); }

	//---- managing system

	public static int getSysInt(String key) {
		try {
			return System.getInt(A.ctnRes(), key);
		} catch(SettingNotFoundException e) {
			return -1; 
		}
	}
	public static void setSysInt(String key, int val) {
		System.putInt(A.ctnRes(), key, val);
		System.putInt(A.ctnRes(), key, val);
	}

	public static int getScreenOffTimeout() {
		return getSysInt(System.SCREEN_OFF_TIMEOUT);
	}
	public static void setScreenOffTimeout(int timeout) {
		if(screenTimeout < 0) screenTimeout = getScreenOffTimeout();
		setSysInt(System.SCREEN_OFF_TIMEOUT, timeout);
	}
	public static void restoreScreenTimeout() {
		if(screenTimeout < 0) return;
		setScreenOffTimeout(screenTimeout);
	}

	/*public static int getBrightness() {
		return getSysInt(System.SCREEN_BRIGHTNESS);
	}
	public static void setBrightness(int b) {
		if(brightness < 0) brightness = getBrightness();
		setSysInt(System.SCREEN_BRIGHTNESS, b);
	}
	public static void restoreBrightness() {
		if(brightness < 0) return;
		setBrightness(brightness);
	}
	
	public static void dimScreen(boolean dim) {
		setSysInt(System.DIM_SCREEN, dim? 1 : 0);
	}*/
	
	//---- undocumented android api

	private static Object iTelMan() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if(iTelMan != null) return iTelMan;
		TelephonyManager tm = A.telMan();
		Method itm = tm.getClass().getDeclaredMethod("getITelephony");
		if(!itm.isAccessible()) itm.setAccessible(true);
		return iTelMan = itm.invoke(tm);
	}

}
