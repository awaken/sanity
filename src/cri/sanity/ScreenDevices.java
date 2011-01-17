package cri.sanity;

import android.os.Bundle;


public class ScreenDevices extends ActivityScreen
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setEnabled("mobdata_skip", A.is("mobdata") && !A.is("gps"));
		on("gps", new Change(){ boolean on(){
			setEnabled("mobdata_skip", !((Boolean)value).booleanValue() && A.is("mobdata"));
			return true;
		}});
		on("mobdata", new Change(){ boolean on(){
			setEnabled("mobdata_skip", ((Boolean)value).booleanValue() && !A.is("gps"));
			return true;
		}});
	}
}
