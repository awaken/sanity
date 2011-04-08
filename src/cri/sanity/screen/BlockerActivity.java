package cri.sanity.screen;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import cri.sanity.*;
import cri.sanity.pref.*;
import cri.sanity.util.*;


public class BlockerActivity extends ScreenActivity
{
	private static final String KEY_BLOCK       = "block";
	private static final String KEY_HELP        = "block_help";
	private static final String KEY_HISTORY     = "block_history";
	private static final String KEY_FILTER      = "filter_block";
	private static final String KEY_SMS_SHARED  = "blocksms_shared";
	private static final String KEY_SMS_HISTORY = "blocksms_history";
	private static final String KEY_SMS_FILTER  = "filter_blocksms";

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    on(KEY_BLOCK, new Change(){ public boolean on(){
    	prefFilter().updateSum((Boolean)value);
    	return true;
    }});
    on(K.BLOCK_MODE+K.WS, new Change(){ public boolean on(){
    	final int mode = Integer.parseInt((String)value);
    	pref(K.BLOCK_RESUME+K.WS).setEnabled(mode == Blocker.MODE_RADIO);
    	pref(K.BLOCK_PICKUP     ).setEnabled(mode == Blocker.MODE_RADIO || mode == Blocker.MODE_HANGUP);
    	pref(K.BLOCK_SKIP       ).setEnabled(mode != Blocker.MODE_SILENT);
    	return true;
    }});
    final int mode = A.geti(K.BLOCK_MODE);
  	pref(K.BLOCK_RESUME+K.WS).setEnabled(mode == Blocker.MODE_RADIO );
  	pref(K.BLOCK_PICKUP     ).setEnabled(mode == Blocker.MODE_RADIO || mode == Blocker.MODE_HANGUP);
    pref(K.BLOCK_SKIP       ).setEnabled(mode != Blocker.MODE_SILENT);
    final boolean isSmsFilter = A.is(K.BLOCK_SMS_FILTER);
    prefSmsFilter().setEnabled(isSmsFilter);
    ((CheckBoxPreference)pref(KEY_SMS_SHARED)).setChecked(!isSmsFilter);
    on(KEY_HISTORY, new Click(){ public boolean on(){
    	startActivity(new Intent(A.app(), CallHistoryActivity.class));
    	return true;
    }});
    on(KEY_HELP, new Click(){ public boolean on(){
    	Alert.msg(A.s(R.string.block_help_title), A.rawstr(R.raw.block_methods), null, null, Alert.NONE);
    	return true;
    }});
    on(KEY_SMS_SHARED, new Change(){ public boolean on(){
    	final boolean on = (Boolean)value;
    	final PFilter p  = prefSmsFilter();
    	p.updateSum(!on);
    	p.setEnabled(!on);
    	return true;
    }});
    on(KEY_SMS_HISTORY, new Click(){ public boolean on(){
    	startActivity(new Intent(A.app(), SmsHistoryActivity.class));
    	return true;
    }});
    fullOnly(K.BLOCK_MODE+K.WS, K.BLOCK_RESUME+K.WS, K.BLOCK_PICKUP, K.BLOCK_SKIP, K.BLOCK_NOTIFY,
    		     K.BLOCK_SMS_MAX+K.WS, KEY_SMS_FILTER, KEY_SMS_SHARED);
  }

	@Override
	public void onResume()
	{
		super.onResume();
		setChecked(KEY_BLOCK     ,  A.is(K.BLOCK_FILTER));
		setChecked(KEY_SMS_SHARED, !A.is(K.BLOCK_SMS_FILTER));
	}

	private PFilter prefFilter   () { return (PFilter)pref(KEY_FILTER    ); }
	private PFilter prefSmsFilter() { return (PFilter)pref(KEY_SMS_FILTER); }

}
