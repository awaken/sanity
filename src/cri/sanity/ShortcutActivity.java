package cri.sanity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import cri.sanity.screen.*;


public final class ShortcutActivity extends Activity implements DialogInterface.OnCancelListener, FilenameFilter
{
	public  static final String EXTRA_KEY    = "cri.sanity.shortcut";
	private static final String EXTRA_KEY2   = "cri.sanity.shortcut2";
	private static final String EXTRA_PRF    = "profile";
	private static final String EXTRA_REC    = "rec_srv";
	private static final String EXTRA_FILTER = "filter";
	private static final String EXTRA_SCREEN = "screen";

	private class Dlg extends AlertDialog.Builder {
		private Dlg() { super(ShortcutActivity.this); }
	}
	private class Click implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dlg, int which) { on(which); }
		void on(int which) {}
	}
	private static class Entry {
		int icon;
		String extra, extra2;
		Entry(int ic, String e, String e2) { icon = ic; extra = e; extra2 = e2; }
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		A.activity = this;
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
		A.activity = this;
		super.onResume();
	}

	private void chooseShortcut() {
		final Map<Integer,Entry> map = new HashMap<Integer,Entry>();
		map.put(R.string.profile_shortcut     , new Entry(R.drawable.ic_prf        , EXTRA_PRF   , null));
		map.put(R.string.profile_cat          , new Entry(R.drawable.menu_profiles , EXTRA_SCREEN, ProfileActivity.class.getName()));
		map.put(R.string.general_cat          , new Entry(R.drawable.menu_general  , EXTRA_SCREEN, GeneralActivity.class.getName()));
		map.put(R.string.devices_cat          , new Entry(R.drawable.menu_devices  , EXTRA_SCREEN, DevicesActivity.class.getName()));
		map.put(R.string.proximity_cat        , new Entry(R.drawable.menu_proximity, EXTRA_SCREEN, ProximityActivity.class.getName()));
		map.put(R.string.speaker_cat          , new Entry(R.drawable.menu_speaker  , EXTRA_SCREEN, SpeakerActivity.class.getName()));
		map.put(R.string.vol_cat              , new Entry(R.drawable.menu_vol      , EXTRA_SCREEN, VolumeActivity.class.getName()));
		map.put(R.string.notify_cat           , new Entry(R.drawable.menu_notify   , EXTRA_SCREEN, NotifyActivity.class.getName()));
		map.put(R.string.tts_cat              , new Entry(R.drawable.menu_tts      , EXTRA_SCREEN, TtsActivity.class.getName()));
		map.put(R.string.tts_cat              , new Entry(R.drawable.menu_tts      , EXTRA_SCREEN, BlockerActivity.class.getName()));
		map.put(R.string.rec_cat              , new Entry(R.drawable.menu_rec      , EXTRA_SCREEN, RecordActivity.class.getName()));
		map.put(R.string.rec_shortcut         , new Entry(R.drawable.ic_rec_now    , EXTRA_REC   , null));
		map.put(R.string.rec_browse_title     , new Entry(R.drawable.menu_browse   , EXTRA_SCREEN, BrowseActivity.class.getName()));
		map.put(R.string.filter_shortcut_rec  , new Entry(R.drawable.menu_rec      , EXTRA_FILTER, "rec"));
		map.put(R.string.filter_shortcut_tts  , new Entry(R.drawable.menu_tts      , EXTRA_FILTER, "tts"));
		map.put(R.string.filter_shortcut_block, new Entry(R.drawable.menu_block    , EXTRA_FILTER, "block"));
		final int[] items = new int[]{ R.string.profile_shortcut, R.string.profile_cat, R.string.general_cat, R.string.devices_cat,
			R.string.proximity_cat, R.string.speaker_cat, R.string.vol_cat, R.string.notify_cat, R.string.block_cat, R.string.tts_cat,
			R.string.rec_cat, R.string.rec_browse_title, R.string.rec_shortcut,
			R.string.filter_shortcut_rec, R.string.filter_shortcut_tts, R.string.filter_shortcut_block };
		dlg(A.s(R.string.app_shortcut), items, new Click(){ void on(int which){
    	if(!A.isFull()) { askDonate(); return; }
  		final int title = items[which];
			final Entry e = map.get(title);
			createShortcut("[S] "+A.s(title), e.icon, e.extra, e.extra2);
			finish();
		}}).show();
	}

	private void createShortcut(String name, int icon, String extra, String extra2) {
		Intent si = new Intent(Intent.ACTION_MAIN);
    si.setClassName(this, getClass().getName());
    if(extra  != null) si.putExtra(EXTRA_KEY , extra );
    if(extra2 != null) si.putExtra(EXTRA_KEY2, extra2);
    Intent i = new Intent();
    i.putExtra(Intent.EXTRA_SHORTCUT_INTENT       , si);
    i.putExtra(Intent.EXTRA_SHORTCUT_NAME         , name);
    i.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, icon));
    setResult(RESULT_OK, i);
    finish();
	}

	private boolean execShortcut(Intent i) {
		final String e = i.getStringExtra(EXTRA_KEY);
		if(EXTRA_PRF   .equals(e)) return execProfiles(i.getStringExtra(EXTRA_KEY2));
		if(EXTRA_REC   .equals(e)) return execRecSrv();
		if(EXTRA_SCREEN.equals(e)) return execScreen(i.getStringExtra(EXTRA_KEY2));
		if(EXTRA_FILTER.equals(e)) return execFilter(i.getStringExtra(EXTRA_KEY2));
		A.toast(R.string.err);
		return false;
	}

	private boolean execProfiles(String prf) {
		if(prf != null) return restoreProfile(prf);
  	Dlg dlg = profileChooser();
  	if(dlg == null) { A.toast(A.name()+": "+A.s(R.string.msg_prf_empty)); return false; }
  	dlg.show();
  	return true;
	}

	private boolean execRecSrv() {
		if(!RecService.isRunning()) { A.toast(R.string.msg_rec_no); return false; }
		if(RecService.isRecord()) { RecService.recStop (0); A.toast(R.string.msg_rec_limit); }
		else                      { RecService.recStart(0); A.toast(R.string.msg_rec_go   ); }
		if(A.is(K.REC_CALLSCREEN))
			try { Dev.iTel().showCallScreen(); } catch(Exception e) {}
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

	private Dlg profileChooser() {
		final String[] profiles = profiles();
		if(profiles == null) return null;
		return dlg(A.s(R.string.profile_shortcut), profiles, new Click(){
			void on(int which) {
				restoreProfile(profiles[which]);
				finish();
			}
		});
	}
	
	private boolean restoreProfile(String name) {
		boolean res = P.restore(A.sdcardDir()+'/'+name+Conf.PRF_EXT);
		A.toast(String.format(A.s(res? R.string.msg_prf_restore_ok : R.string.msg_prf_restore_err), name));
		return res;
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
	
	private Dlg dlg(String title, String[] items, Click click) {
		Dlg dlg = new Dlg();
		dlg.setIcon(R.drawable.ic_bar).setTitle(title).setItems(items, click).setCancelable(true).setOnCancelListener(this);
		return dlg;
	}

	private Dlg dlg(String title, int[] items, Click click) {
		final int n = items.length;
		String[] labels = new String[n];
		for(int i=0; i<n; i++)
			labels[i] = A.s(items[i]);
		return dlg(title, labels, click);
	}
	
	private void askDonate() {
		A.alert(
			A.rawstr(R.raw.shortcut_free),
			new A.Click(){ public void on(){ A.gotoMarketDetails(Conf.DONATE_PKG); finish(); }},
			new A.Click(){ public void on(){ finish(); }}
		);
	}

	@Override
	public void onCancel(DialogInterface dlg) { finish(); }

	@Override
	public boolean accept(File dir, String fn) { return fn.endsWith(Conf.PRF_EXT); }

}
