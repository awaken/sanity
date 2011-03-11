package cri.sanity;

import cri.sanity.util.*;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.AudioManager;
import android.os.SystemClock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public final class PhoneListener extends PhoneStateListener implements SensorEventListener
{
	public  static final int   LISTEN = LISTEN_CALL_STATE|LISTEN_CALL_FORWARDING_INDICATOR;	
	private static final int   FORCE_AUTOSPEAKER_DELAY = Conf.FORCE_AUTOSPEAKER_DELAY;
	private static final int   CALL_STATE_NONE         = -1;
	private static final int   CALL_STATE_RINGING      = TelephonyManager.CALL_STATE_RINGING;
	private static final int   CALL_STATE_OFFHOOK      = TelephonyManager.CALL_STATE_OFFHOOK;
	private static final int   CALL_STATE_IDLE         = TelephonyManager.CALL_STATE_IDLE;
	private static final int   SPEAKER_CALL_INCOMING   = 1;
	private static final int   SPEAKER_CALL_OUTGOING   = 2;
	private static final float PROXIM_MIN              = 0.5f;
	private static final float PROXIM_MAX              = 10.0f;
	private static final int   TASK_DEVS               = Task.idNew();
	private static final int   TASK_SPEAKER            = Task.idNew();

	private static PhoneListener activeInst;

	public int btCount;
	public SpeakerListener speakerListener;

	private String  phoneNumber;
	private int     calls, lastCallState, speakerCall, speakerOnCount, speakerOffCount;
	private boolean outgoing, offhook, shutdown, rec, notifyEnable, notifyDisable;
	private boolean headsetRegistered, proximRegistered, proximReverse, proximDisable, proximEnable;
	private boolean autoSpeaker, speakerOn, speakerOff;
	private boolean gpsAuto, btAuto, btReverse, skipBtConn, screenOff, screenOn;
	private boolean lastFar, volSolo, headsetOn, wiredHeadsetOn, devsLastEnable;
	private long    devsLastTime;
	private int     volRestore, volPhone, volWired, volBt, volSpeaker, volFlags;
	private int     disableDelay, enableDelay, speakerDelay, speakerCallDelay;
	private float   proximFar;
	private TTS     tts;
	private WifiTracker    wifiTrack;
	private MobDataTracker mobdTrack;

	private final AudioManager audioMan = A.audioMan();
	private final Sensor proximSensor   = Dev.sensorProxim();
	private final Task   taskSpeakerOn  = new Task(){ public void run(){ autoSpeaker(true ); }};
	private final Task   taskSpeakerOff = new Task(){ public void run(){ autoSpeaker(false); }};
	private final Task   taskDevsOn     = new Task(){ public void run(){ enableDevs (true ); }};
	private final Task   taskDevsOff    = new Task(){ public void run(){ enableDevs (false); }};
	private       Task   taskSpeakerOnFar;

	private final BroadcastReceiver headsetWiredReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent i) {
			final boolean on = i.getIntExtra("state",0) != 0;
			if(on == wiredHeadsetOn) return;
			updateHeadset(wiredHeadsetOn = on, volWired);
			if(on && btReverse) { Dev.enableBt(false); btReverse = false; }
		}
	};

	//---- static methods

	public static final PhoneListener getActiveInstance() { return activeInst; }
	public static final boolean       isRunning()         { return activeInst != null; }

	//---- methods

	public final void startup()
	{
		activeInst  = this;
		shutdown    = false;
		calls       = 0;
		outgoing    = true;
		offhook     = false;
		phoneNumber = null;
		lastFar     = true;
		rec         = false;
		btReverse   = false;
		btCount     = Math.max(A.geti(K.BT_COUNT), 0);
		proximRegistered = headsetRegistered = false; 
		devsLastTime     = 0;
		devsLastEnable   = true;
		speakerListener  = null;
		lastCallState    = CALL_STATE_NONE;
		wiredHeadsetOn   = audioMan.isWiredHeadsetOn();
		headsetOn        = (btCount>0 && A.is(K.FORCE_BT_AUDIO)) || wiredHeadsetOn || audioMan.isBluetoothA2dpOn() || audioMan.isBluetoothScoOn();
		Dev.wakeCpu();
	}

	private void initCall()
	{
		//A.logd("initCall: begin");
		skipBtConn       = A.is(K.SKIP_BT);
		notifyEnable     = A.is(K.NOTIFY_ENABLE);
		notifyDisable    = A.is(K.NOTIFY_DISABLE);
		proximDisable    = A.is(K.DISABLE_PROXIMITY) && proximSensor!=null;
		proximEnable     = A.is(K.ENABLE_PROXIMITY ) && proximDisable;
		proximReverse    = A.is(K.REVERSE_PROXIMITY);
		rec              = A.is(K.REC);
		autoSpeaker      = A.is(K.SPEAKER_AUTO) && proximSensor!=null;
		screenOff        = Admin.isActive() && A.is(K.SCREEN_OFF);
		screenOn         = A.is(K.SCREEN_ON);
		speakerOnCount   = A.geti(K.SPEAKER_ON_COUNT);
		speakerOffCount  = A.geti(K.SPEAKER_OFF_COUNT);
		speakerOn        = speakerOnCount  >= 0;
		speakerOff       = speakerOffCount >= 0;
		speakerCall      = A.geti(K.SPEAKER_CALL);
		speakerDelay     = A.geti(K.SPEAKER_DELAY);
		speakerCallDelay = A.geti(K.SPEAKER_CALL_DELAY);
		disableDelay     = A.geti(K.DISABLE_DELAY);
		enableDelay      = A.geti(K.ENABLE_DELAY);
		if(enableDelay < 0) enableDelay = disableDelay;
		volRestore = -1;
		volSpeaker = A.geti(K.SPEAKER_VOL);
		volPhone   = A.geti(K.VOL_PHONE);
		volWired   = A.geti(K.VOL_WIRED);
		volBt      = A.geti(K.VOL_BT);
		volSolo    = A.is(K.VOL_SOLO);
		volFlags   = A.is(K.NOTIFY_VOLUME) ? AudioManager.FLAG_SHOW_UI : 0;
		final boolean hotspot = A.is(K.SKIP_HOTSPOT) && Dev.isHotspotOn();
		final boolean tether  = A.is(K.SKIP_TETHER) && Dev.isTetheringOn();
		final boolean btOn    = Dev.isBtOn();
		gpsAuto = A.is(K.AUTO_GPS) && Dev.isGpsOn();
		final boolean wifi = !hotspot && A.is(K.AUTO_WIFI) && A.wifiMan().isWifiEnabled();
		final boolean mobd = !hotspot && !tether && (!gpsAuto || !A.is(K.SKIP_MOBDATA)) && A.is(K.AUTO_MOBDATA) && Dev.isMobDataOn();
		wifiTrack = wifi? new WifiTracker()    : null;
		mobdTrack = mobd? new MobDataTracker() : null;
		btAuto    = btOn && A.is(K.AUTO_BT);
		btReverse = !btOn && !headsetOn && A.is(K.REVERSE_BT);
		regProximity();
		regHeadset();
		if(rec) RecService.start(this);
		//A.logd("startup PhoneListener: end");
	}

	//public final int     getState     () { return lastCallState; }
	//public final boolean isRinging    () { return lastCallState == CALL_STATE_RINGING; }
	//public final boolean isOffhook    () { return lastCallState == CALL_STATE_OFFHOOK; }
	//private      boolean isIdle       () { return lastCallState == CALL_STATE_IDLE; }
	//public final boolean isShutdown   () { return shutdown; }
	public final boolean isOutgoing    () { return outgoing; }
	public final boolean isHeadsetOn   () { return headsetOn; }
	public final boolean hasAutoDev    () { return mobdTrack!=null || wifiTrack!=null|| btAuto || gpsAuto; }
	public final boolean hasAutoSpeaker() { return autoSpeaker; }
	public final String  phoneNumber   () { return phoneNumber; }
	public final boolean hasRingtone   () { return audioMan.getRingerMode() == AudioManager.RINGER_MODE_NORMAL; }

	public final void updateHeadsetBt(boolean on)
	{
		if(wiredHeadsetOn) return;	// if wired headset are on, then the new bt device connected is NOT headset one!
		updateHeadset(on, volBt);
	}

	private void updateHeadset(boolean on, int vol)
	{
		if(headsetOn == on) return;
		headsetOn = on;
		if(!offhook) return;
		setCallVolume(on, vol);
		final boolean on2 = on || (lastFar && proximDisable);
		if(autoSpeaker) autoSpeaker(on2);
		enableDevs(on2);
		screenOff(!on2);
		if(rec) RecService.updateHeadset(on);
		//A.logd("headset connected: "+on);
	}

	// invoked when headsets are (un)plugged for automatic volume (un)setting
	private void setCallVolume(boolean on, int vol)
	{
		if(vol < 0) return;
		if(!on)
			vol = volPhone;
		else if(volPhone < 0)
			volPhone = volRestore<0 ? audioMan.getStreamVolume(AudioManager.STREAM_VOICE_CALL) : volRestore;
		setVolume(vol);
		//A.logd((on?"preferred":"restored")+" volume set to level "+vol);
	}

	private void onRinging()
	{
		//A.logd("onRinging");
		outgoing    = false;
		phoneNumber = PhoneReceiver.number;
		if(CallFilter.includes(phoneNumber, "block", false) && (!A.is(K.BLOCK_SKIP) || hasRingtone()))
			if(Blocker.apply(A.geti(K.BLOCK_MODE))) return;
		if(A.is(K.TTS) && (headsetOn || !A.is(K.TTS_HEADSET)) && (!A.is(K.TTS_SKIP) || hasRingtone()))
			tts = new TTS(phoneNumber, true);
		initCall();
		btReverse();
	}

	// we have a call!
	private void onOffhook()
	{
		//A.logd("onOffhook: begin");
		offhook = true;
		if(tts != null) tts.stop();
		if(outgoing) initCall();
		Dev.enableLock(false);
		volSolo(true);
		if(headsetOn)
			setCallVolume(true, wiredHeadsetOn? volWired : volBt);
		else {
			if(volPhone >= 0) setVolume(volPhone);
			if(     speakerCall == SPEAKER_CALL_INCOMING) { if( outgoing) speakerCall = 0; }
			else if(speakerCall == SPEAKER_CALL_OUTGOING) { if(!outgoing) speakerCall = 0; }
			if(speakerCall>0 && lastFar) speakerOnFar();
		}
		if(!proximDisable) enableDevs(false);
		if(outgoing) {
			phoneNumber = PhoneReceiver.number;
			btReverse();
		}
		if(rec) RecService.checkAutoRec();
		if(!lastFar) screenOff(true);
		//A.logd("onOffhook: end");
	}

	// call completed: restore & shutdown
	private void onIdle()
	{
		//A.logd("onIdle: begin");
		shutdown = true;
		Task.stop(TASK_SPEAKER);
		if(offhook && !headsetOn && A.is(K.SPEAKER_SILENT_END)) audioMan.setSpeakerphoneOn(false);
		unregProximity();
		unregHeadset();
		PhoneReceiver.number = null;
		if(tts != null) { tts.shutdown(); tts = null; }
		if(rec) RecService.stop();
		Task.stopAll();
		if(offhook) {
			if(volRestore >= 0) setVolume(volRestore);
			Dev.enableLock(true);
			volSolo(false);
			screenOff(false);
			if(A.is(K.VIBRATE_END)) A.vibrate();
		}
		try { A.telMan().listen(this, LISTEN_NONE); } catch(Exception e) {}
		Blocker.shutdown();
		CallFilter.shutdown();
		if(offhook) enableDevs(true);
		if(rec) RecService.cron();
		if(mobdTrack != null) { mobdTrack.shutdown(); mobdTrack = null; }
		if(wifiTrack != null) { wifiTrack.shutdown(); wifiTrack = null; }
		Task.shutdownWait();
		if((btReverse || A.is(K.BT_OFF)) && Dev.isBtOn()) Dev.enableBt(false);
		MainService.stop();
		phoneNumber = null;
		activeInst  = null;
		//A.logd("onIdle: end");
		Dev.unwakeCpu();
	}

	private synchronized void enableDevs(boolean enable)
	{
		if(!enable && headsetOn) return;
		final long now  = SystemClock.elapsedRealtime();
		final long diff = now - devsLastTime;
		if(diff < Conf.DEVS_MIN_RETRY) {
			if(enable == devsLastEnable) return;
			(enable? taskDevsOn : taskDevsOff).exec(TASK_DEVS, (int)diff);
		}
		boolean done = false;
		if(gpsAuto         && enable!=Dev.isGpsOn())      { Dev.toggleGps();          done = true; }
		if(wifiTrack!=null && enable!=wifiTrack.willOn()) { wifiTrack.enable(enable); done = true; }
		if(mobdTrack!=null && enable!=mobdTrack.willOn()) { mobdTrack.enable(enable); done = true; }
		if(btAuto && !btReverse && (!skipBtConn||btCount<1) && enable!=Dev.isBtOn()) { Dev.enableBt(enable); done = true; }
		//A.logd("enableDevs: "+enable+", done = "+done);
		if(!done) return;
		devsLastTime   = now;
		devsLastEnable = enable;
		if(enable) { if(notifyEnable ) { A.notify(A.s(R.string.msg_devs_enabled )); if(rec) A.notifyCanc(); }}
		else       { if(notifyDisable) { A.notify(A.s(R.string.msg_devs_disabled)); if(rec) A.notifyCanc(); }}
	}

	private synchronized void autoSpeaker(boolean far)
	{
		if(headsetOn || far==audioMan.isSpeakerphoneOn()) return;
		if(far && volSpeaker>=0 && volRestore<0)
			volRestore = volPhone<0 ? audioMan.getStreamVolume(AudioManager.STREAM_VOICE_CALL) : volPhone;  // retrieve current call volume (to restore later)
		audioMan.setSpeakerphoneOn(far);
		//A.logd("auto set speaker: far="+far);
		if(volSpeaker >= 0) {
			if(far) {
				setVolume(volSpeaker);
				//A.logd("auto set speaker volume");
			} else if(volRestore >= 0) {
				setVolume(volRestore);
				//A.logd("restore from speaker volume to "+volRestore);
				//volRestore = -1;
			}
		}
		if(speakerListener != null) speakerListener.onSpeakerChanged(far);
	}
	
	private void speakerOnFar()
	{
		if(speakerCallDelay <= 0) {
			autoSpeaker(true);
			if(!outgoing) return;
		}
		if(taskSpeakerOnFar == null)
			taskSpeakerOnFar = new Task(){ public void run(){ if(lastFar) autoSpeaker(true); }};
		// enable speaker after a little delay (some phones auto-disable speaker on offhook!!)
		if(outgoing && speakerCallDelay<FORCE_AUTOSPEAKER_DELAY) speakerCallDelay = FORCE_AUTOSPEAKER_DELAY;
		taskSpeakerOnFar.exec(TASK_SPEAKER, speakerCallDelay);
		//A.logd("speakerOnFar: delay="+speakerCallDelay);
	}
	
	private void setVolume(int vol) { audioMan.setStreamVolume(AudioManager.STREAM_VOICE_CALL, vol, volFlags); }

	private void screenOff(boolean off)
	{
		if(off) {
			if(!screenOff) return;
			try { A.devpolMan().lockNow(); } catch(Exception e) {}
		} else {
			if(!screenOn) return;
			Dev.wakeScreen();
		}
		//A.logd("screenOff: "+off);
	}
	
	private void volSolo(boolean enable)
	{
		if(!volSolo) return;
		audioMan.setStreamMute(AudioManager.STREAM_SYSTEM      , enable);
		audioMan.setStreamMute(AudioManager.STREAM_ALARM       , enable);
		audioMan.setStreamMute(AudioManager.STREAM_NOTIFICATION, enable);
		if(!rec) audioMan.setStreamMute(AudioManager.STREAM_MUSIC, enable);		// if we mute media, we cannot record phone call!
		//A.logd("set volume solo: "+enable);
	}
	
	private void btReverse()
	{
		if(!btReverse) return;
		Dev.enableBt(true);
		final int timeout = A.geti(K.REVERSE_BT_TIMEOUT);
		if(timeout <= 0) return;
		new Task(){ public void run(){
			if(headsetOn || !btReverse) return;
			Dev.enableBt(false);
			btReverse = false;
		}}.exec(timeout);
	}

	private void regProximity() {
		proximRegistered = proximSensor!=null && (autoSpeaker || screenOff || screenOn || (proximDisable && hasAutoDev()));
		if(!proximRegistered) return;
		final float range = proximSensor.getMaximumRange();
		proximFar = Math.max(PROXIM_MIN, Math.min(PROXIM_MAX, Math.abs(range))) - 0.1f;
		A.sensorMan().registerListener(this, proximSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	private void unregProximity() {
		if(!proximRegistered) return;
		proximRegistered = false;
		try { A.sensorMan().unregisterListener(this); } catch(Exception e) {}
	}

	private void regHeadset() {
		headsetRegistered = true;
		A.app().registerReceiver(headsetWiredReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
	}
	private void unregHeadset() {
		if(!headsetRegistered) return;
		headsetRegistered = false;
		try { A.app().unregisterReceiver(headsetWiredReceiver); } catch(Exception e) {}
	}

	//---- PhoneStateListener implementation

	@Override
	public void onCallForwardingIndicatorChanged(boolean cfi)
	{
		if(outgoing && offhook && !headsetOn && speakerCall>0 && lastFar) speakerOnFar();
		//A.logd("onCallForwardingIndicatorChanged: "+cfi);
	}

	@Override
	public void onCallStateChanged(int state, String num)
	{
		// check against "calls" counter to skip multiple/concurrent phone calls
		switch(state) {
			case CALL_STATE_RINGING:
				if(lastCallState != CALL_STATE_NONE) break;
				onRinging();
				break;
			case CALL_STATE_OFFHOOK:
				if(++calls != 1) break;
				onOffhook();
				break;
			case CALL_STATE_IDLE:
				if(--calls > 0) break;
				onIdle();
				calls = 0;
				break;
		}
		lastCallState = state;
		//A.logd("onCallStateChanged: state="+state+", calls="+calls);
	}

	//---- SensorEventListener implementation

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }

	@Override
	public void	onSensorChanged(SensorEvent evt)
	{
		if(shutdown) return;
		final float   val = evt.values[0];
		final boolean far = proximReverse? val<proximFar : val>=proximFar;
		//A.logd("proximity sensor value = "+val);
		if(far == lastFar) return;
		if(offhook && !headsetOn) {
			if(autoSpeaker && !headsetOn) {
				if(far) {
					if(speakerOn) {
						taskSpeakerOn.exec(TASK_SPEAKER, speakerDelay);
						speakerOn = --speakerOnCount != 0;
					}
				}
				else {
					if(speakerOff) {
						taskSpeakerOff.exec(TASK_SPEAKER, 0);
						speakerOff = --speakerOffCount != 0;
					}
				}
			}
			if(proximDisable && (!far || proximEnable)) {
				if(far) taskDevsOn .exec(TASK_DEVS,  enableDelay);
				else    taskDevsOff.exec(TASK_DEVS, disableDelay);
			}
			screenOff(!far);
		}
		lastFar = far;
	}

}
