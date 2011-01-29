package cri.sanity;

import android.os.Bundle;
import android.preference.ListPreference;


public class ScreenVolume extends ActivityScreen
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
  	final CharSequence[] av = new CharSequence[m+1];
  	final CharSequence[] ae = new CharSequence[m+1];
  	av[0] = "0";
  	av[1] = "1";
  	av[m] = Integer.toString(m);
  	ae[0] = A.tr(R.string.nochange);
  	ae[1] = lev + av[1] + " ("+A.tr(R.string.min)+')';
  	ae[m] = lev + av[m] + " ("+A.tr(R.string.max)+')';
  	for(int i=2; i<m; ++i) {
  		av[i] = Integer.toString(i);
  		ae[i] = lev + av[i];
  	}
  	for(String k : new String[]{ K.VOL_PHONE, K.VOL_WIRED, K.VOL_BT }) {
  		final ListPreference lp = (ListPreference)findPref(k);
  		lp.setEntries    (ae);
  		lp.setEntryValues(av);
  	}
	}
	
}
