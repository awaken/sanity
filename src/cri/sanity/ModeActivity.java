package cri.sanity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import cri.sanity.util.*;


public class ModeActivity extends Activity implements OnDismissListener
{
	private static final String PREFIX_TIME = "priv_mode_time_";
	private static final String EXTRA_FORCE = "force";

	private static boolean started = false;
	private static boolean running = false;
	private String action, msg, forceMethod;

	public static final void start(String action, boolean force)
	{
		started  = true;
		Intent i = new Intent(A.app(), ModeActivity.class);
		i.setAction(action);
		i.putExtra(EXTRA_FORCE, force);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		A.app().startActivity(i);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if(running || !init()) {
			if(!running) startActivity(new Intent(A.app(), MainActivity.class));
			finish();
			return;
		}
		running = true;
		final String keyTime = PREFIX_TIME + action;
		final int time = A.geti(keyTime);
		final int hour = (time >> 8) & 0xff;
		final int mins = (time     ) & 0xff;
		Alert.time(
			msg,
			new Alert.Timed(hour, mins) {
				@Override
				public void on() {
					if(forceMethod != null) try { ModeActivity.class.getMethod(forceMethod).invoke(null); } catch(Exception e) {}
					A.putc(keyTime, hour<<8 | mins);
					final long delay = hour*(3600l*1000l) + mins*(60l*1000l);
					Alarmer.exec(action, delay);
					if(delay >= 60000) A.toast(String.format(A.s(R.string.msg_mode_scheduled), DateFormat.format("kk:mm", A.time()+delay)));
				}
			},
			this
		).setOnDismissListener(this);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		running = false;
	}

	private boolean init()
	{
		if(!started) return false;
		started = false;
		final Intent i = getIntent();
		if(i == null) return false;
		action = i.getAction();
		return checkAction(i.getBooleanExtra(EXTRA_FORCE, false));
	}

	private boolean checkAction(boolean force)
	{
		if(Alarmer.ACT_SILENTLIMIT.equals(action)) {
			if(!force && A.audioMan().getRingerMode()==AudioManager.RINGER_MODE_NORMAL) return false;
			msg = A.s(R.string.ask_silent_limit);
			forceMethod = force? "forceSilent" : null;
		}
		else if(Alarmer.ACT_FLIGHTOFF.equals(action)) {
			if(!force && !Dev.isFlightModeOn()) return false;
			msg = A.s(R.string.ask_airplane_limit);
			forceMethod = force? "forceFlight" : null;
		}
		else return false;
		return true;
	}

	public static void forceSilent() {
		if(A.audioMan().getRingerMode() != AudioManager.RINGER_MODE_NORMAL) return;
		ModeReceiver.skip = true;
		A.audioMan().setRingerMode(AudioManager.RINGER_MODE_SILENT);
	}
	public static void forceFlight() {
		if(Dev.isFlightModeOn()) return;
		ModeReceiver.skip = true;
		Dev.enableFlightMode(true);
	}

	@Override
	public void onDismiss(DialogInterface dlg)
	{
		finish();
	}

}
