package cri.sanity.screen;

import cri.sanity.*;
import cri.sanity.util.Dev;
import android.os.Bundle;


public class DevicesActivity extends ScreenActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setEnabled(K.SKIP_MOBDATA, A.is(K.AUTO_MOBDATA) && !A.is(K.AUTO_GPS));
		setEnabled(K.SKIP_HOTSPOT, A.is(K.AUTO_MOBDATA) && Dev.isHotspotSupported());
		setEnabled(K.SKIP_TETHER , A.is(K.AUTO_MOBDATA) && Dev.isTetheringSupported());

		on(K.AUTO_MOBDATA, new Change(){ public boolean on(){
			final boolean auto = (Boolean)value;
			setEnabled(K.SKIP_MOBDATA, auto && !A.is(K.AUTO_GPS));
			setEnabled(K.SKIP_HOTSPOT, auto && Dev.isHotspotSupported());
			setEnabled(K.SKIP_TETHER , auto && Dev.isTetheringSupported());
			return true;
		}});

		on(K.AUTO_GPS, new Change(){ public boolean on(){
			final boolean auto = (Boolean)value;
			setEnabled(K.SKIP_MOBDATA, !auto && A.is(K.AUTO_MOBDATA));
			return true;
		}});
	}

}
