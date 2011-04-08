package cri.sanity.util;

import java.util.Map;
import java.util.TreeMap;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import cri.sanity.A;


public final class Contacts
{
	public static final int GRP_ID    = 0;
	public static final int GRP_TITLE = 1;
	public static final int GRP_SID   = 2;
	public static final int GRP_ACC   = 3;

	//--- public api

	public static final Map<String,String> groups()
	{
		Map<String,String> map = new TreeMap<String,String>();
		Cursor c = A.resolver().query(Groups.CONTENT_URI, new String[]{ Groups._ID, Groups.TITLE }, null, null, null);
		if(c.moveToFirst()) {
			final int colId    = c.getColumnIndex(Groups._ID);
			final int colTitle = c.getColumnIndex(Groups.TITLE);
			do {
				final String id = c.getString(colId);
				if(id == null) continue;
				map.put(id, adjustTitle(c.getString(colTitle)));
			} while(c.moveToNext());
		}
		c.close();
		return map;
	}

	public static final String[][] fullGroups()
	{
		Cursor c = A.resolver().query(Groups.CONTENT_URI,
			new String[]{ Groups._ID, Groups.TITLE, Groups.SYSTEM_ID, Groups.ACCOUNT_NAME },
			null, null, null
		);
		String[][] res = new String[c.getCount()][];
		if(c.moveToFirst()) {
			final int colId    = c.getColumnIndex(Groups._ID);
			final int colTitle = c.getColumnIndex(Groups.TITLE);
			final int colSid   = c.getColumnIndex(Groups.SYSTEM_ID);
			final int colAcc   = c.getColumnIndex(Groups.ACCOUNT_NAME);
			int i = -1;
			do {
				final String id = c.getString(colId );
				if(id == null) continue;
				res[++i] = new String[]{ id, adjustTitle(c.getString(colTitle)), c.getString(colSid), adjustAcc(c.getString(colAcc)) };
			} while(c.moveToNext());
		}
		c.close();
		return res;
	}

	public static final String[] groups(String idContact)
	{
		Cursor c = A.resolver().query(Data.CONTENT_URI, new String[]{ GroupMembership.GROUP_ROW_ID },
			"contact_id="+idContact+" AND mimetype='vnd.android.cursor.item/group_membership'", null, null
		);
		String[] groups = new String[c.getCount()];
		if(c.moveToFirst()) {
			final int col = c.getColumnIndex(GroupMembership.GROUP_ROW_ID);
			int i = -1;
			do {
				groups[++i] = c.getString(col);
			} while(c.moveToNext());
		}
		c.close();
		return groups;
	}

	//--- private apiù

	private static String adjustTitle(String title)
	{
		if(title == null) return "";
		final int p = title.indexOf(':');
		return p>0? title.substring(p+1).trim() : title;
	}
	
	private static String adjustAcc(String acc) { return acc==null || acc.indexOf('@')<0 ? "" : acc; }

}
