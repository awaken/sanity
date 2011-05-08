package cri.sanity.screen;

import android.os.Bundle;
import cri.sanity.*;


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
		setEnabled(K.ANONYM_NOTIFY   , A.is(K.ANONYM));
		fullOnly(K.BLOCK_NOTIFY, K.BLOCK_SMS_NOTIFY, K.ANONYM_NOTIFY);
	}
}
