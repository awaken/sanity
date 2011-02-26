package cri.sanity.screen;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import cri.sanity.*;

public class CallFilterActivity extends ScreenActivity
{
	public static final String EXTRA_ID    = "cri.sanity.CallFilter.id";
	public static final String EXTRA_TITLE = "cri.sanity.CallFilter.title";

	private String id;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		skipAllKeys = true;
		screener(CallFilterActivity.class, R.xml.prefs_callfilter, R.layout.img_call);
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		String t = i.getStringExtra(EXTRA_TITLE);
		if(!A.empty(t)) {
			final Preference p = pref("callfilter");
			p.setTitle(p.getTitle() + " - " + t);
		}
		id = i.getStringExtra(EXTRA_ID);
	}

}
