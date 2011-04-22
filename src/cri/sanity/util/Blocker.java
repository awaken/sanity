package cri.sanity.util;

import java.io.FileWriter;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import cri.sanity.*;
import cri.sanity.screen.*;


public final class Blocker
{
	public  static final char SEP           = Conf.BLOCK_SEP;
	public  static final int MODE_HANGUP    = 0;
	public  static final int MODE_RADIO     = 1;
	public  static final int MODE_SILENT    = 2;
	public  static final int MODE_ANSWER    = 3;
	private static final int MODE_NONE      = -1;
	private static final int NID            = 3;
	private static final int ANSWER_TIMEOUT = 60*1000;

	private static int     mode = MODE_NONE;
	private static boolean pickup;
	private static String  name, num;

	//---- public api

	//public static final boolean isBlocking() { return mode != MODE_NONE; }

	public static final boolean apply(int blockMode)
	{
		name = num = null;
		switch(blockMode) {
			case MODE_HANGUP:
				if(pickup = A.is(K.BLOCK_PICKUP)) Dev.answerCall();
				else if(!Dev.endCall()) { blockFailed(); return false; }
				break;
			case MODE_RADIO:
				if(pickup = A.is(K.BLOCK_PICKUP)) Dev.answerCall();
				else if(!Dev.enableFlightMode(true)) { blockFailed(); return false; }
				break;
			case MODE_SILENT:
				final PhoneListener pl = PhoneListener.getActiveInstance();
				if(pl==null || !pl.changeRinger(AudioManager.RINGER_MODE_SILENT, AudioManager.VIBRATE_SETTING_OFF)) return false;
				new Task(){ public void run(){ Dev.lock(); }}.exec(Conf.BLOCK_LOCK_DELAY);
				break;
			case MODE_ANSWER:
				Dev.answerCall();
				break;
			default:
				blockFailed();
				return false;
		}
		mode = blockMode;
		return true;
	}

	public static final boolean onOffhook()
	{
		switch(mode) {
			case MODE_NONE:
				return false;
			case MODE_HANGUP:
				if(pickup) { if(!Dev.endCall()) blockFailed(); }
				break;
			case MODE_RADIO:
				if(pickup) { if(!Dev.enableFlightMode(true)) blockFailed(); }
				break;
			case MODE_SILENT:
				break;
			case MODE_ANSWER:
				final Runnable runMute = new Runnable(){ public void run(){
					final AudioManager am = A.audioMan();
					am.setMode(AudioManager.MODE_NORMAL);
					am.setStreamMute(AudioManager.STREAM_VOICE_CALL, true);
					am.setStreamSolo(AudioManager.STREAM_ALARM, true);
					am.setMicrophoneMute(true);
				}};
				runMute.run();
				BlankActivity.postSingleton(runMute);
				BlankActivity.postSingleton(new Runnable(){ public void run(){ Dev.lock(); }});
				Intent i = new Intent(A.app(), BlankActivity.class);
				i.putExtra(BlankActivity.EXTRA_BLOCK, true);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				A.app().startActivity(i);
				new Task(){ public void run(){ Dev.endCall(); }}.exec(ANSWER_TIMEOUT);
				break;
			default:
				return false;
		}
		return true;
	}

	public static final void shutdown()
	{
		Alarmer.stop(Alarmer.ACT_FLIGHTOFF);
		switch(mode) {
			case MODE_HANGUP:
			case MODE_SILENT:
				break;
			case MODE_RADIO:
				final int delay = A.geti(K.BLOCK_RESUME);
				if(delay > 0) Alarmer.exec(Alarmer.ACT_FLIGHTOFF, delay);
				else Dev.enableFlightMode(false);
				break;
			case MODE_ANSWER:
				final AudioManager am = A.audioMan();
				am.setMode(AudioManager.MODE_NORMAL);
				am.setStreamSolo(AudioManager.STREAM_ALARM, false);
				am.setStreamMute(AudioManager.STREAM_VOICE_CALL, false);
				am.setMicrophoneMute(false);
				//am.setMode(AudioManager.MODE_IN_CALL);
				final BlankActivity ba = BlankActivity.getInstance();
				if(ba != null) ba.postFinish();
				break;
			default:
				return;
		}
		if(A.is(K.BLOCK_NOTIFY)) notification(false);
		log();
		Dev.lock();
		mode = MODE_NONE;
	}

	public static final void notification(boolean sms)
	{
		String msg   = A.name()+": "+A.s(sms? R.string.blocksms_cat : R.string.block_cat);
		String title = name();
		if(num.length() > 0) title += " (" + num + ')';
		final Context ctx = A.app();
		Intent i = new Intent(ctx, sms? SmsHistoryActivity.class : CallHistoryActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Notification notif = new Notification(R.drawable.ic_block_bar, msg, A.time());
		notif.flags = Notification.FLAG_AUTO_CANCEL;
		notif.setLatestEventInfo(ctx, title, msg, PendingIntent.getActivity(ctx, 0, i, 0));
		A.notifMan().notify(NID, notif);
	}
	
	//---- private api

	private static void blockFailed()
	{
		A.notify(A.s(R.string.err_block));
		mode = MODE_NONE;
	}

	private static void log()
	{
		try {
			final FileWriter fw = new FileWriter(A.sdcardDir()+'/'+Conf.BLOCK_FN, true);
			fw.append(A.date() + SEP + name() + SEP + num() + '\n');
			fw.flush();
			fw.close();
		} catch(Exception e) {}
	}
	
	private static String num () { if(num  == null) readNameNum(); return num ; }
	private static String name() { if(name == null) readNameNum(); return name; }
	
	private static void readNameNum()
	{
		final CallFilter cf = PhoneListener.isRunning() ? CallFilter.instance() : SmsReceiver.callFilter();
		num = cf.lastNum();
		if(num == null) num = "";
		if(num.length() <= 0)
			name = A.s(R.string.anonymous);
		else {
			name = cf.searchName(num);
			if(A.empty(name)) name = A.s(R.string.unknown);
		}
	}
	
}
