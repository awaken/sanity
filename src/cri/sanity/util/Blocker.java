package cri.sanity.util;

import cri.sanity.*;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;


public final class Blocker
{
	public  static final int MODE_HANGUP = 0;
	public  static final int MODE_FLIGHT = 1;
	public  static final int MODE_SILENT = 2;
	private static final int MODE_NONE   = -1;
	private static final int NID         = 3;

	private static int mode = MODE_NONE;
	private static int ring;

	public static final boolean apply(int mode)
	{
		switch(mode) {
			case MODE_HANGUP:
				try {
					if(!Dev.iTel().endCall()) return false;
				} catch(Exception e) {
					return false;
				}
				break;
			case MODE_FLIGHT:
				if(!Dev.enableFlightMode(true)) return false;
				break;
			case MODE_SILENT:
				ring = A.audioMan().getRingerMode();
				if(ring == AudioManager.RINGER_MODE_SILENT) return false;
				A.audioMan().setRingerMode(AudioManager.RINGER_MODE_SILENT);
				Blocker.mode = mode;
			default:
				return false;
		}
		Blocker.mode = mode;
		return true;
	}
	
	public static final void shutdown()
	{
		switch(mode) {
			case MODE_HANGUP:
				break;
			case MODE_FLIGHT:
				final int delay = A.geti(K.BLOCK_RESUME);
				if(delay > 0) delayUnFlight(delay);
				else Dev.enableFlightMode(false);
				break;
			case MODE_SILENT:
				A.audioMan().setRingerMode(ring);
				break;
			default:
				return;
		}
		if(A.is(K.BLOCK_NOTIFY)) notifyBlock();
		mode = MODE_NONE;
	}

	private static void delayUnFlight(int delay)
	{
		final Context ctx = A.app();
		Intent i = new Intent(ctx, AlarmReceiver.class);
		i.setAction(AlarmReceiver.ACT_UNFLIGHT);
		A.alarmMan().set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				             SystemClock.elapsedRealtime() + delay,
				             PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_ONE_SHOT));
	}

	private static void notifyBlock()
	{
		String num  = PhoneListener.getActiveInstance().phoneNumber();
		String name = A.empty(num)? num : CallFilter.searchName(num);
		if(A.empty(name)) name = A.empty(num)? A.gets(K.TTS_ANONYM) : num;
		A.notify(name, A.name()+": "+A.s(R.string.block_cat), NID, R.drawable.ic_block_bar);
	}

}
