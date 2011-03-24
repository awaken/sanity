package cri.sanity.screen;

import cri.sanity.*;
import android.os.Bundle;


public class NotifyActivity extends ScreenActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setEnabled(K.NOTIFY_REC_STOP, A.is(K.REC));
		setEnabled(K.BLOCK_NOTIFY   , A.is(K.BLOCK_FILTER));
		fullOnly(K.BLOCK_NOTIFY);
	}
}
