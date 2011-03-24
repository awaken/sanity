package cri.sanity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import cri.sanity.util.*;


public class RingerActivity extends Activity implements OnDismissListener
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final String key = "priv_" + K.SILENT_LIMIT;
		final int time = A.geti(key);
		final int hour = (time >> 8) & 0xff;
		final int mins = (time     ) & 0xff;
		Alert.time(
			A.s(R.string.ask_silent_limit),
			new Alert.Timed(hour, mins) {
				@Override
				public void on() {
					Alarmer.exec(Alarmer.ACT_SILENTLIMIT, hour*(3600l*1000l)+mins*(60l*1000l));
					A.putc(key, hour<<8 | mins);
				}
			},
			this
		).setOnDismissListener(this);
	}

	@Override
	public void onDismiss(DialogInterface dlg)
	{
		finish();
	}

}
