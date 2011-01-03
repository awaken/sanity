package cri.sanity;

import android.os.Bundle;
import android.preference.Preference;


public class MainActivity extends PrefActivity
{
	private static boolean isDonateAsking = false;

	//---- Activity override

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.prefs);
    //setContentView(R.layout.main);
    try {
    	Click cl = new Click(){ boolean on() { A.gotoAuthorApps(); return true; }};
      on("app_logo" , cl);
      on("app_logo2", cl);
      on("app_logo3", cl);
      on("app_logo4", cl);
      on("app_logo5", cl);
      on("app_logo6", cl);
      donateSetup();
      agreeSetup();
    }
    catch(Exception e) {
    	String msg = "Activity exception: "+e.getMessage();
    	A.loge(msg);
    	A.loge(e);
    	if(A.DEBUG) A.notify(msg);
    }
  }

	@Override
	public void onResume()
	{
		super.onResume();
		A.notifyCanc();
	}
	
	@Override
	public void onStop()
	{
  	notifyRun();
  	super.onStop();
	}
	
	@Override
	public void onDestroy()
	{
		A.notifyCanc();
		super.onDestroy();
	}
	
	//---- private api

	private void agreeSetup()
	{
		if(A.is("agree"))
  		donateCheck();
		else
			A.alert(A.tr(R.string.msg_eula_title),
				A.tr(R.string.app_fullname)+"\n\n"+A.tr(R.string.app_desc)+"\n\n"+A.tr(R.string.msg_eula),
				new A.DlgClick(){ void on(){ A.putc("agree",true); setChecked("enabled",true); donateCheck(); }},
				new A.DlgClick(){ void on(){ onBackPressed(); }},
				A.ALERT_OKCANC, false
			);
	}

	private void donateSetup()
	{
		Preference p = findPref("donate");
    if(!A.isFull()) {
      on(p, new Click(){ boolean on() {
      	A.gotoAuthorApps();
  	    return true;
      }});
    }
    else {
    	p.setTitle(R.string.donated_title);
    	p.setSummary(R.string.donated_sum);
    	p.setEnabled(false);
    }
	}
	
	private void donateCheck()
	{
		if(isDonateAsking || A.isFull()) return;
		isDonateAsking = true;
		A.alert(A.tr(R.string.msg_donate),
			new A.DlgClick(){ void on(){ A.gotoAuthorApps(); isDonateAsking = false; }},
			new A.DlgClick(){ void on(){                     isDonateAsking = false; }}
		);
	}

	//---- static api

	public static void notifyRun()
	{
		if(!A.is("notify_activity")) return;
		if(A.activity!=null && ((PrefActivity)A.activity).isQuitting()) return;
		A.notify(A.tr(A.isEnabled() ? R.string.msg_run_enabled : R.string.msg_run_disabled));
	}

}
