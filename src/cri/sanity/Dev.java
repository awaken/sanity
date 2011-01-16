package cri.sanity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
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
	private static final Uri uriGps = Uri.parse("3");


	private Dev() { }

	//---- getting devices

	public static final Sensor sensor(int type) {
		SensorManager sm = A.sensorMan();
		return sm==null ? null : sm.getDefaultSensor(type);
	}
	public static final Sensor sensorProxim() { return sensor(Sensor.TYPE_PROXIMITY     ); }
	/*public static final Sensor sensorOrient() { return sensor(Sensor.TYPE_ORIENTATION   ); }
	public static final Sensor sensorAccel () { return sensor(Sensor.TYPE_ACCELEROMETER ); }
	public static final Sensor sensorLight () { return sensor(Sensor.TYPE_LIGHT         ); }
	public static final Sensor sensorMagnet() { return sensor(Sensor.TYPE_MAGNETIC_FIELD); }
	public static final Sensor sensorTemper() { return sensor(Sensor.TYPE_TEMPERATURE   ); }
	public static final Sensor sensorPress () { return sensor(Sensor.TYPE_PRESSURE      ); }
	public static final Sensor sensorGyro  () { return sensor(Sensor.TYPE_GYROSCOPE     ); }*/

	//---- having devices?
	
	public static final boolean haveProxim() { return sensorProxim() != null; }
	public static final boolean haveWifi()   { return A.wifiMan()    != null; }
	public static final boolean haveBt()     { return A.btAdapter()  != null; }
	public static final boolean haveTel()    { return A.telMan()     != null; }
	public static final boolean haveLoc()    { return A.locMan()     != null; }

	//public static final boolean allowMobData() { return Settings.Secure.getInt(A.ctxRes(), "mobile_data", 1) == 1; }

	//---- check on/off device state
	
	public static final boolean isSpeakerOn() { return A.audioMan().isSpeakerphoneOn(); }
	public static final boolean isHeadsetOn() {
		AudioManager am = A.audioMan();
		return am.isWiredHeadsetOn() || am.isBluetoothA2dpOn() || am.isBluetoothScoOn();
	}

	public static final boolean isMobDataOn() {
    if(Settings.Secure.getInt(A.ctnRes(), "mobile_data", 1) != 1) return false;
		final int ds = A.telMan().getDataState();
		return ds==TelephonyManager.DATA_CONNECTED || ds==TelephonyManager.DATA_CONNECTING;
	}
	public static final boolean isWifiOn() { return haveWifi() && A.wifiMan().isWifiEnabled(); }
	public static final boolean isBtOn()   { return haveBt() && A.btAdapter().isEnabled(); }
	public static final boolean isGpsOn()  { return haveLoc() && A.locMan().isProviderEnabled(LocationManager.GPS_PROVIDER); }

	//---- enable/disable devices

	public static final boolean enableMobData(boolean enable) {
		try {
			// use undocumented android api
			Object i = iTelMan();
			return ((Boolean)i.getClass().getMethod(enable ? "enableDataConnectivity" : "disableDataConnectivity").invoke(i)).booleanValue();
		}
		catch(Exception e) {
			A.logd("unable to "+(enable?"enable":"disable")+" mobile data: undocumented api failed ("+e+')');
			return false;
		}
	}

	public static final boolean enableWifi(boolean enable) { return A.wifiMan().setWifiEnabled(enable); }

	public static final boolean enableBt(boolean enable) {
		if(!haveBt()) return false;
		return enable ? A.btAdapter().enable() : A.btAdapter().disable();
	}
	
	/*
	public static final boolean enableGps(boolean enable) {
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
	}
	*/

	public static final void toggleGps() {
    Intent i = new Intent();
    i.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
    i.addCategory("android.intent.category.ALTERNATIVE");
    i.setData(uriGps);
    A.app().sendBroadcast(i);
	}

	public static final void enableSpeaker(boolean enable) { A.audioMan().setSpeakerphoneOn(enable); }

	//---- volume

	public static final int  getVolume   (int type) { return A.audioMan().getStreamVolume   (type); }
	public static final int  getVolumeMax(int type) { return A.audioMan().getStreamMaxVolume(type); }

	public static final void setVolume(int type, int vol) { A.audioMan().setStreamVolume(type, vol               , 0); }
	public static final void setVolumeMax(int type)       { A.audioMan().setStreamVolume(type, getVolumeMax(type), 0); }

	//---- managing system

	public static final int getSysInt(String key) {
		try {
			return System.getInt(A.ctnRes(), key);
		} catch(SettingNotFoundException e) {
			return -1; 
		}
	}
	public static final void setSysInt(String key, int val) {
		System.putInt(A.ctnRes(), key, val);
		System.putInt(A.ctnRes(), key, val);
	}

	public static final int getScreenOffTimeout() {
		return getSysInt(System.SCREEN_OFF_TIMEOUT);
	}
	public static final void setScreenOffTimeout(int timeout) {
		if(screenTimeout < 0) screenTimeout = getScreenOffTimeout();
		setSysInt(System.SCREEN_OFF_TIMEOUT, timeout);
	}
	public static final void restoreScreenTimeout() {
		if(screenTimeout < 0) return;
		setScreenOffTimeout(screenTimeout);
	}

	/*
	public static final int getBrightness() {
		return getSysInt(System.SCREEN_BRIGHTNESS);
	}
	public static final void setBrightness(int b) {
		if(brightness < 0) brightness = getBrightness();
		setSysInt(System.SCREEN_BRIGHTNESS, b);
	}
	public static final void restoreBrightness() {
		if(brightness < 0) return;
		setBrightness(brightness);
	}
	
	public static final void dimScreen(boolean dim) {
		setSysInt(System.DIM_SCREEN, dim? 1 : 0);
	}
	*/
	
	//---- undocumented android api

	private static final Object iTelMan() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if(iTelMan != null) return iTelMan;
		TelephonyManager tm = A.telMan();
		Method itm = tm.getClass().getDeclaredMethod("getITelephony");
		if(!itm.isAccessible()) itm.setAccessible(true);
		return iTelMan = itm.invoke(tm);
	}

}
