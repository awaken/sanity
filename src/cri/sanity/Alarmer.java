package cri.sanity;

import cri.sanity.util.*;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;


public final class Alarmer extends BroadcastReceiver
{
	public  static final String ACT_SILENTLIMIT = "actSilentLimit";
	public  static final String ACT_FLIGHTOFF   = "actFlightOff";
	public  static final String ACT_SETPROFILE  = "actSetProfile";
	public  static final String ACT_ONIDLE      = "actOnIdleAsync";
	public  static final String ACT_BOOT        = "actBootAsync";
	public  static final String EXTRA_PROFILE   = "prf";
	private static final int    RETRY_TIME      = 60*1000;

	private Context ctx;
	private Bundle  extras;

	public Alarmer() {}
	public Alarmer(Bundle extras) { this.extras = extras; }

	//---- receiver implementation

	@Override
	public void onReceive(Context ctx, Intent i) {
		if(i == null) return;
		final String action = i.getAction();
		if(action == null) return;
		if(action.endsWith("Async"))
			runService(i);
		else {
			this.ctx    = ctx;
			this.extras = i.getExtras();
			runAction(action);
		}
	}

	//---- public api

	public static final void exec(String action, long delay) {
		exec(action, null, delay, true);
	}
	public static final void exec(String action, long delay, boolean replace) {
		exec(action, null, delay, replace);
	}
	public static final void exec(String action, Bundle extras, long delay, boolean replace) {
		final PendingIntent pi = alarmIntent(action, extras);
		final AlarmManager  am = A.alarmMan();
		if(replace) am.cancel(pi);
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+delay, pi);
	}

	public static void stop(String action) {
		A.alarmMan().cancel(alarmIntent(action));
	}

	public static void runService(String action, Bundle extras) {
		final Intent i = new Intent(action);
		if(extras != null) i.putExtras(extras);
		runService(i);
	}
	public final static void runService(Intent i) {
		final Context ctx = A.app();
		i.setClass(ctx, AlarmService.class);
		ctx.startService(i);
	}

	//---- private api

	public void runAction(String action) {
		try { Alarmer.class.getMethod(action).invoke(this); } catch(Exception e) {}
	}

	private static PendingIntent alarmIntent(String action) {
		return alarmIntent(action, null);
	}
	private static PendingIntent alarmIntent(String action, Bundle extras) {
		final Context ctx = A.app();
		Intent i = new Intent(ctx, Alarmer.class);
		i.setAction(action);
		if(extras != null) i.putExtras(extras);
		return PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_ONE_SHOT);
	}

	//---- actions

	public final void actSilentLimit() {
		if(PhoneListener.isRunning())
			exec(ACT_SILENTLIMIT, RETRY_TIME);
		else {
			ModeReceiver.skip = true;
			A.audioMan().setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			A.notifyCanc(ModeActivity.NID_SILENT);
		}
	}

	public final void actFlightOff() {
		if(PhoneListener.isRunning() || !Dev.isFlightModeOn()) return;
		ModeReceiver.skip = true;
		Dev.enableFlightMode(false);
		A.notifyCanc(ModeActivity.NID_FLIGHT);
	}
	
	public final void actSetProfile() {
		if(extras == null) return;
		final String prf = extras.getString(EXTRA_PROFILE);
		if(A.empty(prf)) return;
		// TODO
		//P.load
		A.toast(ctx, String.format(A.s(R.string.msg_prf_restore_ok), prf));
	}

	public final void actOnIdleAsync() {
		if(A.is(K.REC)) RecService.cron();
		if(A.is(K.QUICK_START) && !BootService.isRunning())
			A.app().startService(new Intent(A.app(), BootService.class));
	}

	public final void actBootAsync() {
		actOnIdleAsync();
	}

}
