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
		if(i==null || MainService.isRunning() || !A.isEnabled()) return;
		String act = i.getAction();
		if(act == null) return;
		if(skip) { skip = false; return; }

		String  enable;
		int     nid;
		boolean abort;

		if(AudioManager.RINGER_MODE_CHANGED_ACTION.equals(act)) {
			act    = Alarmer.ACT_SILENTLIMIT;
			enable = K.SILENT_LIMIT;
			nid    = ModeActivity.NID_SILENT;
			abort  = i.getIntExtra(AudioManager.EXTRA_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL) == AudioManager.RINGER_MODE_NORMAL;
		}
		else if(Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(act)) {
			act    = Alarmer.ACT_FLIGHTOFF;
			enable = K.AIRPLANE_LIMIT;
			nid    = ModeActivity.NID_FLIGHT;
			abort  = !i.getBooleanExtra("state", false);
		}
		else return;

		if(abort) {
			Alarmer.stop(act);
			A.notifyCanc(nid);
		}	else if(A.is(enable))
			ModeActivity.start(act, false);
	}

}
