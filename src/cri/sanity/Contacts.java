package cri.sanity;

import java.util.Map;
import java.util.TreeMap;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;


public final class Contacts
{
	private static final String[] groupRowId = new String[]{ GroupMembership.GROUP_ROW_ID };

	public  static final int GRP_ID    = 0;
	public  static final int GRP_TITLE = 1;
	public  static final int GRP_SID   = 2;
	public  static final int GRP_ACC   = 3;

	public static final Map<String,String> groups()
	{
		Map<String,String> map = new TreeMap<String,String>();
		Cursor c = A.resolver().query(Groups.CONTENT_URI, new String[]{ Groups._ID, Groups.TITLE }, null, null, null);
		if(c.moveToFirst()) {
			final int colId    = c.getColumnIndex(Groups._ID);
			final int colTitle = c.getColumnIndex(Groups.TITLE);
			do {
				String title = c.getString(colTitle);
				final int p = title.indexOf(':');
				if(p > 0) title = title.substring(p+1).trim();
				map.put(c.getString(colId), title);
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
			final int colId      = c.getColumnIndex(Groups._ID);
			final int colTitle   = c.getColumnIndex(Groups.TITLE);
			final int colSid     = c.getColumnIndex(Groups.SYSTEM_ID);
			final int colAcc = c.getColumnIndex(Groups.ACCOUNT_NAME);
			int i = 0;
			do {
				String title = c.getString(colTitle);
				String acc   = c.getString(colAcc);
				final int p  = title.indexOf(':');
				if(p > 0) title = title.substring(p+1).trim();
				if(acc.indexOf('@') < 0) acc = "";
				res[i++] = new String[]{ c.getString(colId), title, c.getString(colSid), acc, };
			} while(c.moveToNext());
		}
		c.close();
		return res;
	}

	public static final String[] groups(String idContact)
	{
		Cursor c = A.resolver().query(Data.CONTENT_URI, groupRowId,
			"contact_id="+idContact+" AND mimetype='vnd.android.cursor.item/group_membership'", null, null
		);
		String[] groups = new String[c.getCount()];
		if(c.moveToFirst()) {
			int col = c.getColumnIndex(GroupMembership.GROUP_ROW_ID);
			int i   = -1;
			do {
				groups[++i] = c.getString(col);
			} while(c.moveToNext());
		}
		c.close();
		return groups;
	}

}
