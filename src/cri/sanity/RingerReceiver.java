package cri.sanity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;


public class RingerReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context ctx, Intent i)
	{
		if(i==null || PhoneListener.isRunning() || !A.isEnabled() || !A.is(K.SILENT_LIMIT)) return;
		final int mode = i.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -666);
		if(mode == -666) return;
		Alarmer.stop(Alarmer.ACT_SILENTLIMIT);
		if(mode == AudioManager.RINGER_MODE_NORMAL) return;
		i = new Intent(A.app(), RingerActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		A.app().startActivity(i);
	}
}
