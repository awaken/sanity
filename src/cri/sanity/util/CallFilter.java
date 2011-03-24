package cri.sanity.util;

import java.util.Calendar;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import cri.sanity.*;


public final class CallFilter
{
	private static Cursor   cursor;
	private static String   lastNum;
	private static String   lastName;
	private static String[] lastGroups;

	//---- public api

	public static final boolean includes(String num, String sect, boolean resultIfDisabled) {
		if(!A.is("filter_enable_"+sect) || skipDateTime(sect)) return resultIfDisabled;
		// check if this filter has all numbers
		if(A.is("filter_all_"+sect)) return res(true, sect);
		// check for anonym number
		if(num==null || num.length()<=0)
			return res(A.is("filter_anonym_"+sect), sect);
		// search contacts by number (skip duplicate search of the last number)
		if(!query(num, num.equals(lastNum)))
			return res(A.is("filter_unknown_"+sect) || A.is("filter_num_"+num+sect), sect);		// no contact found: check inside explicit number list
		// contact found: check for all contacts or if contact is starred
		if(A.is("filter_allcontacts_"+sect) || (A.is("filter_star_"+sect) && isStarred()))
			return res(true, sect);
		// check found contact
		final String con = cursor.getString(cursor.getColumnIndex(PhoneLookup._ID));
		if(A.is("filter_contact_"+con+sect))
			return res(true, sect);
		// check found contact groups if any
		if(A.geti("filter_groups_count_"+sect) > 0) {
			if(lastGroups == null) lastGroups = Contacts.groups(con);
			for(String group : lastGroups)
				if(A.is("filter_group_"+group+sect)) return true;
		}
		// not found in this filter
		return res(false, sect);
	}

	// get display name of given phone number
	public static String searchName(String num) {
		if(num==null || num.length()<=0) return null;
		final boolean cached = num.equals(lastNum);
		if(cached && lastName!=null) return lastName;
		if(!query(num, cached)) return lastName = "";
		return lastName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
	}

	public static final String lastNum () { return lastNum ; }
	public static final String lastName() { return lastName; }

	public static final void shutdown() {
		if(cursor == null) return;
		cursor.close();
		cursor     = null;
		lastNum    = null;
		lastName   = null;
		lastGroups = null;
	}

	//---- private api

	private static boolean skipDateTime(String sect) {
		if(!A.is("filter_dt_"+sect)) return false;
		sect = '_' + sect;
		// date skip
		final Calendar cal = Calendar.getInstance();
		if(!A.is("filter_dt_day"+cal.get(Calendar.DAY_OF_WEEK)+sect)) return true;
		// time skip
		final int cnt = A.geti("filter_dt_time_count"+sect);
		if(cnt <= 0) return false;
		final int h = cal.get(Calendar.HOUR_OF_DAY);
		final int m = cal.get(Calendar.MINUTE);
		for(int i=1; i<=cnt; i++) {
			final int t  = A.geti("filter_dt_time"+i+sect);
			final int h1 = (t >> 24) & 0xff;
			if(h < h1) continue;
			final int m1 = (t >> 16) & 0xff;
			if(h==h1 && m<m1) continue;
			final int h2 = (t >>  8) & 0xff;
			if(h > h2) continue;
			final int m2 = t & 0xff;
			if(h==h2 && m>m2) continue;
			return false;
		}
		return true;
	}

	private static boolean query(String num, boolean cached) {
		if(!cached) {
			if(cursor != null) cursor.close();
			lastGroups = null;
			lastNum    = num;
			if(num.charAt(0) == '+') num = "%2B"+num.substring(1);	// equals to Uri.encode(num) for phone numbers
			cursor = A.resolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, num),
					                        new String[]{ PhoneLookup._ID, PhoneLookup.DISPLAY_NAME, PhoneLookup.STARRED },
					                        null, null, null);
		}
		return cursor.moveToFirst();
	}

	private static boolean isStarred() {
		return cursor.getString(cursor.getColumnIndex(PhoneLookup.STARRED)).equals("1");
	}

	private static boolean res(boolean found, String sect) {
		return A.geti("filter_mode_"+sect)==0 ? found : !found;
	}

}
