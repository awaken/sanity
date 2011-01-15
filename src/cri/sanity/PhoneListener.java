package cri.sanity;

import java.util.Timer;
import java.util.TimerTask;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
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
	public  static final String BTCOUNT_KEY = "bt_count";
	public  static final int    LISTEN = LISTEN_CALL_STATE|LISTEN_CALL_FORWARDING_INDICATOR;
	private static final int    SKIP_VOLUME = -1;
	private static final float  PROX_NEAR   = 0.9f;
	private static final int   FORCE_AUTOSPEAKER_DELAY = Conf.FORCE_AUTOSPEAKER_DELAY;

	private static PhoneListener activeInst;

	public  int btCount;

	private boolean shutdown = false, applied = false, proxRegistered = false, headsetRegistered = false;
	private boolean notifyEnable, notifyDisable, proximity, restoreFar, skipHeadset, autoSpeaker, loudSpeaker, speakerCall, screenOff;
	private boolean headsetOn, wiredHeadsetOn, mobdataOn, wifiOn, btOn, skipBtConn;
	private boolean lastFar;
	private int     lastState = -1;
	private int     disableDelay, enableDelay, speakerDelay;
	private int     volRestore, volPhone, volWired, volBt;

	private final int    volMax     = Dev.getVolumeMax(Dev.VOLUME_CALL);
	private final Sensor proxSensor = Dev.sensorProxim();
	private final Timer  timer      = new Timer();
	private TimerTask    enableTask, speakerTask;

	private final BroadcastReceiver headsetWiredReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent i) {
			final boolean on = i.getIntExtra("state",0) != 0;
			if(on == wiredHeadsetOn) return;
			updateHeadset(wiredHeadsetOn = on, volWired);
			A.logd("wired headset connected = "+on);
		}
	};

	//---- methods

	public static final PhoneListener getActiveInstance() { return activeInst; }
	
	public final void startup()
	{
		A.logd("max volume = "+volMax);
		onRinging();
		lastState = TelephonyManager.CALL_STATE_RINGING;
		if(A.DEBUG) {
			if(proximity)               A.logd("using proximity sensor");
			else if(proxSensor == null) A.logd("no proximity: sensor not found");
			else                        A.logd("no proximity: disabled option");
		}
	}

	public final int     getState     () { return lastState; }
	public final boolean isRinging    () { return lastState == TelephonyManager.CALL_STATE_RINGING; }
	public final boolean isOffhook    () { return lastState == TelephonyManager.CALL_STATE_OFFHOOK; }
	public final boolean isIdle       () { return lastState == TelephonyManager.CALL_STATE_IDLE   ; }
	public final boolean isShutdown   () { return shutdown; }
	public final boolean isCallSpeaker() { return speakerCall && lastFar; }
	public final boolean isDevEnabled () { return mobdataOn || wifiOn || btOn; }

	public final void updateHeadsetBt(boolean on)
	{
		if(wiredHeadsetOn) return;
		updateHeadset(on, volBt);
	}

	private void updateHeadset(boolean on, int vol)
	{
		if(headsetOn == on) return;
		headsetOn = on;
		A.logd("headset connected: "+on);
		volumeSetter(on, vol);
		on |= lastFar;
		if(!skipHeadset) enableDevs(on);
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
		shutdown      = false;
		lastFar       = true;
		notifyEnable  = A.is("notify_enable");
		notifyDisable = A.is("notify_disable");
		skipHeadset   = A.is("skip_headset");
		proximity     = A.is("proximity") && proxSensor!=null;
		restoreFar    = A.is("restore_far");
		speakerCall   = A.is("speaker_call");
		autoSpeaker   = A.is("auto_speaker");
		loudSpeaker   = A.is("loud_speaker");
		speakerDelay  = A.getsi("delay_speaker");
		disableDelay  = A.getsi("disable_delay");
		enableDelay   = A.getsi("enable_delay");
		screenOff     = proximity && A.is("screen_off");
		if(enableDelay < 0) enableDelay = disableDelay;
		volRestore = SKIP_VOLUME;
		volWired   = A.getsi("vol_wired");
		volBt      = A.getsi("vol_bt");
		volPhone   = A.getsi("vol_phone");
		// get all enabled devices states
		mobdataOn  = A.is("mobdata") && Dev.isMobDataOn();
		wifiOn     = A.is("wifi")    && Dev.isWifiOn();
		btOn       = A.is("bt")      && Dev.isBtOn();
		skipBtConn = A.is("bt_skip");
		btCount    = Math.max(A.geti(BTCOUNT_KEY), 0);
		headsetOn  = skipHeadset && Dev.isHeadsetOn();
		wiredHeadsetOn = skipHeadset && A.audioMan().isWiredHeadsetOn();
		// register listeners
		regHeadset();
		regProximity();
		// set current active instance of this class (used by BtReceiver)
		activeInst = this;
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
		shutdown   = true;
		activeInst = null;
		unregProximity();
		unregHeadset();
		cleanAllTasks();
		timer.cancel();
		//Dev.restoreBrightness();
		Dev.restoreScreenTimeout();
		if(isOffhook()) {
			if(volRestore != SKIP_VOLUME) Dev.setVolume(Dev.VOLUME_CALL, volRestore);  // restore volume
			enableDevs(true);                                                          // restore devices
		}
		MainService.stop();
	}

	private void cleanEnableTask()
	{
		if(enableTask == null) return;
		enableTask.cancel();
		enableTask = null;
		timer.purge();
	}

	private void cleanSpeakerTask()
	{
		if(speakerTask == null) return;
		speakerTask.cancel();
		speakerTask = null;
		timer.purge();
	}
	
	private void cleanAllTasks()
	{
		cleanEnableTask();
		cleanSpeakerTask();
	}

	private synchronized void enableDevs(boolean enable)
	{
		if(headsetOn || enable!=applied || !isDevEnabled()) return;
		if(wifiOn   ) Dev.enableWifi   (enable);
		if(mobdataOn) Dev.enableMobData(enable);
		if(btOn     ) {
			if(!skipBtConn || btCount<1 || (enable && enable!=Dev.isBtOn()))
				Dev.enableBt(enable);
			else if(!wifiOn && !mobdataOn)
				return;
		}
		if(enable) { if(notifyEnable ) A.notify(A.tr(R.string.msg_devs_enabled )); }
		else       { if(notifyDisable) A.notify(A.tr(R.string.msg_devs_disabled)); }
		applied = !enable;
		A.logd("enableDevs: " + enable);
	}

	private void deferEnableDevs(boolean enable)
	{
		cleanEnableTask();
		if(enable) {
			if(enableDelay  <= 0) { enableDevs(true ); return; } 
			enableTask = new TimerTask() { public void run() { enableDevs(true ); } };
	    timer.schedule(enableTask, enableDelay);
		}
		else {
			if(disableDelay <= 0) { enableDevs(false); return; }
			enableTask = new TimerTask() { public void run() { enableDevs(false); } };
	    timer.schedule(enableTask, disableDelay);
		}
		A.logd("defer "+(enable?"enable":"disable")+" devs");
	}
	
	private void deferAutoSpeaker(boolean far)
	{
		cleanSpeakerTask();
		// do not defer when enabling speaker and when delay is zero!
		if(!far || speakerDelay<=0) {
			autoSpeaker(far);
			return;
		}
		speakerTask = new TimerTask() { public void run() { autoSpeaker(true); } };
    timer.schedule(speakerTask, speakerDelay);
		A.logd("defer auto speaker on");
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
		cleanEnableTask();
		// use enableTask so that any proximity changes will cancel this task
		enableTask = new TimerTask(){ public void run(){ if(lastFar) autoSpeaker(true); }};
    timer.schedule(enableTask, FORCE_AUTOSPEAKER_DELAY);
    A.logd("force auto speaker on");
	}

	private void screenOff(boolean off)
	{
		if(off) {
			//Dev.setBrightness(0);
			//Dev.dimScreen(true);
			Dev.setScreenOffTimeout(Conf.CALL_SCREEN_TIMEOUT);
		}
		else {
			//Dev.dimScreen(false);
			//Dev.restoreBrightness();
			Dev.restoreScreenTimeout();
		}
	}

	private void regProximity()
	{
		if(proxSensor==null || proxRegistered) return;
		if(!autoSpeaker && (!proximity || !isDevEnabled())) return;
		A.sensorMan().registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_NORMAL);
		proxRegistered = true;
	}

	private void unregProximity()
	{
		if(!proxRegistered) return;
		proxRegistered = false;
		A.sensorMan().unregisterListener(this);
	}

	private void regHeadset()
	{
		if(headsetRegistered || !skipHeadset) return;
		headsetRegistered = true;
		A.app().registerReceiver(headsetWiredReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		//A.logd("register wired headser listener");
	}

	private void unregHeadset()
	{
		if(!headsetRegistered) return;
		headsetRegistered = false;
		A.app().unregisterReceiver(headsetWiredReceiver);
		//A.logd("unregister wired headset listener");
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
		if(shutdown) return;
		final float   val = evt.values[0];
		final boolean far = val > PROX_NEAR;
		//A.logd("proximity sensor value = "+val);
		if(far == lastFar) return;
		if(!headsetOn) {
			if(autoSpeaker) deferAutoSpeaker(far);
			if(proximity) {
				if(!far || restoreFar) deferEnableDevs(far);
				if(screenOff) screenOff(!far);
			}
		}
		lastFar = far;
	}

}
