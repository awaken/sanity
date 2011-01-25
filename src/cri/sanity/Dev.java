package cri.sanity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
//import android.net.wifi.WifiConfiguration;
//import android.content.ContentResolver;
import android.app.KeyguardManager.KeyguardLock;


public final class Dev
{
	public static final int VOL_CALL   = AudioManager.STREAM_VOICE_CALL;
	public static final int VOL_MEDIA  = AudioManager.STREAM_MUSIC;
	public static final int VOL_ALARM  = AudioManager.STREAM_ALARM;
	public static final int VOL_NOTIFY = AudioManager.STREAM_NOTIFICATION;
	public static final int VOL_RING   = AudioManager.STREAM_RING;
	public static final int VOL_DTMF   = AudioManager.STREAM_DTMF;
	public static final int VOL_SYS    = AudioManager.STREAM_SYSTEM;
	public static final int VOL_DEF    = AudioManager.USE_DEFAULT_STREAM_TYPE;
	public static final int FLAG_VOL_SHOW     = AudioManager.FLAG_SHOW_UI;
	public static final int FLAG_VOL_PLAY     = AudioManager.FLAG_PLAY_SOUND;
	public static final int FLAG_VOL_VIBRATE  = AudioManager.FLAG_VIBRATE;
	public static final int FLAG_VOL_RING     = AudioManager.FLAG_ALLOW_RINGER_MODES;
	public static final int FLAG_VOL_REMOVE   = AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE;
	public static final int HOTSPOT_DISABLING = 0;
	public static final int HOTSPOT_DISABLED  = 1;
	public static final int HOTSPOT_ENABLING  = 2;
	public static final int HOTSPOT_ENABLED   = 3;
	public static final int HOTSPOT_FAILED    = 4;

	private static final Uri URI_GPS = Uri.parse("3");

	public static int defVolFlags = 0;

	private static Object iTelMan;
	private static int    screenTimeoutBak = -1;
	//private static int    brightnessBak    = -1;
	//private static PowerManager.WakeLock wakeCpuLock;
	private static PowerManager.WakeLock wakeScreenLock;
	private static KeyguardLock keyguardLock;

	private Dev() { }

	//---- getting devices

	public static final Sensor sensor(int type) {
		final SensorManager sm = A.sensorMan();
		return sm==null ? null : sm.getDefaultSensor(type);
	}
	public static final Sensor sensorProxim() { return sensor(Sensor.TYPE_PROXIMITY     ); }
	/*
	public static final Sensor sensorOrient() { return sensor(Sensor.TYPE_ORIENTATION   ); }
	public static final Sensor sensorAccel () { return sensor(Sensor.TYPE_ACCELEROMETER ); }
	public static final Sensor sensorLight () { return sensor(Sensor.TYPE_LIGHT         ); }
	public static final Sensor sensorMagnet() { return sensor(Sensor.TYPE_MAGNETIC_FIELD); }
	public static final Sensor sensorTemper() { return sensor(Sensor.TYPE_TEMPERATURE   ); }
	public static final Sensor sensorPress () { return sensor(Sensor.TYPE_PRESSURE      ); }
	public static final Sensor sensorGyro  () { return sensor(Sensor.TYPE_GYROSCOPE     ); }
	*/

	//---- having devices?
	
	public static final boolean haveWifi() { return A.wifiMan()   != null; }
	public static final boolean haveBt()   { return A.btAdapter() != null; }
	public static final boolean haveTel()  { return A.telMan()    != null; }
	public static final boolean haveLoc()  { return A.locMan()    != null; }

	//public static final boolean allowMobData() { return Settings.Secure.getInt(A.ctxRes(), "mobile_data", 1) == 1; }

	//---- check on/off device state
	
	public static final boolean isSpeakerOn() { return A.audioMan().isSpeakerphoneOn(); }
	public static final boolean isHeadsetOn() {
		final AudioManager am = A.audioMan();
		return am!=null && (am.isWiredHeadsetOn() || am.isBluetoothA2dpOn() || am.isBluetoothScoOn());
	}

	public static final boolean isMobDataOn() {
    if(Settings.Secure.getInt(A.ctnRes(), "mobile_data",1) != 1) return false;
		final int ds = A.telMan().getDataState();
		return ds==TelephonyManager.DATA_CONNECTED || ds==TelephonyManager.DATA_CONNECTING;
	}
	public static final boolean isWifiOn() { return haveWifi() && A.wifiMan().isWifiEnabled(); }
	public static final boolean isBtOn()   { return haveBt() && A.btAdapter().isEnabled(); }
	public static final boolean isGpsOn()  { return haveLoc() && A.locMan().isProviderEnabled(LocationManager.GPS_PROVIDER); }
	
	//public static final boolean isScreenOn() { return A.powerMan().isScreenOn(); }

	public static final boolean isHotspotOn() {
		try                { return _isHotspotOn(); }
		catch(Exception e) { return false;          }
	}
	public static final boolean isHotspotSupported() {
		try                { _isHotspotOn(); return true;  }
		catch(Exception e) {                 return false; }
	}
	public static final boolean isTetheringOn() {
		try                { return _isTetheringOn(); }
		catch(Exception e) { return false;            }
	}
	public static final boolean isTetheringSupported() {
		try                { _isTetheringOn(); return true;  }
		catch(Exception e) {                   return false; }
	}

	//---- enable/disable devices

	public static final boolean enableMobData(boolean enable) {
		try {
			// use undocumented android api
			final Object itm = iTelMan();
			return (Boolean)itm.getClass().getMethod(enable ? "enableDataConnectivity" : "disableDataConnectivity").invoke(itm);
		}
		catch(Exception e) {
			//A.logd("unable to "+(enable?"enable":"disable")+" mobile data: undocumented api failed ("+e+')');
			return false;
		}
	}

	public static final boolean enableWifi(boolean enable) {
		return A.wifiMan().setWifiEnabled(enable);
	}

	public static final boolean enableBt(boolean enable) {
		final BluetoothAdapter bt = A.btAdapter();
		if(bt == null) return false;
		return enable ? bt.enable() : bt.disable();
	}

	public static final void toggleGps() {
		// use undocumented android broadcast
    final Intent i = new Intent();
    i.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
    i.addCategory("android.intent.category.ALTERNATIVE");
    i.setData(URI_GPS);
    A.app().sendBroadcast(i);
	}

	public static final void enableSpeaker(boolean enable) { A.audioMan().setSpeakerphoneOn(enable); }

	//---- volume

	public static final int  getVolume   (int type) { return A.audioMan().getStreamVolume   (type); }
	public static final int  getVolumeMax(int type) { return A.audioMan().getStreamMaxVolume(type); }

	public static final void setVolume(int type, int vol)            { A.audioMan().setStreamVolume(type, vol, defVolFlags); }
	public static final void setVolume(int type, int vol, int flags) { A.audioMan().setStreamVolume(type, vol, flags); }
	public static final void setVolumeMax(int type)            { A.audioMan().setStreamVolume(type, getVolumeMax(type), defVolFlags); }
	public static final void setVolumeMax(int type, int flags) { A.audioMan().setStreamVolume(type, getVolumeMax(type), flags); }

	public static final void mute(int type, boolean enable) { A.audioMan().setStreamMute(type, enable); }
	//public static final void solo(int type, boolean enable) { A.audioMan().setStreamSolo(type, enable); }

	//---- managing system

	public static final int getSysInt(String key) {
		try {
			return System.getInt(A.ctnRes(), key);
		} catch(SettingNotFoundException e) {
			return -1; 
		}
	}
	public static final void setSysInt(String key, int val) {
		final ContentResolver cr = A.ctnRes();
		System.putInt(cr, key, val);
		System.putInt(cr, key, val);
	}

	public static final int getScreenOffTimeout() {
		return getSysInt(System.SCREEN_OFF_TIMEOUT);
	}
	public static final void setScreenOffTimeout(int timeout) {
		if(screenTimeoutBak < 0) screenTimeoutBak = getScreenOffTimeout();
		setSysInt(System.SCREEN_OFF_TIMEOUT, timeout);
	}
	public static final void restoreScreenTimeout() {
		if(screenTimeoutBak < 0) return;
		setScreenOffTimeout(screenTimeoutBak);
	}

	/*
	public static final int getBrightness() {
		return getSysInt(System.SCREEN_BRIGHTNESS);
	}
	public static final void setBrightness(int brightness) {
		if(brightnessBak < 0) brightnessBak = getBrightness();
		setSysInt(System.SCREEN_BRIGHTNESS, brightness);
	}
	public static final void restoreBrightness() {
		if(brightnessBak < 0) return;
		setBrightness(brightnessBak);
	}

	public static final void dimScreen(boolean dim) {
		setSysInt(System.DIM_SCREEN, dim? 1 : 0);
	}

	public static final void wakeCpu() {
		if(wakeCpuLock == null) wakeCpuLock = A.powerMan().newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE|PowerManager.ACQUIRE_CAUSES_WAKEUP, "Dev");
		wakeCpuLock.acquire();
		wakeCpuLock.release();
	}
	*/

	public static final boolean isWakeScreen() { return wakeScreenLock!=null && wakeScreenLock.isHeld(); }
	public static final void wakeScreen()      { wakeScreen(true); }
	public static final void wakeScreen(boolean release) {
		if(wakeScreenLock == null)
			wakeScreenLock = A.powerMan().newWakeLock(
				PowerManager.SCREEN_DIM_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE|PowerManager.ACQUIRE_CAUSES_WAKEUP, "Dev");
		wakeScreenLock.acquire();
		if(release) wakeScreenLock.release();
	}
	public static final void unwakeScreen() {
		if(wakeScreenLock==null || !wakeScreenLock.isHeld()) return;
		wakeScreenLock.release();
	}

	public static final void enableLock(boolean enable) {
		if(keyguardLock == null) keyguardLock = A.keyguardMan().newKeyguardLock("Dev");
		if(enable) keyguardLock.reenableKeyguard();
		else       keyguardLock.disableKeyguard();
	}

	//---- undocumented android api

	public static final Object iTelMan() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if(iTelMan != null) return iTelMan;
		final TelephonyManager tm = A.telMan();
		final Method itm = tm.getClass().getDeclaredMethod("getITelephony");
		if(!itm.isAccessible()) itm.setAccessible(true);
		return iTelMan = itm.invoke(tm);
	}

	private static boolean _isHotspotOn() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		// use undocumented android api
		final WifiManager wm = A.wifiMan();
		final int as = (Integer)wm.getClass().getMethod("getWifiApState").invoke(wm);
		return as==HOTSPOT_ENABLED || as==HOTSPOT_ENABLING;
	}

	private static boolean _isTetheringOn() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		final ConnectivityManager cm = A.connMan();
		final Class<?> cls = cm.getClass();
    final String[] ifaces = (String[])cls.getMethod("getTetheredIfaces").invoke(cm);
    final String[] regexs = (String[])cls.getMethod("getTetherableUsbRegexs").invoke(cm);
    for(String iface : ifaces)
    	for(String regex : regexs)
    		if(iface.matches(regex)) return true;
    return false;
	}

}
