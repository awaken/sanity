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
	public  static final int LISTEN = LISTEN_CALL_STATE|LISTEN_CALL_FORWARDING_INDICATOR;	
	private static final int FORCE_AUTOSPEAKER_DELAY = Conf.FORCE_AUTOSPEAKER_DELAY;
	private static final int CALL_STATE_NONE         = -1;
	private static final int SPEAKER_CALL_INCOMING   = 1;
	private static final int SPEAKER_CALL_OUTGOING   = 2;
	private static final int TASK_DEVS               = Task.idNew();
	private static final int TASK_SPEAKER            = Task.idNew();

	private static PhoneListener activeInst;

	public int btCount;
	public SpeakerListener speakerListener;

	private String  phoneNumber;
	private int     lastCallState, speakerCall, speakerOnCount, speakerOffCount;
	private boolean outgoing, offhook, shutdown, rec, notifyEnable, notifyDisable;
	private boolean headsetRegistered, proximRegistered, proximReverse, proximDisable, proximEnable;
	private boolean autoSpeaker, speakerOn, speakerOff;
	private boolean gpsAuto, btAuto, btReverse, skipBtConn, screenOff, screenOn;
	private boolean lastFar, volSolo, headsetOn, wiredHeadsetOn, devsLastEnable;
	private long    devsLastTime;
	private int     volRestore, volPhone, volWired, volBt, volSpeaker, volFlags, ringMode, vibrMode;
	private int     disableDelay, enableDelay, speakerDelay, speakerCallDelay;
	private float   proximFar;
	private TTS     tts;
	private WifiTracker    wifiTrack;
	private MobDataTracker mobdTrack;

	private final AudioManager audioMan = A.audioMan();
	private Sensor proximSensor;
	private Task   taskSpeakerOn;
	private Task   taskSpeakerOff;
	private Task   taskDevsOn;
	private Task   taskDevsOff;
	private Task   taskSpeakerOnFar;

	private BroadcastReceiver screenOnReceiver, screenOffReceiver;

	private final BroadcastReceiver headsetWiredReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent i) {
			final boolean on = i.getIntExtra("state",0) != 0;
			if(on == wiredHeadsetOn) return;
			updateHeadset(wiredHeadsetOn = on, volWired);
			if(on && btReverse) {
				btReverse = false;
				Dev.enableBt(false);
			}
		}
	};

	//---- static public api

	public static final PhoneListener getActiveInstance() { return activeInst; }
	public static final boolean       isRunning()         { return activeInst != null; }

	//---- public api

	public final void startup() {
		activeInst  = this;
		shutdown    = false;
		outgoing    = true;
		offhook     = false;
		phoneNumber = null;
		lastFar     = true;
		rec         = false;
		btReverse   = false;
		btCount     = Math.max(A.geti(K.BT_COUNT), 0);
		ringMode    = -1;
		vibrMode    = -1;
		proximRegistered = headsetRegistered = false; 
		devsLastTime     = 0;
		devsLastEnable   = true;
		speakerListener  = null;
		lastCallState    = CALL_STATE_NONE;
		wiredHeadsetOn   = audioMan.isWiredHeadsetOn();
		headsetOn        = wiredHeadsetOn || (btCount>0 && A.is(K.FORCE_BT_AUDIO)) || audioMan.isBluetoothA2dpOn() || audioMan.isBluetoothScoOn();
	}

	public final boolean isOutgoing    () { return outgoing;  }
	public final boolean isHeadsetOn   () { return headsetOn; }
	public final boolean hasAutoDev    () { return mobdTrack!=null || wifiTrack!=null || btAuto || gpsAuto; }
	public final boolean hasAutoSpeaker() { return autoSpeaker; }
	public final String  phoneNumber   () { return phoneNumber; }

	public boolean changeRinger(int ring, int vibr) {
		return changeRinger(ring, vibr, audioMan.getRingerMode(), audioMan.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER));
	}

	public final void updateHeadsetBt(boolean on) {
		if(wiredHeadsetOn) return;	// if wired headset are on, then the new bt device connected is NOT headset one!
		updateHeadset(on, volBt);
	}
	
	//---- private api

	private void initCall() {
		//A.logd("PhoneListener.initCall: begin");
		proximSensor = A.sensorProxim();
		if(proximSensor == null) {
			proximDisable = proximEnable = proximReverse = autoSpeaker = false;
		} else {
			autoSpeaker     = A.is(K.SPEAKER_AUTO);
			speakerDelay    = A.geti(K.SPEAKER_DELAY);
			speakerOnCount  = A.geti(K.SPEAKER_ON_COUNT);
			speakerOffCount = A.geti(K.SPEAKER_OFF_COUNT);
			speakerOn       = speakerOnCount  >= 0;
			speakerOff      = speakerOffCount >= 0;
			proximReverse   = A.is(K.REVERSE_PROXIMITY);
			proximDisable   = A.is(K.DISABLE_PROXIMITY);
			if(!proximDisable)
				proximEnable  = false;
			else {
				proximEnable  = A.is(K. ENABLE_PROXIMITY);
				disableDelay  = A.geti(K.DISABLE_DELAY);
				enableDelay   = A.geti(K.ENABLE_DELAY);
				if(enableDelay < 0) enableDelay = disableDelay;
			}
		}
		speakerCall      = A.geti(K.SPEAKER_CALL);
		speakerCallDelay = A.geti(K.SPEAKER_CALL_DELAY);
		screenOff  = Admin.isActive() && A.is(K.SCREEN_OFF);
		screenOn   = A.is(K.SCREEN_ON);
		rec        = A.is(K.REC);
		volRestore = -1;
		volSpeaker = A.geti(K.SPEAKER_VOL);
		volPhone   = A.geti(K.VOL_PHONE);
		volWired   = A.geti(K.VOL_WIRED);
		volBt      = A.geti(K.VOL_BT);
		volSolo    = A.is(K.VOL_SOLO);
		volFlags   = A.is(K.NOTIFY_VOLUME) ? AudioManager.FLAG_SHOW_UI : 0;
		gpsAuto    = A.is(K.AUTO_GPS) && Dev.isGpsOn();
		boolean mobd = (!gpsAuto || !A.is(K.SKIP_MOBDATA)) && A.is(K.AUTO_MOBDATA) && Dev.isMobDataOn();
		boolean wifi = A.is(K.AUTO_WIFI) && A.wifiMan().isWifiEnabled();
		if((wifi || mobd) && A.is(K.SKIP_HOTSPOT) && Dev.isHotspotOn()) wifi = mobd = false;
		else if(mobd && A.is(K.SKIP_TETHER) && Dev.isTetheringOn()) mobd = false;
		wifiTrack = wifi? new WifiTracker()    : null;
		mobdTrack = mobd? new MobDataTracker() : null;
		if(Dev.isBtOn()) {
			btAuto     = A.is(K.AUTO_BT);
			btReverse  = false;
			skipBtConn = A.is(K.SKIP_BT);
		} else {
			btAuto     = false;
			btReverse  = !headsetOn && A.is(K.REVERSE_BT);
		}
		if(autoSpeaker) {
			taskSpeakerOn  = new Task(){ public void run(){ autoSpeaker(true ); }};
			taskSpeakerOff = new Task(){ public void run(){ autoSpeaker(false); }};
		}
		if(!hasAutoDev()) {
			proximEnable = proximDisable = false;
		} else {
			taskDevsOn    = new Task(){ public void run(){ enableDevs(true ); }};
			taskDevsOff   = new Task(){ public void run(){ enableDevs(false); }};
			notifyEnable  = A.is(K.NOTIFY_ENABLE);
			notifyDisable = A.is(K.NOTIFY_DISABLE);
		}
		regProximity();
		regHeadset();
		if(rec) RecService.start(this);
		//A.logd("PhoneListener.initCall: end");
	}

	private void updateHeadset(boolean on, int vol) {
		if(A.is(K.NOTIFY_HEADSET)) {
			A.notify(A.s(on? R.string.msg_headset_on : R.string.msg_headset_off)); 
			if(rec) A.notifyCanc(); 
		}
		if(headsetOn == on) return;
		headsetOn = on;
		if(!offhook) return;
		setHeadsetVolume(on, vol);
		final boolean on2 = on || (lastFar && proximDisable);
		if(autoSpeaker) autoSpeaker(on2);
		enableDevs(on2);
		screenOff(!on2);
		if(rec) RecService.updateHeadset(on);
		//A.logd("headset connected: "+on);
	}

	// incoming call: phone is ringing
	private void onRinging() {
		//A.logd("onRinging");
		outgoing = false;
		final String number = phoneNumber = PhoneReceiver.number;
		final int  ringMode = audioMan.getRingerMode();
		final boolean  ring = ringMode == AudioManager.RINGER_MODE_NORMAL;
		final CallFilter cf = CallFilter.instance();
		// call blocker
		final boolean block = cf.includes(number, "block", false) && (!A.is(K.BLOCK_SKIP) || ring);
		if(block && Blocker.apply(A.geti(K.BLOCK_MODE)))
			return;
		// urgent call
		if(!block && !ring && !headsetOn && cf.includes(number, "urgent", false))
			urgentCall(ringMode);
		// TODO: change ring volume (if activated)
		// delay before auto answer
		final int answer = !block && A.is(K.ANSWER) && (headsetOn || !A.is(K.ANSWER_HEADSET)) && (!A.is(K.ANSWER_SKIP) || ring)
		                   && cf.includes(number, "answer", true)
		                   ? A.geti(K.ANSWER_DELAY) : -1;
		// announce call
		if((answer<0 || answer>3000) && A.is(K.TTS) && (headsetOn || !A.is(K.TTS_HEADSET)) && (!A.is(K.TTS_SKIP) || ring))
			tts = new TTS(number, true, true, false);
		initCall();
		btAdjust();
		// auto answer
		if(answer >= 0) new Task(){ public void run(){ Dev.answerCall(); }}.exec(answer);
	}

	// we have a call!
	private void onOffhook() {
		//A.logd("onOffhook: begin");
		//if(tts != null) tts.stop();
		if(tts != null) { tts.shutdown(); tts = null; }
		if(Blocker.onOffhook()) return;
		offhook = true;
		Dev.enableLock(false);
		if(outgoing) initCall();
		volSolo(true);
		audioMan.setMicrophoneMute(false);	// FIX: remove????
		if(headsetOn) {
			setHeadsetVolume(true, wiredHeadsetOn? volWired : volBt);
		} else {
			if(volPhone >= 0) setVolume(volPhone);
			if(     speakerCall == SPEAKER_CALL_INCOMING) { if( outgoing) speakerCall = 0; }
			else if(speakerCall == SPEAKER_CALL_OUTGOING) { if(!outgoing) speakerCall = 0; }
			if(speakerCall > 0) speakerOnFar();
		}
		if(!proximDisable) enableDevs(false);
		if(outgoing) {
			phoneNumber = PhoneReceiver.number;
			btAdjust();
		}
		if(rec) RecService.checkAutoRec();
		if(!lastFar) screenOff(true);
		//A.logd("onOffhook: end");
	}

	// call completed: restore & shutdown
	private void onIdle() {
		shutdown = true;
		//A.logd("onIdle: begin");
		PickupService.stop();
		if(offhook) {
			Task.stop(TASK_SPEAKER);
			if(!headsetOn && A.is(K.SPEAKER_SILENT_END)) audioMan.setSpeakerphoneOn(false);
		}
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
			if(A.is(K.VIBRATE_END)) Vibra.vibra();
		}
		restoreRinger();
		try { A.telMan().listen(this, LISTEN_NONE); } catch(Exception e) {}
		Blocker.shutdown();
		CallFilter.shutdown();
		if(offhook) enableDevs(true);
		if(rec) Alarmer.runService(Alarmer.ACT_ONIDLE, null);
		//if(rec) RecService.cron();
		if(mobdTrack != null) { mobdTrack.shutdown(); mobdTrack = null; }
		if(wifiTrack != null) { wifiTrack.shutdown(); wifiTrack = null; }
		Task.shutdownWait();
		final boolean btOn = Dev.isBtOn();
		if((btReverse || A.is(K.BT_OFF)) && btOn) { Dev.enableBt(false); A.putc(K.BT_COUNT, 0); }
		else if(!btOn) A.putc(K.BT_COUNT, 0);		// recheck to avoid false counter
		offhook = false;
		phoneNumber = null;
		MainService.stop();
		activeInst  = null;
		//A.logd("onIdle: end");
	}

	private synchronized void enableDevs(boolean enable) {
		if(!enable && headsetOn) return;
		final long now  = SystemClock.elapsedRealtime();
		final long diff = now - devsLastTime;
		if(diff < Conf.DEVS_MIN_RETRY) {
			if(enable == devsLastEnable) return;
			final Task task = enable? taskDevsOn : taskDevsOff;
			if(task != null) task.exec(TASK_DEVS, Math.max(500, (int)diff));
			return;
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

	private synchronized void autoSpeaker(boolean far) {
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

	private void speakerOnFar() {
		if(taskSpeakerOnFar == null) taskSpeakerOnFar = new Task(){ public void run(){
			if(!lastFar) return;
			autoSpeaker(true);
		}};
		if(speakerCallDelay <= 0) {
			taskSpeakerOnFar.exec(0);
			if(!outgoing) return;
		}
		// enable speaker after a little delay (some phones auto-disable speaker on offhook!!)
		if(outgoing && speakerCallDelay<FORCE_AUTOSPEAKER_DELAY) speakerCallDelay = FORCE_AUTOSPEAKER_DELAY;
		taskSpeakerOnFar.exec(TASK_SPEAKER, speakerCallDelay);
		//A.logd("speakerOnFar: delay="+speakerCallDelay);
	}
	
	private void setVolume(int vol) { audioMan.setStreamVolume(AudioManager.STREAM_VOICE_CALL, vol, volFlags); }

	private void setHeadsetVolume(boolean on, int vol) {
		if(vol < 0) return;
		if(!on)
			vol = volPhone;
		else if(volPhone < 0)
			volPhone = volRestore<0 ? audioMan.getStreamVolume(AudioManager.STREAM_VOICE_CALL) : volRestore;
		setVolume(vol);
		//A.logd((on?"preferred":"restored")+" volume set to level "+vol);
	}

	private synchronized void screenOff(boolean off) {
		if(off) {
			if(!screenOff) return;
			Dev.lock();
		} else {
			if(!screenOn) return;
			Dev.wakeScreen();
		}
		//A.logd("screenOff: "+off);
	}
	
	private void volSolo(boolean enable) {
		if(!volSolo) return;
		audioMan.setStreamMute(AudioManager.STREAM_SYSTEM      , enable);
		audioMan.setStreamMute(AudioManager.STREAM_ALARM       , enable);
		audioMan.setStreamMute(AudioManager.STREAM_NOTIFICATION, enable);
		if(!rec) audioMan.setStreamMute(AudioManager.STREAM_MUSIC, enable);		// if we mute media, we cannot record phone call!
		//A.logd("set volume solo: "+enable);
	}
	
	private void btAdjust() {
		if(headsetOn) return;
		boolean btOn;
		if(!btReverse)
			btOn = Dev.isBtOn();
		else {
			btOn = true;
			Dev.enableBt(true);
			final int timeout = A.geti(K.REVERSE_BT_TIMEOUT);
			if(timeout > 0) new Task(){ public void run(){
				if(headsetOn || !btReverse) return;
				Dev.enableBt(false);
				btReverse = false;
			}}.exec(timeout);
		}
		if(btOn) {
			audioMan.setBluetoothScoOn(true);
			//headsetOn |= audioMan.isBluetoothScoOn();		// FIX: remove???
		}
	}

	private void urgentCall(int ringMode) {
		final int     vibrMode = audioMan.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
		final boolean vibrOnly = ringMode==AudioManager.RINGER_MODE_VIBRATE || vibrMode==AudioManager.VIBRATE_SETTING_ONLY_SILENT;
		if(vibrOnly && A.is(K.URGENT_SKIP)) return;
		int ring, vibr;
		switch(A.geti(K.URGENT_MODE)) {
			case 1: ring = AudioManager.RINGER_MODE_NORMAL ; vibr = AudioManager.VIBRATE_SETTING_OFF;         break;
			case 2: ring = AudioManager.RINGER_MODE_VIBRATE; vibr = AudioManager.VIBRATE_SETTING_ONLY_SILENT; break;
			case 3: ring = AudioManager.RINGER_MODE_NORMAL ; vibr = AudioManager.VIBRATE_SETTING_ON;          break;
			default: return;
		}
		changeRinger(ring, vibr, ringMode, vibrMode);
	}

	private boolean changeRinger(int ringNew, int vibrNew, int ringCur, int vibrCur) {
		boolean changed = false;
		if(ringCur != ringNew) {
			audioMan.setRingerMode(ringNew);
			if(ringMode == -1) ringMode = ringCur;
			changed = true;
		}
		if(vibrCur != vibrNew) {
			audioMan.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, vibrNew);
			if(vibrMode == -1) vibrMode = vibrCur;
			changed = true;
		}
		return changed;
	}

	private void restoreRinger() {
		if(ringMode >= 0) {
			ModeReceiver.skip = true;	// FIX??????
			audioMan.setRingerMode(ringMode);
		}
		if(vibrMode >= 0)
			audioMan.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, vibrMode);
	}

	private void regProximity() {
		proximRegistered = proximSensor!=null && (autoSpeaker || screenOff || screenOn || (proximDisable && hasAutoDev()));
		if(!proximRegistered) return;
		final float range = proximSensor.getMaximumRange();
		proximFar = Math.max(Conf.PROXIM_MIN, Math.min(Conf.PROXIM_MAX, Math.abs(range))) - 0.1f;
		// register proximity sensor
		A.sensorMan().registerListener(this, proximSensor, SensorManager.SENSOR_DELAY_NORMAL);
		// register screen off intents for automatic screen on
		if(screenOn) A.app().registerReceiver(screenOffReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context ctx, Intent i) { if(lastFar) Dev.wakeScreen(); }
		}, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		// register screen on intents for automatic screen off
		if(screenOff) A.app().registerReceiver(screenOnReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context ctx, Intent i) { if(!lastFar) Dev.lock(); }
		}, new IntentFilter(Intent.ACTION_SCREEN_ON));
	}
	private void unregProximity() {
		if(!proximRegistered) return;
		proximRegistered = false;
		try { A.sensorMan().unregisterListener(this); } catch(Exception e) {}
		if(screenOff) try { A.app().unregisterReceiver(screenOnReceiver ); } catch(Exception e) {}
		if(screenOn ) try { A.app().unregisterReceiver(screenOffReceiver); } catch(Exception e) {}
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
	public void onCallForwardingIndicatorChanged(boolean cfi) {
		if(!offhook || headsetOn) return;
		if(!lastFar) new Task(){ public void run(){ screenOff(true); }}.exec(500);
		//if(!lastFar) screenOff(true);
		else if(outgoing && speakerCall>0) speakerOnFar();
		//screenOff(!lastFar);	// FIX: remove????
		//A.logd("onCallForwardingIndicatorChanged: "+cfi);
	}

	@Override
	public void onCallStateChanged(int state, String num) {
		switch(state) {
			case TelephonyManager.CALL_STATE_RINGING:
				if(lastCallState != CALL_STATE_NONE) break;		// to skip multiple/concurrent calls
				//A.logd("onRinging");
				onRinging();
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				if(offhook) break;		// check against "offhook" to skip multiple/concurrent calls
				//A.logd("onOffhook");
				onOffhook();
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				//A.logd("onIdle");
				onIdle();
				break;
		}
		//A.logd("onCallStateChanged: state="+state+", lastCallState="+lastCallState);
		lastCallState = state;
	}

	//---- SensorEventListener implementation

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }

	@Override
	public void	onSensorChanged(SensorEvent evt) {
		if(shutdown) return;
		final float   val = evt.values[0];
		final boolean far = proximReverse? val<proximFar : val>=proximFar;
		//A.logd("proximity sensor: value="+val+", far="+far+", lastFar="+lastFar);
		//if(far == lastFar) return;
		if(offhook && !headsetOn) {
			// auto switch handsfree
			if(autoSpeaker) {
				if(far) {
					if(speakerOn) {
						taskSpeakerOn.exec(TASK_SPEAKER, speakerDelay);
						speakerOn = --speakerOnCount != 0;
					}
				} else {
					if(speakerOff) {
						taskSpeakerOff.exec(TASK_SPEAKER, 0);
						speakerOff = --speakerOffCount != 0;
					}
				}
			}
			// auto switch devices
			if(far) { if(proximEnable ) taskDevsOn .exec(TASK_DEVS,  enableDelay); }
			else    { if(proximDisable) taskDevsOff.exec(TASK_DEVS, disableDelay); }
			// auto switch screen
			screenOff(!far);
		}
		// save last proximity state
		lastFar = far;
	}

}
