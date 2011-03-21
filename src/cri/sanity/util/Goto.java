package cri.sanity.util;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import cri.sanity.*;


public final class Goto
{
	//public static final boolean marketSearchPkg(String pkg) { return marketUrl("search?q=pname:\""+pkg+'"'); }
	public static final boolean marketSearchPub(String pub) { return marketUrl("search?q=pub:\""+pub+'"'); }
	public static final boolean marketDetails(String pkg) { return marketUrl("details?id="+pkg); }

	public static final boolean marketUrl(String query) {
		final boolean res = url("market://"+query);
		if(!res) Alert.msg(A.s(R.string.err_market));
		return res;
	}

	public static final boolean url(String url) {
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			A.app().startActivity(i);
			return true;
		} catch(ActivityNotFoundException e) {
			return false;
		}
	}
	
}
