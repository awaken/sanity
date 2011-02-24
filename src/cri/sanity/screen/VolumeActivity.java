package cri.sanity.screen;

import cri.sanity.*;
import android.os.Bundle;


public class VolumeActivity extends ScreenActivity
{
	private static final String VOL_PHONE = K.VOL_PHONE + K.WS;
	private static final String VOL_WIRED = K.VOL_WIRED + K.WS;
	private static final String VOL_BT    = K.VOL_BT    + K.WS;
	
	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setupVolumeLevels();
  }

	private void setupVolumeLevels()
	{
  	// setup volume ranges for ListPreferences
  	final String lev = A.s(R.string.level) + ' ';
  	final int m = Dev.getVolumeMax(Dev.VOL_CALL);
  	final int n = m + 1;
  	final CharSequence[] av = new CharSequence[n+1];
  	final CharSequence[] ae = new CharSequence[n+1];
  	av[0] = "-1";
  	av[1] = "0";
  	av[n] = Integer.toString(m);
  	ae[0] = A.s(R.string.nochange);
  	ae[1] = lev + av[1] + " ("+A.s(R.string.min)+')';
  	ae[n] = lev + av[n] + " ("+A.s(R.string.max)+')';
  	for(int i=2; i<n; i++) {
  		av[i] = Integer.toString(i-1);
  		ae[i] = lev + av[i];
  	}
  	for(String k : new String[]{ VOL_PHONE, VOL_WIRED, VOL_BT }) {
  		final PrefList p = (PrefList)pref(k);
  		p.setEntries    (ae);
  		p.setEntryValues(av);
  		p.update();
  	}
	}
	
}
