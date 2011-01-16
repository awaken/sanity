package cri.sanity;

import android.os.Bundle;


public class ScreenDevices extends ActivityScreen
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setEnabled("mobdata_skip", !A.is("gps"));
		on("gps", new Change(){ boolean on(){
			setEnabled("mobdata_skip", !((Boolean)value).booleanValue());
			return true;
		}});
	}
}
