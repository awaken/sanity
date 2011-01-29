package cri.sanity;

import java.io.File;

import android.os.Bundle;


public class ScreenRecord extends ActivityScreen
{
	//---- Activity override

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		on(K.REC_BROWSE, new Click(){ boolean on(){ A.alert(A.tr(R.string.msg_browse_beta)); return true; }});
		on(K.REC_SCAN,  new Change(){ boolean on(){
			boolean res = (Boolean)value? scanEnable() : scanDisable();
			if(!res) A.alert(A.tr(R.string.msg_rec_scan_err).replace("$FN", scanFn()));
			return res;
		}});
	}

	@Override
	public void onStart()
	{
		super.onStart();
		setChecked(K.REC_SCAN, scanAllowed());
	}

	//---- private static api

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
