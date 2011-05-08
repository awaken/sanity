package cri.sanity.screen;

import cri.sanity.*;
import cri.sanity.util.*;
import java.io.File;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;


public class RecordActivity extends ScreenActivity
{
	private static final String REC_START_HEADSET = K.REC_START_HEADSET + K.WS;
	private static final String REC_STOP_LIMIT    = K.REC_STOP_LIMIT    + K.WS;
	private static final String REC_START_TIMES   = K.REC_START_TIMES   + K.WS;
	
	private boolean speakerCall;
	
	//---- Activity override

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		updateEnabled();
		on("rec_browse", new Click(){ public boolean on(){
			startActivity(new Intent(A.app(), BrowseActivity.class));
			return true;
		}});
		on("rec_scan", new Change(){ public boolean on(){
			boolean res = (Boolean)value? scanEnable() : scanDisable();
			if(!res) Alert.msg(String.format(A.s(R.string.err_scan), scanFn()));
			return res;
		}});
		on(K.REC_START, new Change(){ public boolean on(){
			final boolean on = (Boolean)value;
			setEnabled(K.REC_START_SPEAKER, on && (A.is(K.SPEAKER_AUTO) || speakerCall));
			return true;
		}});
		on(K.REC_STOP, new Change(){ public boolean on(){
			final boolean on = (Boolean)value;
			setEnabled(K.REC_STOP_SPEAKER, on && (A.is(K.SPEAKER_AUTO) || speakerCall));
			setEnabled(REC_STOP_LIMIT, on &&  A.isFull());
			return true;
		}});
		on(K.REC_START_SPEAKER, new Change(){ public boolean on(){
			setEnabled(REC_START_TIMES, (Boolean)value || A.geti(K.REC_START_HEADSET)!=RecService.ACT_HEADSET_SKIP);
			return true;
		}});
		on(REC_START_HEADSET, new Change(){ public boolean on(){
			setEnabled(REC_START_TIMES, A.is(K.REC_START_SPEAKER) || !Integer.toString(RecService.ACT_HEADSET_SKIP).equals((String)value));
			return true;
		}});
		if(!A.isFull()) {
			final Preference p = pref(REC_STOP_LIMIT);
			p.setEnabled(false);
			p.setSummary(p.getSummary()+" "+A.s(R.string.full_only)+'.');
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		updateEnabled();
	}

	//---- private static api

	private void updateEnabled() {
		speakerCall = A.geti(K.SPEAKER_CALL) != 0;
		boolean speakerAuto = speakerCall || A.is(K.SPEAKER_AUTO);
		boolean recStop = A.is(K.REC_STOP);
		setEnabled(K.REC_START_SPEAKER, speakerAuto && A.is(K.REC_START));
		setEnabled(K.REC_STOP_SPEAKER , speakerAuto && recStop);
		setEnabled(  REC_STOP_LIMIT   , recStop && A.isFull());
		setEnabled(  REC_START_TIMES  , A.is(K.REC_START_SPEAKER) || A.geti(K.REC_START_HEADSET)!=RecService.ACT_HEADSET_SKIP);
		setChecked( "rec_scan"        , scanAllowed());
	}

	// manage multimedia scanner
	private static boolean scanAllowed() { return !scanFile().exists(); }
	private static File    scanFile   () { return new File(scanFn());   }
	private static String  scanFn     () { return A.sdcardDir() + "/.nomedia"; }
	private static boolean scanEnable () {
		final File f = scanFile();
		try { f.delete(); } catch(Exception e) {}
		return !f.exists();
	}
	private static boolean scanDisable() {
		final File f = scanFile();
		try { f.createNewFile(); } catch(Exception e) {}
		return f.exists();
	}

}
