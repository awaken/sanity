package cri.sanity.util;

import java.io.FileWriter;

import android.content.Intent;
import android.media.AudioManager;
import android.text.format.DateFormat;
import cri.sanity.*;


public final class Blocker
{
	public  static final char SEP = '|';
	public  static final int MODE_HANGUP = 0;
	public  static final int MODE_RADIO  = 1;
	public  static final int MODE_SILENT = 2;
	public  static final int MODE_ANSWER = 3;
	private static final int MODE_NONE   = -1;

	private static final int NID            = 3;
	private static final int ANSWER_TIMEOUT = 60*1000;

	private static int    mode = MODE_NONE;
	private static int    ring;
	private static String name, num;

	//---- public api

	public static final boolean apply(int mode)
	{
		name = num = null;
		switch(mode) {
			case MODE_HANGUP:
				try {
					if(!Dev.iTel().endCall()) return false;
				} catch(Exception e) {
					return false;
				}
				break;
			case MODE_RADIO:
				if(!Dev.enableFlightMode(true)) return false;
				break;
			case MODE_SILENT:
				ring = A.audioMan().getRingerMode();
				if(ring == AudioManager.RINGER_MODE_SILENT) return false;
				A.audioMan().setRingerMode(AudioManager.RINGER_MODE_SILENT);
				new Task(){ public void run(){ try { A.devpolMan().lockNow(); } catch(Exception e) {} }}.exec(Conf.BLOCK_LOCK_DELAY);
				break;
			case MODE_ANSWER:
				try {
					Dev.iTel().answerRingingCall();
				} catch(Exception e) {
					return false; 
				}
				break;
			default:
				return false;
		}
		Blocker.mode = mode;
		return true;
	}

	public static final boolean onOffhook()
	{
		if(mode == MODE_NONE  ) return false;
		if(mode != MODE_ANSWER) return true;
		final Runnable runMute = new Runnable(){ public void run(){
			final AudioManager am = A.audioMan();
			am.setMode(AudioManager.MODE_NORMAL);
			am.setStreamMute(AudioManager.STREAM_VOICE_CALL, true);
			am.setStreamSolo(AudioManager.STREAM_ALARM, true);
			am.setMicrophoneMute(true);
		}};
		runMute.run();
		BlankActivity.postSingleton(runMute);
		BlankActivity.postSingleton(new Runnable(){ public void run(){ try { A.devpolMan().lockNow(); } catch(Exception e) {} }});
		Intent i = new Intent(A.app(), BlankActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		A.app().startActivity(i);
		new Task(){ public void run(){
			try {
				Dev.iTel().endCall();
			} catch(Exception e) { try {
				Dev.enableFlightMode(true );
				Dev.enableFlightMode(false);
			} catch(Exception e2) {}}
		}}.exec(ANSWER_TIMEOUT);
		return true;
	}

	public static final void shutdown()
	{
		switch(mode) {
			case MODE_HANGUP:
				break;
			case MODE_RADIO:
				final int delay = A.geti(K.BLOCK_RESUME);
				if(delay > 0) Alarmer.exec(Alarmer.ACT_FLIGHTOFF, delay);
				else Dev.enableFlightMode(false);
				break;
			case MODE_SILENT:
				A.audioMan().setRingerMode(ring);
				break;
			case MODE_ANSWER:
				final AudioManager am = A.audioMan();
				am.setStreamSolo(AudioManager.STREAM_ALARM, false);
				am.setStreamMute(AudioManager.STREAM_VOICE_CALL, false);
				am.setMicrophoneMute(false);
				//am.setMode(AudioManager.MODE_IN_CALL);
				BlankActivity ba = BlankActivity.getInstance();
				if(ba != null) ba.postFinish();
				break;
			default:
				return;
		}
		if(A.is(K.BLOCK_NOTIFY)) notification();
		log();
		try { A.devpolMan().lockNow(); } catch(Exception e) {}
		mode = MODE_NONE;
	}
	
	//---- private api
	
	private static void notification()
	{
		String title = name();
		if(num.length() > 0) title += " (" + num + ')';
		A.notify(title, A.name()+": "+A.s(R.string.block_cat), NID, R.drawable.ic_block_bar);
	}
	
	private static void log()
	{
		try {
			FileWriter fw = new FileWriter(A.sdcardDir()+'/'+Conf.BLOCK_FN, true);
			String line = DateFormat.format(Conf.DATE_PATTERN, A.time()).toString() + SEP + name() + SEP + num() + '\n';
			fw.append(line);
			fw.flush();
			fw.close();
		} catch(Exception e) {}
	}
	
	private static String num () { if(num  == null) readNameNum(); return num ; }
	private static String name() { if(name == null) readNameNum(); return name; }
	
	private static void readNameNum()
	{
		num = PhoneListener.getActiveInstance().phoneNumber();
		if(num == null) num = "";
		if(num.length() <= 0)
			name = A.gets(K.TTS_ANONYM);
		else {
			name = CallFilter.searchName(num);
			if(A.empty(name)) name = A.gets(K.TTS_UNKNOWN);
		}
	}
	
}
