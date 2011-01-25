package cri.sanity;

import android.os.Bundle;


public class ScreenDevices extends ActivityScreen
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setEnabled(P.SKIP_MOBDATA, A.is(P.AUTO_MOBDATA) && !A.is(P.AUTO_GPS));
		setEnabled(P.SKIP_HOTSPOT, A.is(P.AUTO_MOBDATA) && Dev.isHotspotSupported());
		setEnabled(P.SKIP_TETHER , A.is(P.AUTO_MOBDATA) && Dev.isTetheringSupported());

		on(P.AUTO_MOBDATA, new Change(){ boolean on(){
			final boolean auto = (Boolean)value;
			setEnabled(P.SKIP_MOBDATA, auto && !A.is(P.AUTO_GPS));
			setEnabled(P.SKIP_HOTSPOT, auto && Dev.isHotspotSupported());
			setEnabled(P.SKIP_TETHER , auto && Dev.isTetheringSupported());
			return true;
		}});

		on(P.AUTO_GPS, new Change(){ boolean on(){
			final boolean auto = (Boolean)value;
			setEnabled(P.SKIP_MOBDATA, !auto && A.is(P.AUTO_MOBDATA));
			return true;
		}});
	}

}
