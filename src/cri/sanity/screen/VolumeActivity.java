package cri.sanity.screen;

import cri.sanity.*;
import android.os.Bundle;
import android.preference.ListPreference;


public class VolumeActivity extends ScreenActivity
{
	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setupVolumeLevels();
  }

	private void setupVolumeLevels()
	{
  	// setup volume ranges for ListPreferences
  	final String lev = A.tr(R.string.level) + ' ';
  	final int m = Dev.getVolumeMax(Dev.VOL_CALL);
  	final int n = m + 1;
  	final CharSequence[] av = new CharSequence[n+1];
  	final CharSequence[] ae = new CharSequence[n+1];
  	av[0] = "-1";
  	av[1] = "0";
  	av[n] = Integer.toString(m);
  	ae[0] = A.tr(R.string.nochange);
  	ae[1] = lev + av[1] + " ("+A.tr(R.string.min)+')';
  	ae[n] = lev + av[n] + " ("+A.tr(R.string.max)+')';
  	for(int i=2; i<n; i++) {
  		av[i] = Integer.toString(i-1);
  		ae[i] = lev + av[i];
  	}
  	for(String k : new String[]{ K.VOL_PHONE, K.VOL_WIRED, K.VOL_BT }) {
  		final ListPreference lp = (ListPreference)pref(k);
  		lp.setEntries    (ae);
  		lp.setEntryValues(av);
  	}
	}
	
}
