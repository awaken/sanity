package cri.sanity.screen;

import cri.sanity.*;
import cri.sanity.util.Alert;
import cri.sanity.util.Dev;
import android.os.Bundle;


public class DevicesActivity extends ScreenActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final boolean mobdata = A.is(K.AUTO_MOBDATA);
		setEnabled(K.SKIP_MOBDATA, mobdata && !A.is(K.AUTO_GPS));
		setEnabled(K.SKIP_TETHER , mobdata && Dev.isTetheringSupported());
		setEnabled(K.SKIP_HOTSPOT, (mobdata || A.is(K.AUTO_WIFI)) && Dev.isHotspotSupported());

		on(K.AUTO_MOBDATA, A.SDK > 8
			? new Change(){ public boolean on(){
				final boolean auto = (Boolean)value;
				if(auto) Alert.msg(A.s(R.string.msg_incompatible));
				setEnabled(K.SKIP_MOBDATA, false);
				setEnabled(K.SKIP_TETHER , false);
				setEnabled(K.SKIP_HOTSPOT, A.is(K.AUTO_WIFI));
				return !auto;
			}}
		  : new Change(){ public boolean on(){
				final boolean auto = (Boolean)value;
				setEnabled(K.SKIP_MOBDATA, auto && !A.is(K.AUTO_GPS));
				setEnabled(K.SKIP_TETHER , auto && Dev.isTetheringSupported());
				setEnabled(K.SKIP_HOTSPOT, (auto || A.is(K.AUTO_WIFI)) && Dev.isHotspotSupported());
				return true;
			}}
		);

		on(K.AUTO_WIFI, new Change(){ public boolean on(){
			setEnabled(K.SKIP_HOTSPOT, ((Boolean)value || A.is(K.AUTO_MOBDATA)) && Dev.isHotspotSupported());
			return true;
		}});

		on(K.AUTO_GPS, new Change(){ public boolean on(){
			setEnabled(K.SKIP_MOBDATA, !(Boolean)value && A.is(K.AUTO_MOBDATA));
			return true;
		}});
	}

}
