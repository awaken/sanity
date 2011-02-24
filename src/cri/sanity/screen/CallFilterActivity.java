package cri.sanity.screen;

import android.content.Intent;
import android.os.Bundle;
import cri.sanity.*;

public class CallFilterActivity extends ScreenActivity
{
	public  static final String EXTRA_KEY_TITLE  = "cri.sanity.CallFilter";
	public  static final String EXTRA_KEY_PREFIX = "cri.sanity.CallFilter";
	private static final String GROUP            = "callfilter";
	
	private String prefix;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		skipAllKeys = true;
		screener(CallFilterActivity.class, R.xml.prefs_callfilter, R.layout.img_call);
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		prefix = i.getStringExtra(EXTRA_KEY_PREFIX);
		pref(GROUP).setTitle(i.getStringExtra(EXTRA_KEY_TITLE));
	}

}
