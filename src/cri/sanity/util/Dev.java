package cri.sanity.util;

import java.lang.reflect.Method;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.app.KeyguardManager.KeyguardLock;
import android.net.Uri;
import com.android.internal.telephony.ITelephony;
import cri.sanity.A;
import cri.sanity.ghost.*;


public final class Dev
{
	private static ITelephony   iTel;
	private static WifiMan      gWifi;
	private static ConnMan      gConn;
	private static KeyguardLock keyguardLock;
	private static PowerManager.WakeLock wakeCpuLock;
	private static PowerManager.WakeLock wakeScreenLock;

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

	//public static final boolean allowMobData() { return Settings.Secure.getInt(A.ctxRes(), "mobile_data", 1) == 1; }

	//---- ghost manager

	public static final ITelephony iTel() { if(iTel  == null) iTel  = getITelephony(); return iTel;  }
	public static final WifiMan   gWifi() { if(gWifi == null) gWifi = new WifiMan();   return gWifi; }
	public static final ConnMan   gConn() { if(gConn == null) gConn = new ConnMan();   return gConn; }

	//---- check on/off device state

	//public static final boolean isHeadsetOn() {
	//	final AudioManager am = A.audioMan();
	//	return am.isWiredHeadsetOn() || am.isBluetoothA2dpOn() || am.isBluetoothScoOn();
	//}

	public static final boolean isMobDataOn() {
    if(iTel()==null || Settings.Secure.getInt(A.resolver(),"mobile_data",1)!=1) return false;
		final int ds = A.telMan().getDataState();
		return ds==TelephonyManager.DATA_CONNECTED || ds==TelephonyManager.DATA_CONNECTING;
	}
	public static final boolean isBtOn() {
		final BluetoothAdapter ba = A.btAdapter();
		return ba!=null && ba.isEnabled();
	}
	public static final boolean isGpsOn() {
		final LocationManager lm = A.locMan();
		return lm!=null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public static final boolean isFlightModeOn() { return getSysInt(System.AIRPLANE_MODE_ON) == 1; }

	public static final boolean isHotspotOn()        { return gWifi().isHotspotOn(); }
	public static final boolean isHotspotSupported() { return gWifi().callable("getWifiApState"); }

	public static final boolean isTetheringOn()        { return gConn().isTetheringOn(); }
	public static final boolean isTetheringSupported() { return gConn().callable("getTetheredIfaces", "getTetherableUsbRegexs"); }

	public static final boolean isRinging() { return A.telMan().getCallState() == TelephonyManager.CALL_STATE_RINGING; }

	//---- enable/disable devices
	
	public static final void answerCall() {
		try {
			iTel().answerRingingCall();
		} catch(Exception e) {
			Intent i1 = new Intent(Intent.ACTION_MEDIA_BUTTON);             
      i1.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
      A.app().sendOrderedBroadcast(i1, "android.permission.CALL_PRIVILEGED");
      Intent i2 = new Intent(Intent.ACTION_MEDIA_BUTTON);               
      i2.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
      A.app().sendOrderedBroadcast(i2, "android.permission.CALL_PRIVILEGED");
		}
	}
	
	public static final boolean endCall() {
		try {
			iTel().endCall();
			return true;
		} catch(Exception e) { try {
			enableFlightMode(true );
			enableFlightMode(false);
			return true;
		} catch(Exception e2) {
			return false;
		}}
	}

	public static final boolean enableBt(boolean enable) {
		final BluetoothAdapter bt = A.btAdapter();
		if(bt == null) return false;
		return enable ? bt.enable() : bt.disable();
	}

	public static final boolean enableFlightMode(boolean enable) {
		if(!System.putInt(A.resolver(), System.AIRPLANE_MODE_ON, enable? 1 : 0)) return false;
		Intent i = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		i.putExtra("state", enable);
		A.app().sendBroadcast(i);
		return true;
	}

	public static final void toggleGps() {
		// use undocumented android broadcast
    Intent i = new Intent();
    i.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
    i.addCategory("android.intent.category.ALTERNATIVE");
    i.setData(Uri.parse("3"));
    A.app().sendBroadcast(i);
	}

	//---- managing system

	public static final int getSysInt(String key) {
		try {
			return System.getInt(A.resolver(), key);
		} catch(SettingNotFoundException e) {
			return -1; 
		}
	}

	//public static final boolean isWakeCpu() { return wakeCpuLock!=null && wakeCpuLock.isHeld(); }
	public static final void wakeCpu() { wakeCpu(false); }
	public static final void wakeCpu(boolean release) {
		if(wakeCpuLock == null) {
			wakeCpuLock = A.powerMan().newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE|PowerManager.ACQUIRE_CAUSES_WAKEUP, "Dev");
			wakeCpuLock.setReferenceCounted(false);
		}
		wakeCpuLock.acquire();
		if(release) wakeCpuLock.release();
	}
	public static final void unwakeCpu() {
		if(wakeCpuLock==null || !wakeCpuLock.isHeld()) return;
		wakeCpuLock.release();
	}

	//public static final boolean isWakeScreen() { return wakeScreenLock!=null && wakeScreenLock.isHeld(); }
	public static final void wakeScreen() { wakeScreen(true); }
	public static final void wakeScreen(boolean release) {
		if(wakeScreenLock == null) {
			wakeScreenLock = A.powerMan().newWakeLock(
				PowerManager.SCREEN_DIM_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE|PowerManager.ACQUIRE_CAUSES_WAKEUP, "Dev");
			wakeScreenLock.setReferenceCounted(false);
		}
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

	private static ITelephony getITelephony() {
		try {
			final TelephonyManager tm = A.telMan();
			final Method m = tm.getClass().getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			return (ITelephony)m.invoke(tm);
		} catch(Exception e) {
			return null;
		}
	}

}
