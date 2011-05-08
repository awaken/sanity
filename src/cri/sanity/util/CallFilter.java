package cri.sanity.util;

import java.util.Calendar;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import cri.sanity.*;


public final class CallFilter
{
	private static CallFilter instance;
	private static String[]   proj;
	private Cursor   cursor;
	private String   lastNum, lastName;
	private String[] lastGroups;

	//---- public api
	
	public final static CallFilter instance() {
		if(instance == null) instance = new CallFilter();
		return instance;
	}
	public final static void shutdown() {
		if(instance != null)
			instance.close();
	}

	public final boolean includes(String num, String sect, boolean resultIfDisabled) {
		if(!A.is("filter_enable_"+sect) || skipDateTime(sect)) return resultIfDisabled;
		// check if this filter has all numbers
		if(A.is("filter_all_"+sect)) return res(true, sect);
		// check for anonym number
		if(num==null || num.length()<=0)
			return res(A.is("filter_anonym_"+sect), sect);
		// search contacts by number (skip duplicate search of the last number)
		if(!query(num, num.equals(lastNum)))		// no contact found: check inside explicit number list and check prefix
			return res(A.is("filter_unknown_"+sect) || A.is("filter_num_"+num+sect) || skipPrefix(num,sect), sect);
		// contact found: check for all contacts or if contact is starred
		if(A.is("filter_allcontacts_"+sect) || (A.is("filter_star_"+sect) && isStarred()))
			return res(true, sect);
		// check found contact
		final String con = cursor.getString(cursor.getColumnIndex(PhoneLookup._ID));
		if(A.is("filter_contact_"+con+sect))
			return res(true, sect);
		// check found contact groups if any
		if(A.geti("filter_groups_count_"+sect) > 0) {
			//synchronized(this) {
			if(lastGroups == null) lastGroups = Contacts.groups(con);
			//}
			for(String group : lastGroups)
				if(A.is("filter_group_"+group+sect)) return res(true, sect);
		}
		// check number prefix
		return res(skipPrefix(num,sect), sect);
	}

	// get display name of given phone number
	public final synchronized String searchName(String num) {
		if(num==null || num.length()<=0) return null;
		final boolean cached = num.equals(lastNum);
		if(cached && lastName!=null) return lastName;
		if(!query(num, cached)) return lastName = "";
		return lastName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
	}

	public final String lastNum () { return lastNum;  }
	public final String lastName() { return lastName; }

	public final synchronized void close() {
		if(cursor == null) return;
		cursor.close();
		cursor     = null;
		lastNum    = null;
		lastName   = null;
		lastGroups = null;
	}

	//---- private api

	private static boolean res(boolean found, String sect) {
		return A.geti("filter_mode_"+sect)==0 ? found : !found;
	}

	private static boolean skipDateTime(String sect) {
		if(!A.is("filter_dt_"+sect)) return false;
		sect = '_' + sect;
		final Calendar cal = Calendar.getInstance();
		// date skip
		//if(!A.is("filter_dt_day"+cal.get(Calendar.DAY_OF_WEEK)+sect)) return true;
		final String days = A.gets("filter_dt_days"+sect);
		if(days.length()>0 && days.indexOf(Integer.toString(cal.get(Calendar.DAY_OF_WEEK)))<0) return true;
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
	
	private static boolean skipPrefix(String num, String sect) {
		final String prefix = A.gets("filter_prefix_"+sect);
		final int    len    = prefix.length();
		if(len <= 0) return false;
		String pre;
		int end, beg = 0;
		do {
			end = prefix.indexOf(Conf.FILTER_SEP, beg);
			if(end < 0) pre = prefix.substring(beg);
			else if(beg == end) break;
			else pre = prefix.substring(beg, end);
			if(num.startsWith(pre)) return true;
			beg = end + 1;
		} while(beg>0 && beg<len);
		return false;
	}

	private synchronized boolean query(String num, boolean cached) {
		if(!cached) {
			if(cursor != null) cursor.close();
			lastGroups = null;
			lastNum    = num;
			//if(num.charAt(0) == '+') num = "%2B"+num.substring(1);	// like Uri.encode(num) but faster for phone numbers
			num = Uri.encode(num);
			if(proj == null) proj = new String[]{ PhoneLookup._ID, PhoneLookup.DISPLAY_NAME, PhoneLookup.STARRED };
			cursor = A.resolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, num), proj, null, null, null);
		}
		return cursor!=null && cursor.moveToFirst();
	}

	private boolean isStarred() {
		return cursor.getString(cursor.getColumnIndex(PhoneLookup.STARRED)).equals("1");
	}

}
