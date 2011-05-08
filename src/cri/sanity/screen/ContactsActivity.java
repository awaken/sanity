package cri.sanity.screen;

import cri.sanity.*;
import cri.sanity.util.*;
import java.util.Map;
import java.util.HashMap;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.CheckBoxPreference;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Data;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class ContactsActivity extends ScreenActivity
{
	private static final String SEP = FilterActivity.SEP;
	private static final int CODE_SEARCH = 1;

	private PreferenceCategory prefGroup;
	private String sect;
	private boolean changed, grouped;
	private Map<String,Pref> prefs;

	private Handler handler = new Handler() {
		@Override @SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			if(msg == null) return;
			Pair<Pref,CharSequence> pair = (Pair<Pref,CharSequence>)msg.obj;
			pair.first.setSummary(pair.second);
		}
	};

	//---- overriding

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		skipAllKeys = true;
		screener(ContactsActivity.class, R.xml.filter_contacts, R.layout.img_contacts);
		super.onCreate(savedInstanceState);
		prefGroup = (PreferenceCategory)pref("filter_contacts");
		Intent i  = getIntent();
		sect      = i.getStringExtra(FilterActivity.EXTRA_SECT );
		String t  = i.getStringExtra(FilterActivity.EXTRA_TITLE);
		if(!A.empty(t)) pref("filter_header").setTitle(prefGroup.getTitle()+"  ("+t+')');
		on("filter_search", new Click(){ public boolean on(){ search(); return true; }});
		readContacts();
		changed = false;
		grouped = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.contacts, menu);
    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
			case R.id.search : search();  break;
			case R.id.selall : selall();  break;
			case R.id.selnone: selnone(); break;
			case R.id.canc   : canc();    break;
			default: return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		if(!grouped)
			new Async(){ public void run(){ readContactGroups(this); }}.execute();
	}

	@Override
	public void onBackPressed()
	{
		if(isFinishing()) return;
		Intent i = new Intent();
		i.putExtra(FilterActivity.EXTRA_SECT, saveList());
		setResult(RESULT_OK, i);
		super.onBackPressed();
	}
	
	@Override
	public void onActivityResult(int code, int res, Intent i)
	{
		if(i==null || code!=CODE_SEARCH) return;
		Cursor c = A.resolver().query(i.getData(), new String[]{ Contacts._ID, Contacts.DISPLAY_NAME }, null, null, null);
		if(c.moveToFirst()) {
			Pref p = prefs.get(c.getString(c.getColumnIndex(Contacts._ID)));
			int msgId = p!=null? p.isChecked()? R.string.msg_contact_unsel : R.string.msg_contact_sel : R.string.msg_contact_err;
			A.toast(String.format(A.s(msgId), c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME))));
			if(p == null) return;
			p.setChecked(!p.isChecked());
			changed = true;
		}
		c.close();
	}
	
	//---- private api

	private void readContacts()
	{
		Cursor c = A.resolver().query(
			Contacts.CONTENT_URI, new String[]{ Contacts._ID, Contacts.DISPLAY_NAME, Contacts.STARRED },
			Contacts.HAS_PHONE_NUMBER+"=1", null,
			Contacts.DISPLAY_NAME
		);
		final int n = c.getCount();
		if(n <= 0) return;
		final String starred = A.s(R.string.starred);
		final int colId   = c.getColumnIndex(Contacts._ID);
		final int colName = c.getColumnIndex(Contacts.DISPLAY_NAME);
		final int colStar = c.getColumnIndex(Contacts.STARRED);
		prefs = new HashMap<String,Pref>(n);
		Pref[] arr = new Pref[n];
		// read all contacts
		for(int i=0; c.moveToNext(); i++) {
			String con  = c.getString(colId);
			Pref   pref = new Pref(con, c.getString(colName));
			if(c.getString(colStar).equals("1")) pref.setSummary(starred);
			prefs.put(con, pref);
			arr[i] = pref;
		}
		c.close();
		// first, put all already selected contacts
		for(String con : A.gets(keyAll()).split(SEP)) {
			Pref p = prefs.get(con);
			if(p == null) continue;
			prefGroup.addPreference(p);
			p.setChecked(true);
		}
		// after, put all remaining contacts
		for(Pref p : arr)
			if(!p.isChecked())
				prefGroup.addPreference(p);
	}

	private void readContactGroups(Async async)
	{
		Cursor c = A.resolver().query(
			Data.CONTENT_URI,
			new String[]{ GroupMembership.GROUP_ROW_ID, GroupMembership.CONTACT_ID },
			Data.MIMETYPE+"='"+GroupMembership.CONTENT_ITEM_TYPE+'\'',
			null, GroupMembership.CONTACT_ID
		);
		if(async!=null && !async.isCancelled())
			readContactGroups(async, c);
		c.close();
	}

	private void readContactGroups(Async async, Cursor c)
	{
		if(!c.moveToFirst()) return;
		Map<String,String> groups = cri.sanity.util.Contacts.groups();
		if(async!=null && async.isCancelled()) return;
		final int colGrp = c.getColumnIndex(GroupMembership.GROUP_ROW_ID);
		final int colCon = c.getColumnIndex(GroupMembership.CONTACT_ID);
		Pref    lastPref = null;
		CharSequence sum = null;
		do {
			if(prefs == null) return;
			Pref   p = prefs .get(c.getString(colCon));
			String g = groups.get(c.getString(colGrp));
			if(p==null || g==null) continue;
			if(p == lastPref)
				sum = sum+", "+g;
			else {
				if(lastPref != null) lastPref.setSummaryAsync(sum);
				sum = p.getSummary();
				sum = sum==null || sum.length()<=0 ? g : sum+", "+g;
				lastPref = p;
			}
			if(async!=null && async.isCancelled()) return;
		} while(c.moveToNext());
		if(lastPref != null) lastPref.setSummaryAsync(sum);
		grouped = true;
	}

	private int saveList()
	{
		if(changed)
			for(String con : A.gets(keyAll()).split(SEP))
				A.del(keySect(con));
		String cons = "";
		int k = 0;
		int n = prefGroup.getPreferenceCount();
		if(n > 0) {
			final StringBuilder sb = new StringBuilder(256);
			boolean first = true;
			for(int i=0; i<n; i++) {
				Pref pref = (Pref)prefGroup.getPreference(i);
				if(!pref.isChecked()) continue;
				++k;
				if(!changed) continue;
				String con = pref.con;
				A.put(keySect(con), true);
				if(first) first = false;
				else      sb.append(SEP);
				sb.append(con);
			}
			if(n > k) cons = sb.toString();
		}
		if(changed) {
			if(n==0 || n>k) A.put(keyAll(), cons).putc(keyCount(), k);
			else            A.put(keyAll(), ""  ).putc(keyCount(), k = -1); 
		}
		return k;		// return number of selected contacts
	}

	private String keyAll()            { return "filter_contacts_"+sect; }
	private String keyCount()          { return "filter_contacts_count_"+sect; }
	private String keySect(String val) { return "filter_contact_"+val+sect; }

	private void search()
	{
		startActivityForResult(new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI), CODE_SEARCH);
	}

	private void selall()
	{
		final int n = prefGroup.getPreferenceCount();
		if(n < 1) return;
		for(int i=0; i<n; i++)
			((Pref)prefGroup.getPreference(i)).setChecked(true);
		A.toast(String.format(A.s(R.string.msg_selected_all), n+""));
		changed = true;
	}
	
	private void selnone()
	{
		final int n = prefGroup.getPreferenceCount();
		if(n < 1) return;
		for(int i=0; i<n; i++)
			((Pref)prefGroup.getPreference(i)).setChecked(false);
		changed = true;
	}
	
	private void canc()
	{
		if(!changed) { finish(); return; }
		Alert.msg(
			A.s(R.string.ask_canc_all),
			new Alert.Click(){ public void on(){ changed = false; dismiss(); finish(); }},
			null,
			Alert.OKCANC
		);
	}

	//---- inner class

	private abstract class Async extends A.Async {}

	private class Pref extends CheckBoxPreference implements OnPreferenceChangeListener
	{
		private String con;

		private Pref(String con, String title) {
			super(ContactsActivity.this);
			this.con = con;
			setPersistent(false);
			setTitle(title);
			setOnPreferenceChangeListener(this);
		}
		
		private void setSummaryAsync(CharSequence sum) {
			Message msg = new Message();
			msg.obj = new Pair<Pref,CharSequence>(this, sum==null? "" : sum);
			handler.sendMessage(msg);
		}

		@Override
		public boolean onPreferenceChange(Preference p, Object v) { return changed = true; }
	}

}
