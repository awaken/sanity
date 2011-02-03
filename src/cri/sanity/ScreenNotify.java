package cri.sanity;

import android.os.Bundle;


public class ScreenNotify extends ActivityScreen
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setEnabled(K.NOTIFY_REC_STOP, A.is(K.REC));
	}
}
