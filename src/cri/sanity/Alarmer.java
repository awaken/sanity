package cri.sanity;

import cri.sanity.util.*;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;


public class Alarmer extends BroadcastReceiver
{
	public  static final String ACT_FLIGHTOFF   = "actFlightOff";
	public  static final String ACT_SILENTLIMIT = "actSilentLimit";
	private static final int    RETRY_TIME      = 60*1000;

	@Override
	public void onReceive(Context ctx, Intent i)
	{
		if(i == null) return;
		final String action = i.getAction();
		if(action == null) return;
		try { Alarmer.class.getMethod(action).invoke(null); } catch(Exception e) {}
	}

	public static final void exec(String action, long delay)
	{
		final PendingIntent pi = alarmIntent(action);
		final AlarmManager  am = A.alarmMan();
		am.cancel(pi);
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+delay, pi);
	}

	public static final void stop(String action)
	{
		A.alarmMan().cancel(alarmIntent(action));
	}

	private static PendingIntent alarmIntent(String action)
	{
		final Context ctx = A.app();
		Intent i = new Intent(ctx, Alarmer.class);
		i.setAction(action);
		return PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_ONE_SHOT);
	}

	public static void actFlightOff()
	{
		if(PhoneListener.isRunning()) return;
		if(Dev.isFlightModeOn()) Dev.enableFlightMode(false);
	}

	public static void actSilentLimit()
	{
		if(PhoneListener.isRunning())
			exec(ACT_SILENTLIMIT, RETRY_TIME);
		else
			A.audioMan().setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}

}
