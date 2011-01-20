package cri.sanity;

import java.util.Timer;
import java.util.TimerTask;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


public final class PhoneListener extends PhoneStateListener
{
	public  static final int    LISTEN = LISTEN_CALL_STATE|LISTEN_CALL_FORWARDING_INDICATOR;
	private static final int    FORCE_AUTOSPEAKER_DELAY = Conf.FORCE_AUTOSPEAKER_DELAY;
	private static final int    SKIP_VOLUME = -1;

	private static PhoneListener activeInst;

	public  int     btCount;
	private boolean shutdown, notifyEnable, notifyDisable;
	private boolean proximRegistered, proximReverse, proximDisable, proximEnable;
	private boolean skipHeadset, autoSpeaker, loudSpeaker, speakerCall, headsetOn, wiredHeadsetOn;
	private boolean mobdataAuto, wifiAuto, gpsAuto, btAuto, skipBtConn, screenOff;
	private boolean lastFar;
	private int     lastCallState = -1;
	private int     disableDelay, enableDelay, speakerDelay;
	private int     volRestore, volPhone, volWired, volBt;
	private TimerTask enableTask, speakerTask;
	private Timer     timer;

	private final int volMax = Dev.getVolumeMax(Dev.VOL_CALL);
	private final Sensor proximSensor = Dev.sensorProxim();
	//private final float  proximMax    = proximSensor==null ? 666 : proximSensor.getMaximumRange();

	//---- inner classes

	private final SensorEventListener proximListener = new SensorEventListener()
	{
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) { }
		@Override
		public void	onSensorChanged(SensorEvent evt) {
			if(shutdown) return;
			final float   val = evt.values[0];
			final boolean far = proximReverse? val<0.1f : val>0.1f;
			//final boolean far = proximReverse? val<=proximMax+0.0001 : val>=proximMax-0.0001;
			//A.logd("proximity sensor value = "+val);
			if(far == lastFar) return;
			if(!headsetOn || !skipHeadset) {
				if(autoSpeaker)
					deferAutoSpeaker(far);
				if(proximDisable && (!far || proximEnable))
					deferEnableDevs(far);
				if(screenOff)
					screenOff(!far);
			}
			lastFar = far;
		}
	};

	private final BroadcastReceiver headsetWiredReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context c, Intent i) {
			final boolean on = i.getIntExtra("state",0) != 0;
			if(on == wiredHeadsetOn) return;
			updateHeadset(wiredHeadsetOn = on, volWired);
			A.logd("wired headset connected = "+on);
		}
	};

	//---- static methods

	public static final PhoneListener getActiveInstance() { return activeInst; }
	public static final boolean isRunning()               { return activeInst != null; }
	
	//---- methods

	/*
	public PhoneListener()
	{
		if(proximMax == 666)
			A.logd("cannot get sensor max range: null proximity sensor");
	}
	*/
	
	public final void startup()
	{
		onRinging();
		lastCallState = TelephonyManager.CALL_STATE_RINGING;
		if(A.DEBUG) {
			if(proximRegistered)          A.logd("using proximity sensor");
			else if(proximSensor == null) A.logd("no proximity: sensor not found");
			else                          A.logd("no proximity: disabled option");
		}
	}

	public final int     getState     () { return lastCallState; }
	public final boolean isRinging    () { return lastCallState == TelephonyManager.CALL_STATE_RINGING; }
	public final boolean isOffhook    () { return lastCallState == TelephonyManager.CALL_STATE_OFFHOOK; }
	public final boolean isIdle       () { return lastCallState == TelephonyManager.CALL_STATE_IDLE   ; }
	public final boolean isShutdown   () { return shutdown; }
	public final boolean isCallSpeaker() { return speakerCall && lastFar; }
	public final boolean hasAutoDev   () { return mobdataAuto || wifiAuto || btAuto || gpsAuto; }

	public final void updateHeadsetBt(boolean on)
	{
		if(wiredHeadsetOn) return;	// if wired headset are on, then the new bt device connected is NOT headset one!
		updateHeadset(on, volBt);
	}

	private void updateHeadset(boolean on, int vol)
	{
		if(headsetOn == on) return;
		headsetOn = on;
		setCallVolume(on, vol);
		on |= lastFar && proximDisable;
		if(autoSpeaker) autoSpeaker(on);
		enableDevs(on);
		if(screenOff) screenOff(!on);
		A.logd("headset connected: "+on);
	}

	// invoked when headsets are (un)plugged for automatic volume (un)setting
	private void setCallVolume(boolean on, int vol)
	{
		if(vol <= 0) return;
		if(!on)
			vol = volPhone;
		else if(volPhone <= 0)
			volPhone = volRestore!=SKIP_VOLUME ? volRestore : Dev.getVolume(Dev.VOL_CALL);
		Dev.setVolume(Dev.VOL_CALL, vol);
		A.logd((on?"preferred":"restored")+" volume set to level "+vol);
	}

	private void onRinging()
	{
		A.logd("onRinging");
		timer    = new Timer();
		shutdown = false;
		lastFar  = true;
		btCount  = Math.max(A.geti(P.BT_COUNT), 0);
		skipHeadset   = A.is(P.SKIP_HEADSET);
		skipBtConn    = A.is(P.SKIP_BT);
		notifyEnable  = A.is(P.NOTIFY_ENABLE);
		notifyDisable = A.is(P.NOTIFY_DISABLE);
		proximDisable = A.is(P.DISABLE_PROXIMITY) && proximSensor!=null;
		proximEnable  = A.is(P.ENABLE_PROXIMITY ) && proximDisable;
		proximReverse = A.is(P.REVERSE_PROXIMITY);
		speakerCall   = A.is(P.SPEAKER_CALL);
		autoSpeaker   = A.is(P.SPEAKER_AUTO);
		loudSpeaker   = A.is(P.SPEAKER_LOUD);
		screenOff     = A.is(P.SCREEN_OFF);
		speakerDelay  = A.getsi(P.SPEAKER_DELAY);
		disableDelay  = A.getsi(P.DISABLE_DELAY);
		enableDelay   = A.getsi(P.ENABLE_DELAY);
		if(enableDelay < 0) enableDelay = disableDelay;
		volRestore  = SKIP_VOLUME;
		volPhone    = A.getsi(P.VOL_PHONE);
		volWired    = A.getsi(P.VOL_WIRED);
		volBt       = A.getsi(P.VOL_BT);
		// get all enabled devices states
		gpsAuto     = A.is(P.AUTO_GPS)     && Dev.isGpsOn();
		wifiAuto    = A.is(P.AUTO_WIFI)    && Dev.isWifiOn();
		mobdataAuto = A.is(P.AUTO_MOBDATA) && Dev.isMobDataOn() && (!gpsAuto || !A.is(P.SKIP_MOBDATA));
		btAuto      = A.is(P.AUTO_BT)      && Dev.isBtOn();
		headsetOn   = skipHeadset && (Dev.isHeadsetOn() || (btCount>0 && A.is(P.FORCE_BT_AUDIO)));
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
			setCallVolume(true, wiredHeadsetOn? volWired : volBt);
		else {
			if(volPhone > 0) Dev.setVolume(Dev.VOL_CALL, volPhone);
			if(isCallSpeaker()) forceAutoSpeakerOn();
		}
		if(!proximDisable) enableDevs(false);
		if(screenOff) screenOff(true);
	}

	// call completed: restore & shutdown
	private void onIdle()
	{
		A.logd("onIdle");
		shutdown = true;
		unregProximity();
		unregHeadset();
		cleanAllTasks();
		timer.cancel();
		//Dev.restoreBrightness();
		Dev.restoreScreenTimeout();
		if(volRestore != SKIP_VOLUME)
			Dev.setVolume(Dev.VOL_CALL, volRestore);   // restore call volume before ringing
		enableDevs(true);                            // restore all devices enabled before ringing
		activeInst = null;
		MainService.stop();
		System.gc();
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
		if(!enable && headsetOn && skipHeadset) return;
		boolean done = false;
		if(gpsAuto     && enable!=Dev.isGpsOn    ()) { Dev.toggleGps();           done = true; }
		if(wifiAuto    && enable!=Dev.isWifiOn   ()) { Dev.enableWifi   (enable); done = true; }
		if(mobdataAuto && enable!=Dev.isMobDataOn()) { Dev.enableMobData(enable); done = true; }
		if(btAuto      && enable!=Dev.isBtOn     ())
			if(!skipBtConn || btCount<1)               { Dev.enableBt     (enable); done = true; }
		if(!done) return;
		if(enable) { if(notifyEnable ) A.notify(A.tr(R.string.msg_devs_enabled )); }
		else       { if(notifyDisable) A.notify(A.tr(R.string.msg_devs_disabled)); }
		//A.logd("enableDevs: "+enable);
	}

	private void deferEnableDevs(boolean enable)
	{
		cleanEnableTask();
		if(enable) {
			if(enableDelay  <= 0) { enableDevs(true ); return; }
			enableTask = new TimerTask(){ public void run(){ enableDevs(true ); }};
	    timer.schedule(enableTask, enableDelay);
		} else {
			if(disableDelay <= 0) { enableDevs(false); return; }
			enableTask = new TimerTask(){ public void run(){ enableDevs(false); }};
	    timer.schedule(enableTask, disableDelay);
		}
		A.logd("defer "+(enable?"enable":"disable")+" devs");
	}
	
	private void deferAutoSpeaker(boolean far)
	{
		cleanSpeakerTask();
		if(headsetOn) return;
		// do not defer when enabling speaker and when delay is zero!
		if(!far || speakerDelay<=0) {
			autoSpeaker(far);
			return;
		}
		speakerTask = new TimerTask(){ public void run(){ autoSpeaker(true); }};
    timer.schedule(speakerTask, speakerDelay);
		A.logd("defer auto speaker on");
	}

	private synchronized void autoSpeaker(boolean far)
	{
		if(headsetOn || far==Dev.isSpeakerOn()) return;
		if(far && loudSpeaker && (lastFar!=far || volRestore!=SKIP_VOLUME)) {
			volRestore = Dev.getVolume(Dev.VOL_CALL);               // retrieve current call volume (to restore later)
			if(volRestore == volMax) volRestore = SKIP_VOLUME;      // current volume is already at max: don't change
		}
		A.logd("auto set speaker: far="+far);
		Dev.enableSpeaker(far);                                   // auto enable/disable speaker when phone if far/near to ear
		if(loudSpeaker && volRestore!=SKIP_VOLUME) {
			if(far) {
				Dev.setVolume(Dev.VOL_CALL, 1000);
				//Dev.setVolume(Dev.VOLUME_CALL, volMax);
				A.logd("auto set loud speaker");
			} else {
				Dev.setVolume(Dev.VOL_CALL, volRestore);
				A.logd("restore (from loud speaker) volume to "+volRestore);
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
		} else {
			//Dev.dimScreen(false);
			//Dev.restoreBrightness();
			Dev.restoreScreenTimeout();
		}
	}

	private void regProximity() {
		proximRegistered = proximSensor!=null && (autoSpeaker || screenOff || (proximDisable && hasAutoDev()));
		if(!proximRegistered) return;
		A.sensorMan().registerListener(proximListener, proximSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	private void unregProximity() {
		if(!proximRegistered) return;
		proximRegistered = false;
		A.sensorMan().unregisterListener(proximListener);
	}

	private void regHeadset() {
		A.app().registerReceiver(headsetWiredReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
	}
	private void unregHeadset() {
		A.app().unregisterReceiver(headsetWiredReceiver);
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
		if(state == lastCallState) return;
		switch(state) {
			case TelephonyManager.CALL_STATE_RINGING: onRinging(); break;
			case TelephonyManager.CALL_STATE_OFFHOOK: onOffhook(); break;
			case TelephonyManager.CALL_STATE_IDLE   : onIdle();    break;
		}
		lastCallState = state;
	}

}
