package cri.sanity;

import java.util.Timer;
import java.util.TimerTask;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.hardware.Sensor;
import android.hardware.SensorManager;


public class PhoneListener extends PhoneStateListener implements SensorEventListener
{
	public  static final int   LISTEN = LISTEN_CALL_STATE|LISTEN_CALL_FORWARDING_INDICATOR;
	private static final int   SKIP_VOLUME = -1;
	private static final float PROX_NEAR   = 0.9f;
	private static final int   FORCE_AUTOSPEAKER_DELAY = Conf.FORCE_AUTOSPEAKER_DELAY;

	private boolean applied = false, proxRegistered = false, headsetRegistered = false;
	private boolean notifyEnable, notifyDisable, proximity, restoreFar, skipHeadset, autoSpeaker, loudSpeaker, speakerCall;
	private boolean headsetOn, mobdataOn, wifiOn, btOn;
	private boolean lastFar   = true;
	private int     lastState = -1;
	private int     disableDelay, enableDelay;
	private int     volRestore, volPhone, volWired, volBt;

	private final int    volMax     = Dev.getVolumeMax(Dev.VOLUME_CALL);
	private final Sensor proxSensor = Dev.sensorProxim();
	private final Timer  timer      = new Timer();
	private TimerTask    timerTask;

	private final BroadcastReceiver headsetWiredReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent i) {
			final boolean on = i.getIntExtra("state",0) != 0;
			updateHeadset(on, volWired);
			A.logd("headsetWiredReceiver: on="+on+", action="+i.getAction());
		}
	};

	private final BroadcastReceiver headsetScoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent i) {
			final boolean on = i.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE,0) == AudioManager.SCO_AUDIO_STATE_CONNECTED;
			updateHeadset(on, volBt);
			A.logd("headsetScoReceiver: on="+on+", action="+i.getAction());
		}
	};

	private final BroadcastReceiver headsetBtReceiver = new BroadcastReceiver() {
		// which audio class to listen for, we only want headsets
		private final int REMOTE_AUDIO_CLASS = BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
		                                     | BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES
		                                     | BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO;
		@Override
		public void onReceive(Context c, Intent i) {
			final BluetoothDevice btDev = i.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
			if(btDev == null) {
				A.logd("headsetBtReceiver skip: null btDev!");
				return;
			}
			final BluetoothClass btClass = btDev.getBluetoothClass();
			if(btClass == null) {
				A.logd("headsetBtReceiver skip: null btClass!");
				return;
			}
			if((btClass.getDeviceClass() & REMOTE_AUDIO_CLASS) == 0) {
				A.logd("headsetBtReceiver skip: no remote class!");
				return;
			}
			final boolean on = i.getAction() == "android.bluetooth.device.action.ACL_CONNECTED";
			updateHeadset(on, volBt);
			A.logd("headsetBtReceiver: on="+on+", action="+i.getAction());
		}
	};
	
	//---- methods
	
	public void startup()
	{
		A.logd("max volume = "+volMax);
		onRinging();
		lastState = TelephonyManager.CALL_STATE_RINGING;
		if(proximity)               A.logd("using proximity sensor.");
		else if(proxSensor == null) A.logd("no proximity: sensor not found.");
		else                        A.logd("no proximity: disabled option.");
	}

	public final int     getState () { return lastState; }
	public final boolean isRinging() { return lastState == TelephonyManager.CALL_STATE_RINGING; }
	public final boolean isOffhook() { return lastState == TelephonyManager.CALL_STATE_OFFHOOK; }
	public final boolean isIdle   () { return lastState == TelephonyManager.CALL_STATE_IDLE   ; }
	private boolean isCallSpeaker () { return speakerCall && lastFar; }

	private void updateHeadset(boolean on, int vol)
	{
		if(headsetOn == on) return;
		headsetOn = on;
		A.logd("headsets are "+(on?"connected":"disconnected"));
		volumeSetter(on, vol);
		enableDevs(on |= lastFar);
		if(autoSpeaker) autoSpeaker(on);
	}

	// invoked when headsets are (un)plugged for automatic volume (un)setting
	private void volumeSetter(boolean on, int vol)
	{
		if(vol <= 0) return;
		if(!on)
			vol = volPhone;
		else if(volPhone <= 0)
			volPhone = volRestore!=SKIP_VOLUME ? volRestore : Dev.getVolume(Dev.VOLUME_CALL);
		Dev.setVolume(Dev.VOLUME_CALL, vol);
		A.logd((on?"preferred":"restored")+" volume set to level "+vol);
	}
	
	private void onRinging()
	{
		A.logd("onRinging");
		lastFar       = true;
		notifyEnable  = A.is("notify_enable");
		notifyDisable = A.is("notify_disable");
		skipHeadset   = A.is("skip_headset");
		proximity     = A.is("proximity") && proxSensor!=null;
		restoreFar    = A.is("restore_far");
		speakerCall   = A.is("speaker_call");
		autoSpeaker   = A.is("auto_speaker");
		loudSpeaker   = A.is("loud_speaker");
		disableDelay  = A.getsi("disable_delay");
		enableDelay   = A.getsi("enable_delay");
		if(enableDelay < 0) enableDelay = disableDelay;
		volRestore = SKIP_VOLUME;
		volWired   = A.getsi("vol_wired");
		volBt      = A.getsi("vol_bt");
		volPhone   = A.getsi("vol_phone");
		// get all enabled devices states
		headsetOn  = skipHeadset && Dev.isHeadsetOn();
		mobdataOn  = A.is("mobdata") && Dev.isMobDataOn();
		wifiOn     = A.is("wifi")    && Dev.isWifiOn();
		btOn       = A.is("bt")      && Dev.isBtOn();
		// register listeners
		regHeadset();
		regProximity();
	}

	// we have a call!
	private void onOffhook()
	{
		A.logd("onOffhook");
		if(headsetOn)
			volumeSetter(true, A.audioMan().isWiredHeadsetOn() ? volWired : volBt);
		else {
			if(volPhone > 0)
				Dev.setVolume(Dev.VOLUME_CALL, volPhone);
			if(isCallSpeaker())
				forceAutoSpeakerOn();
		}
		if(!proximity)
			enableDevs(false);
	}

	// call completed: restore & shutdown
	private void onIdle()
	{
		A.logd("onIdle");
		unregProximity();
		unregHeadset();
		if(isOffhook()) {
			timer.cancel();
			if(volRestore != SKIP_VOLUME) Dev.setVolume(Dev.VOLUME_CALL, volRestore);  // restore volume
			enableDevs(true);                                                          // restore devices
		}
		MainService.stop();
	}

	private synchronized void enableDevs(boolean enable)
	{
		if(headsetOn || enable!=applied) return;
		if(!mobdataOn && !wifiOn && !btOn) return;
		A.logd("enableDevs: " + enable);
		if(wifiOn   ) Dev.enableWifi   (enable);
		if(mobdataOn) Dev.enableMobData(enable);
		if(btOn     ) Dev.enableBt     (enable);
		if(enable) { if(notifyEnable ) A.notify(A.tr(R.string.msg_devs_enabled) ); }
		else       { if(notifyDisable) A.notify(A.tr(R.string.msg_devs_disabled)); }
		applied = !enable;
	}

	private void cleanTimerTask()
	{
		if(timerTask == null) return;
		timerTask.cancel();
		timerTask = null;
		timer.purge();
	}
	
	private void deferEnableDevs(boolean enable)
	{
		cleanTimerTask();
		if(enable) {
			if(enableDelay  == 0) { enableDevs(true ); return; } 
			timerTask = new TimerTask() { public void run() { enableDevs(true ); } };
	    timer.schedule(timerTask, enableDelay);
		}
		else {
			if(disableDelay == 0) { enableDevs(false); return; }
			timerTask = new TimerTask() { public void run() { enableDevs(false); } };
	    timer.schedule(timerTask, disableDelay);
		}
		A.logd("defer "+(enable?"enable":"disable")+" devs");
	}

	private synchronized void autoSpeaker(boolean far)
	{
		if(headsetOn || far==Dev.isSpeakerOn()) return;
		if(far && loudSpeaker && (lastFar!=far || volRestore!=SKIP_VOLUME)) {
			volRestore = Dev.getVolume(Dev.VOLUME_CALL);            // retrieve current call volume (to restore later)
			if(volRestore == volMax) volRestore = SKIP_VOLUME;      // current volume is already at max: don't change
		}
		A.logd("auto set speaker to "+(far?"on":"off"));
		Dev.enableSpeaker(far);                                   // auto enable/disable speaker when phone if far/near to ear
		if(loudSpeaker && volRestore!=SKIP_VOLUME) {
			if(far) {
				Dev.setVolume(Dev.VOLUME_CALL, volMax);
				A.logd("auto set volume to max");
			}
			else {
				Dev.setVolume(Dev.VOLUME_CALL, volRestore);
				A.logd("auto restore volume to "+volRestore);
				volRestore = SKIP_VOLUME;
			}
		}
	}
	
	private void forceAutoSpeakerOn()
	{
		autoSpeaker(true);
		// enable speaker again after a little delay (some phones auto-disable speaker on offhook)
		cleanTimerTask();
		timerTask = new TimerTask(){ public void run(){ autoSpeaker(true); }};
    timer.schedule(timerTask, FORCE_AUTOSPEAKER_DELAY);
	}

	private void regProximity()
	{
		if(proxSensor==null || proxRegistered) return;
		if(!autoSpeaker && (!proximity || (!wifiOn && !btOn && !mobdataOn))) return;
		A.sensorMan().registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_NORMAL);
		proxRegistered = true;
	}

	private void unregProximity()
	{
		if(!proxRegistered) return;
		A.sensorMan().unregisterListener(this);
		proxRegistered = false;
	}

	private void regHeadset()
	{
		if(headsetRegistered || !skipHeadset) return;
		A a = A.app();
		a.registerReceiver(headsetWiredReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		a.registerReceiver(headsetScoReceiver  , new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
		a.registerReceiver(headsetBtReceiver   , new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED"));
		a.registerReceiver(headsetBtReceiver   , new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED"));
		headsetRegistered = true;
	}

	private void unregHeadset()
	{
		if(!headsetRegistered) return;
		headsetRegistered = false;
		A a = A.app();
		a.unregisterReceiver(headsetWiredReceiver);
		a.unregisterReceiver(headsetScoReceiver);
		a.unregisterReceiver(headsetBtReceiver);
		A.logd("unregister headset listeners");
	}
	
	//---- PhoneStateListener implementation

	@Override
	public void onCallForwardingIndicatorChanged(boolean cfi)
	{
		A.logd("onCallForwardingIndicatorChanged: "+cfi);
		if(!headsetOn && isCallSpeaker() && isOffhook()) forceAutoSpeakerOn();
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber)
	{
		if(state == lastState) return;
		switch(state) {
			case TelephonyManager.CALL_STATE_RINGING: onRinging(); break;
			case TelephonyManager.CALL_STATE_OFFHOOK: onOffhook(); break;
			case TelephonyManager.CALL_STATE_IDLE   : onIdle();    break;
		}
		lastState = state;
	}
	
	//---- SensorEventListener implementation

	public void onAccuracyChanged(Sensor sensor, int accuracy) { /* never mind */ }

	public void	onSensorChanged(SensorEvent evt)
	{
		final float   val = evt.values[0];
		final boolean far = val > PROX_NEAR;
		//A.logd("proximity sensor value = "+val);
		if(!headsetOn) {
			if(far == lastFar) return;
			if(autoSpeaker) autoSpeaker(far);
			if(proximity && (!far || restoreFar))
				deferEnableDevs(far);
		}
		lastFar = far;
	}

}
