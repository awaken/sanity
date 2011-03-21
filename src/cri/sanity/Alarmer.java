package cri.sanity;

import cri.sanity.util.*;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;


public class Alarmer extends BroadcastReceiver
{
	public  static final String ACT_FLIGHTOFF = "flightOff";
	private static final String KEY_PREFIX    = "priv_alarmer_";
	private static final int    TOLERANCE     = 1000;

	@Override
	public void onReceive(Context ctx, Intent i)
	{
		final String act = i.getAction();
		if(act == null) return;
		final String key = KEY_PREFIX + act;
		if(!A.has(key) || SystemClock.elapsedRealtime()<A.getl(key)-TOLERANCE) return;
		try { Alarmer.class.getMethod(act).invoke(null); } catch(Exception e) {}
		A.delc(key);
	}

	public static final void exec(String action, long delay)
	{
		final Context ctx = A.app();
		Intent i = new Intent(ctx, Alarmer.class);
		i.setAction(action);
		final long when = SystemClock.elapsedRealtime() + delay;
		A.alarmMan().set(AlarmManager.ELAPSED_REALTIME_WAKEUP, when, PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_ONE_SHOT));
		A.putc(KEY_PREFIX + action, when);
	}

	static void flightOff()
	{
		if(PhoneListener.isRunning()) return;
		if(Dev.isFlightModeOn()) Dev.enableFlightMode(false);
	}

}
