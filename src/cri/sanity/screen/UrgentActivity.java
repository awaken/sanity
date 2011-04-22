package cri.sanity.screen;

import cri.sanity.*;
import cri.sanity.pref.*;
import android.os.Bundle;


public class UrgentActivity extends ScreenActivity
{
	private static final String KEY_URGENT = "urgent";
	private static final String KEY_FILTER = "filter_urgent";

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    on(KEY_URGENT, new Change(){ public boolean on(){
    	A.putc(K.URGENT_FILTER, (Boolean)value);
    	((PFilter)pref(KEY_FILTER)).updateSum();
    	return true;
    }});
    fullOnly(K.URGENT_SKIP, K.URGENT_MODE+K.WS);
  }

	@Override
	public void onResume()
	{
		super.onResume();
    setChecked(KEY_URGENT, A.is(K.URGENT_FILTER));
	}

}
