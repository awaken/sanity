package cri.sanity.screen;

import cri.sanity.*;
import android.os.Bundle;


public class NotifyActivity extends ScreenActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final boolean block = A.is(K.BLOCK_FILTER);
		setEnabled(K.NOTIFY_REC_STOP , A.is(K.REC));
		setEnabled(K.BLOCK_NOTIFY    , block);
		setEnabled(K.BLOCK_SMS_NOTIFY, block && A.is(K.BLOCK_SMS));
		fullOnly(K.BLOCK_NOTIFY, K.BLOCK_SMS_NOTIFY);
	}
}
