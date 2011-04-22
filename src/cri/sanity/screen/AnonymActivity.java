package cri.sanity.screen;

import cri.sanity.*;
import cri.sanity.pref.*;
import android.os.Bundle;


public class AnonymActivity extends ScreenActivity
{
	private static final String KEY_COUNTRY = "anonym_country";

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
		final PList countryList = (PList)pref(KEY_COUNTRY);
		final PEdit prefixEdit  = (PEdit)pref(K.ANONYM_PREFIX);
    try {
    	boolean       found = false;
	    final String[] vals = A.resources().getStringArray(R.array.anonym_country_values);
	    final String   pref = A.gets(K.ANONYM_PREFIX);
	    final int      n    = vals.length;
	    for(int i=0; i<n; i++) {
	    	if(vals[i].equals(pref)) {
	    		countryList.setValue(pref);
	    		found = true;
	    		break;
	    	}
	    }
	    if(!found) countryList.setValue("");
    } catch(Exception e) {
    	countryList.setValue("");
    }
    on(countryList, new Change(){ public boolean on(){
    	final String  v = (String)value;
    	final boolean custom = v.length() <= 0;
    	if(!custom) prefixEdit.setText(v);
    	prefixEdit.setEnabled(custom);
    	return true;
    }});
    fullOnly(K.ANONYM_CONFIRM, K.ANONYM_NOTIFY, K.ANONYM_PREFIX);
  }

}
