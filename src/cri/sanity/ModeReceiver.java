package cri.sanity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;


public class ModeReceiver extends BroadcastReceiver
{
	public static boolean skip = false;

	@Override
	public void onReceive(Context ctx, Intent i)
	{
		if(i==null || PhoneListener.isRunning()) return;
		String act = i.getAction();
		if(act == null) return;
		boolean abort;

		if(AudioManager.RINGER_MODE_CHANGED_ACTION.equals(act)) {
			if(!A.is(K.SILENT_LIMIT)) return;
			act   = Alarmer.ACT_SILENTLIMIT;
			abort = i.getIntExtra(AudioManager.EXTRA_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL) == AudioManager.RINGER_MODE_NORMAL;
		}
		else if(Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(act)) {
			if(!A.is(K.AIRPLANE_LIMIT)) return;
			act   = Alarmer.ACT_FLIGHTOFF;
			abort = !i.getBooleanExtra("state", false);
		}
		else return;

		if(skip) { skip = false; return; }
		Alarmer.stop(act);
		if(abort) return;
		ModeActivity.start(act, false);
	}

}
