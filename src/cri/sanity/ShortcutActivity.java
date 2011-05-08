package cri.sanity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Pair;
import cri.sanity.screen.*;
import cri.sanity.util.*;


public final class ShortcutActivity extends Activity implements DialogInterface.OnDismissListener, FilenameFilter
{
	public  static final String EXTRA_KEY           = "cri.sanity.shortcut";
	private static final String EXTRA_KEY2          = "cri.sanity.shortcut2";
	private static final String EXTRA_KEY3          = "cri.sanity.shortcut3";
	private static final String EXTRA_OPTION        = "option";
	private static final String EXTRA_ALARMER       = "alarmer";
	private static final String EXTRA_PRF           = "profile";
	private static final String EXTRA_REC           = "rec_srv";
	private static final String EXTRA_FILTER        = "filter";
	private static final String EXTRA_SCREEN        = "screen";
	private static final String SHORTCUT_PREFIX     = "[S] ";
	private static final int    SHORTCUT_ROW        = 4;
	private static final int    SHORTCUT_FILTER_ROW = PrefGroups.SHORTCUT_FILTER_ROW;

	private boolean skipQuit = false;

	// SHORTCUT_ROW contains how many values are in one row!
	private static Object[] getShortcuts() {
		return new Object[] {
			R.string.option_shortcut       , 0                        , EXTRA_OPTION  , null,
			R.string.filter_cat            , 0                        , EXTRA_FILTER  , null,
			R.string.silent_shortcut       , R.drawable.ic_silent     , EXTRA_ALARMER , Alarmer.ACT_SILENTLIMIT,
			R.string.airplane_shortcut     , R.drawable.ic_airplane   , EXTRA_ALARMER , Alarmer.ACT_FLIGHTOFF,
			R.string.profile_shortcut      , R.drawable.ic_prf        , EXTRA_PRF     , null,
			R.string.profile_cat           , R.drawable.menu_profiles , EXTRA_SCREEN  , ProfileActivity.class.getName(),
			R.string.general_cat           , R.drawable.menu_general  , EXTRA_SCREEN  , GeneralActivity.class.getName(),
			R.string.devices_cat           , R.drawable.menu_devices  , EXTRA_SCREEN  , DevicesActivity.class.getName(),
			R.string.proximity_cat         , R.drawable.menu_proximity, EXTRA_SCREEN  , ProximityActivity.class.getName(),
			R.string.speaker_cat           , R.drawable.menu_speaker  , EXTRA_SCREEN  , SpeakerActivity.class.getName(),
			R.string.vol_cat               , R.drawable.menu_vol      , EXTRA_SCREEN  , VolumeActivity.class.getName(),
			R.string.notify_cat            , R.drawable.menu_notify   , EXTRA_SCREEN  , NotifyActivity.class.getName(),
			R.string.vibrate_cat           , R.drawable.menu_vibra    , EXTRA_SCREEN  , VibraActivity.class.getName(),
			R.string.block_cat             , R.drawable.menu_block    , EXTRA_SCREEN  , BlockerActivity.class.getName(),
			R.string.tts_cat               , R.drawable.menu_tts      , EXTRA_SCREEN  , TtsActivity.class.getName(),
			R.string.urgent_cat            , R.drawable.menu_urgent   , EXTRA_SCREEN  , UrgentActivity.class.getName(),
			R.string.answer_cat            , R.drawable.menu_answer   , EXTRA_SCREEN  , AnswerActivity.class.getName(),
			R.string.anonym_cat            , R.drawable.menu_anonym   , EXTRA_SCREEN  , AnonymActivity.class.getName(),
			R.string.rec_cat               , R.drawable.menu_rec      , EXTRA_SCREEN  , RecordActivity.class.getName(),
			R.string.rec_shortcut          , R.drawable.ic_rec_now    , EXTRA_REC     , null,
			R.string.rec_browse_title      , R.drawable.menu_browse   , EXTRA_SCREEN  , BrowseActivity.class.getName(),
			R.string.history_call          , R.drawable.menu_block    , EXTRA_SCREEN  , CallHistoryActivity.class.getName(),
			R.string.history_sms           , R.drawable.menu_block    , EXTRA_SCREEN  , SmsHistoryActivity.class.getName(),
			R.string.about_cat             , R.drawable.menu_about    , EXTRA_SCREEN  , AboutActivity.class.getName(),
		};
	}
	
	// SHORTCUT_FILTER_ROW contains how many values are in one row!
	private static Object[] getFilterShortcuts() { return PrefGroups.filterShortcuts(); }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Alert.activity = this;
		super.onCreate(savedInstanceState);
    final Intent i = getIntent();
    if(Intent.ACTION_CREATE_SHORTCUT.equals(i.getAction()))
    	chooseShortcut();
    else if(!A.isFull())
    	askDonate();
    else if(!execShortcut(i))
    	finish();
	}

	@Override
	public void onResume() {
		Alert.activity = this;
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		finish();
	}

	private void chooseShortcut() {
		final Object[] shortcuts = getShortcuts();
		final int r = SHORTCUT_ROW;
		final int n = shortcuts.length / r;
		final int[] items = new int[n];
		for(int i=0; i<n; i++)
			items[i] = (Integer)shortcuts[i * r];
		Alert.choose(A.s(R.string.app_shortcut), items, new Alert.Click(){ public void on(){
    	if(!A.isFull()) { askDonate(); return; }
  		final int k = which * r;
			createShortcut(A.s(items[which]), (Integer)shortcuts[k+1], (String)shortcuts[k+2], (String)shortcuts[k+3], null);
		}}).setOnDismissListener(this);
	}

	private void createShortcut(String name, int icon, String extra, String extra2, String extra3) {
		if(icon == 0) {
			skipQuit = true;
			if(     EXTRA_OPTION.equals(extra)) createOptionShortcut();
			else if(EXTRA_FILTER.equals(extra)) createFilterShortcut();
			else skipQuit = false;
			return;
		}
		Intent si = new Intent(Intent.ACTION_MAIN);
    si.setClassName(this, getClass().getName());
    if(extra  != null) si.putExtra(EXTRA_KEY , extra );
    if(extra2 != null) si.putExtra(EXTRA_KEY2, extra2);
    if(extra3 != null) si.putExtra(EXTRA_KEY3, extra3);
    Intent i = new Intent();
    i.putExtra(Intent.EXTRA_SHORTCUT_INTENT       , si);
    i.putExtra(Intent.EXTRA_SHORTCUT_NAME         , SHORTCUT_PREFIX + name);
    i.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, icon));
    setResult(RESULT_OK, i);
    finish();
	}
	
	private void createFilterShortcut() {
		final Object[] filters = getFilterShortcuts();
		final int r = SHORTCUT_FILTER_ROW;
		final int n = filters.length / r;
		final int[] items = new int[n];
		for(int i=0; i<n; i++)
			items[i] = (Integer)filters[i * r];
		final String cat = A.s(R.string.filter_cat);
		Alert.choose(cat, items, new Alert.Click(){ public void on(){
			final int k = which * r;
			createShortcut(cat, (Integer)filters[k+1], EXTRA_FILTER, (String)filters[k+2], null);
		}}).setOnDismissListener(this);
	}
	
	private void createOptionShortcut() {
		final String[][] sects   = PrefGroups.sections();
		final Object[] shortcuts = getShortcuts();
		final int ns = shortcuts.length;
		int n = 0;
		for(String[] s : sects) n += s.length - 1;
		final Vector<String > items = new Vector<String >(n);
		final Vector<String > keys  = new Vector<String >(n);
		final Vector<Integer> icons = new Vector<Integer>(n);
		for(String[] s : sects) {
			int h = 0, ic = 0;
			try {
				h = A.rstring(s[0]);
				for(int i=0; i<ns; i+=SHORTCUT_ROW)		// look for shortcut icon
					if(h == (Integer)shortcuts[i]) { ic = (Integer)shortcuts[i + 1]; break; }
			} catch(Exception e) {}
			if(ic == 0) continue;
			String pref = "[ " + A.s(h) + " ]\n";
			final int o = s.length;
			for(int t=0, i=1; i<o; i++, t=0) {
				final String k = s[i];
				try {
					t = A.rstring(k+"_title");
				} catch(Exception e) { try {
					if(k.startsWith("filter_enable_"))
						t = A.rstring(k.substring(k.lastIndexOf('_')+1) + "_enable_title");
				} catch(Exception e2) {}}
				if(t == 0) continue;
				keys .add(k);
				items.add(pref + A.s(t));
				icons.add(ic);
				if(pref.length() > 0) pref = "";
			}
		}
		Alert.choose(A.s(R.string.app_shortcut), (String[])items.toArray(new String[items.size()]), new Alert.Click(){ public void on(){
			String name = items.get(which);
			final int p = name.indexOf('\n');
			if(p > 0) name = name.substring(p + 1).trim();
			createShortcut(name, icons.get(which), EXTRA_OPTION, keys.get(which), name);
		}}).setOnDismissListener(this);
	}

	private boolean execShortcut(Intent i) {
		final String e  = i.getStringExtra(EXTRA_KEY);
		final String e2 = i.getStringExtra(EXTRA_KEY2);
		if(EXTRA_REC    .equals(e)) return execRecSrv();
		if(EXTRA_PRF    .equals(e)) return execProfiles(e2);
		if(EXTRA_ALARMER.equals(e)) return execAlarmer(e2);
		if(EXTRA_SCREEN .equals(e)) return execScreen(e2);
		if(EXTRA_FILTER .equals(e)) return execFilter(e2);
		if(EXTRA_OPTION .equals(e)) return execOption(e2, i.getStringExtra(EXTRA_KEY3));
		A.toast(R.string.err);
		return false;
	}
	
	private boolean execAlarmer(String action) {
		ModeActivity.start(action, true);
		return false;
	}

	private boolean execProfiles(String prf) {
		if(prf != null) return restoreProfile(prf);
  	if(profileChooser()) return true;
  	A.toast(A.name()+": "+A.s(R.string.msg_prf_empty));
  	return false;
  }

	private boolean execRecSrv() {
		if(!RecService.isRunning())
			A.toast(R.string.msg_rec_no);
		else {
			if(RecService.isRecord()) { RecService.recStop (0); A.toast(R.string.msg_rec_limit); }
			else                      { RecService.recStart(0); A.toast(R.string.msg_rec_go   ); }
			try { Dev.iTel().showCallScreen(); } catch(Exception e) {}
		}
		return false;
	}
	
	private boolean execScreen(String clsName) {
		try {
			Intent i = new Intent(A.app(), Class.forName(clsName));
			i.putExtra(EXTRA_KEY, 1);		// inform the destination screen that it was launched through shortcut
			startActivity(i);
		} catch(Exception e) {
			A.toast(R.string.err);
		}
		return false;
	}
	
	private boolean execFilter(String sect) {
		try {
			if("blocksms".equals(sect) && !A.is(K.BLOCK_SMS_FILTER)) sect = "block";
			String title = null;
			try { title = A.s(A.rstring(sect+"_cat")); } catch(Exception e) {}
			Intent i = new Intent(A.app(), FilterActivity.class);
			i.putExtra(EXTRA_KEY, 1);		// inform the destination screen that it was launched through shortcut
			i.putExtra(FilterActivity.EXTRA_SECT , sect );
			i.putExtra(FilterActivity.EXTRA_TITLE, title);
			FilterActivity.pref = null;
			startActivity(i);
		} catch(Exception e) {
			A.toast(R.string.err);
		}
		return false;
	}

	private boolean execOption(final String key, final String name) {
		skipQuit = false;
		if(K.PWD.equals(key)) return execChoosePwd();
		if(execChangeEdit(key, name)) return true;
		String[] items;
		Object[] vals;
		int selected = -1;
		final Map<String,Pair<Integer,Integer>> mapInt = PrefGroups.intLabVals();
		final Pair<Integer,Integer> pair = mapInt.get(key);
		if(pair != null) {
			Object arr[];
			items = A.resources().getStringArray(pair.first );
			arr   = A.resources().getStringArray(pair.second);
			vals  = new Integer[arr.length];
			Object vsel;
			try                { vsel = A.geti(key); }
			catch(Exception e) { vsel = A.gets(key); }
			final int n = arr.length;
			for(int i=0; i<n; i++) {
				Object v = arr[i];
				vals[i] = v = Integer.parseInt((String)v);
				if(vsel.equals(v)) selected = i;
			}
		} else {
			final Object v = P.getDefaults().get(key);
			if(v instanceof Boolean) {
				items = new String[]{ A.s(R.string.inactive), A.s(R.string.active) };
				vals  = new Boolean[]{ false, true };
				selected = A.is(key) ? 1 : 0;
			} else if(v instanceof Integer && key.indexOf("vol")>=0) {
				final int valsel = A.geti(key);
				CharSequence[][] arr = VolumeActivity.getVolumeLevels(AudioManager.STREAM_VOICE_CALL);
				CharSequence[] a0 = arr[0];
				CharSequence[] a1 = arr[1];
				final int n = a0.length;
				items = new String[n];
				vals  = new Integer[n];
				for(int i=0; i<n; i++) {
					final int vol = Integer.parseInt(a1[i].toString());
					items[i] = a0[i].toString();
					vals [i] = vol;
					if(valsel == vol) selected = i;
				}
			} else {
				A.toast(R.string.err);
				return false;
			}
		}
		if(selected >= 0) items[selected] = ">> "+items[selected]+" <<";
		final String[] items2 = items;
		final Object[] vals2  = vals;
		Alert.choose(name, items, new Alert.Click(){ public void on(){
			final Object v = vals2[which];
			if(A.has(K.PRF_NAME)) A.del(K.PRF_NAME);
			//if(mapInt.get(key) != null) A.put(key+K.WS, ((Integer)v).toString());
			A.putc(key, v);
			optionSet(name, items2[which]);
		}}).setOnDismissListener(this);
		return true;
	}

	private boolean execChangeEdit(final String key, final String name) {
		for(String edit : PrefGroups.edits()) {
			if(!edit.equals(key)) continue;
			Alert.edit(
				name,
				A.gets(key),
				new Alert.Edited(){ public void on(String val){
					if(A.has(K.PRF_NAME)) A.del(K.PRF_NAME);
					A.putc(key, val);
					optionSet(name, val);
					finish();
				}},
				new Alert.Edited(){ public void on(String val){
					finish();
				}}
			);
			return true;
		}
		return false;
	}

	private boolean execChoosePwd() {
		Alert.pwdChoose(A.gets(K.PWD), new Alert.Edited() {
			@Override
			public void on(String pwd){ A.putc(K.PWD, pwd); }
		}).setOnDismissListener(this);
		return true;
	}

	private void optionSet(String name, String value) {
		A.toast(String.format(A.s(R.string.msg_option_set), name, value));
	}

	private boolean profileChooser() {
		final String[] profiles = profiles();
		if(profiles == null) return false;
		Alert.choose(A.s(R.string.profile_shortcut), profiles, new Alert.Click(){
			@Override
			public void on() { restoreProfile(profiles[which]); }
		}).setOnDismissListener(this);
		return true;
	}
	
	private boolean restoreProfile(String name) {
		boolean res = P.restore(A.sdcardDir()+'/'+name+Conf.PRF_EXT);
		A.toast(String.format(A.s(res? R.string.msg_prf_restore_ok : R.string.msg_prf_restore_err), name));
		return false;
	}

	private String[] profiles() {
		final String dir = A.sdcardDir();
		if(dir == null) return null;
		String[] files = new File(dir).list(this);
		final int n = files.length;
		if(n <= 0) return null;
		final int l = Conf.PRF_EXT.length();
		String[] profiles = new String[n];
		for(int i=0; i<n; i++)
			profiles[i] = files[i].substring(0, files[i].length()-l);
		Arrays.sort(profiles, 0, profiles.length);
		return profiles;
	}

	private void askDonate() {
		skipQuit = true;
		Alert.msg(
			A.rawstr(R.raw.shortcut_free),
			new Alert.Click(){ public void on(){ Goto.marketDetails(License.FULL_PKG); }},
			new Alert.Click(){ public void on(){ skipQuit = false; finish(); }}
		).setOnDismissListener(this);
	}

	@Override
	public void onDismiss(DialogInterface dlg) { 
		if(skipQuit) skipQuit = false;
		else finish(); 
	}

	@Override
	public boolean accept(File dir, String fn) { return fn.endsWith(Conf.PRF_EXT); }

}
