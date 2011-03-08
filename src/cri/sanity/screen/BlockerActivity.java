package cri.sanity.screen;

import android.os.Bundle;
import android.preference.Preference.OnPreferenceChangeListener;
import cri.sanity.*;


public class BlockerActivity extends ScreenActivity
{
	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    fullOnly(K.BLOCK_SKIP, K.BLOCK_MODE+K.WS);
    final OnPreferenceChangeListener changeBlock = pref(K.BLOCK_MODE+K.WS).getOnPreferenceChangeListener();
    on(K.BLOCK_MODE+K.WS, new Change(){ public boolean on(){
    	if(changeBlock!=null && !changeBlock.onPreferenceChange(pref, value)) return false;
    	if(A.SDK>8 && value.equals(Blocker.MODE_HANGUP+""))
    		A.alert(A.rawstr(R.raw.block_warn));
    	pref(K.BLOCK_SKIP).setEnabled(!value.equals(Blocker.MODE_SILENT+""));
    	return true;
    }});
    pref(K.BLOCK_SKIP).setEnabled(A.geti(K.BLOCK_MODE) != Blocker.MODE_SILENT);
  }

}
