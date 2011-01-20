package cri.sanity;

import android.os.Bundle;


public class ScreenDevices extends ActivityScreen
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setEnabled(P.SKIP_MOBDATA, A.is(P.AUTO_MOBDATA) && !A.is(P.AUTO_GPS));

		on(P.AUTO_MOBDATA, new Change(){ boolean on(){
			setEnabled(P.SKIP_MOBDATA, ((Boolean)value).booleanValue() && !A.is(P.AUTO_GPS));
			return true;
		}});

		on(P.AUTO_GPS, new Change(){ boolean on(){
			setEnabled(P.SKIP_MOBDATA, !((Boolean)value).booleanValue() && A.is(P.AUTO_MOBDATA));
			return true;
		}});
	}

}
