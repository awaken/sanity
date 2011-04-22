package cri.sanity;

import cri.sanity.util.Alert;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.preference.CheckBoxPreference;
import android.preference.Preference;


public final class Admin
{
	private static ComponentName compName;

	//---- inner classes
	
	public static class Receiver extends DeviceAdminReceiver {
		@Override
	  public CharSequence onDisableRequested(Context ctx, Intent i) { return A.rawstr(R.raw.admin_ask_disable); }
	}

  //---- static methods

	public static final ComponentName compName() {
		if(compName == null) compName = new ComponentName(A.app(), Receiver.class);
		return compName;
	}
	
  public static final boolean isActive() { return A.SDK>=8 && A.devpolMan().isAdminActive(compName()); }

  public static final void request(Context ctx) {
  	if(A.SDK < 8) return;
    final Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
    i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN   , compName());
    i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, A.rawstr(R.raw.admin_explanation));
    ctx.startActivity(i);
  }

  public static final void remove() {
  	final ComponentName cn = compName();
  	synchronized(cn) {
	  	if(!isActive()) return;
	  	try { A.devpolMan().removeActiveAdmin(cn); } catch(Exception e) {}
  	}
  }
  
  public static final void prefSetup(final Preference p) {
		if(A.SDK < 8) {
			p.setEnabled(false);
			p.setSummary(R.string.msg_require_froyo);
		}
		else {
			final PrefActivity act = (PrefActivity)Alert.activity;
			act.on(p, new PrefActivity.Change(){ public boolean on(){
				if((Boolean)value)
					Admin.request(act);
				else
					Alert.msg(
						A.rawstr(R.raw.admin_ask_disable),
						new Alert.Click(){ public void on(){ remove(); prefCheck(p); }},
						null,
						Alert.OKCANC
					);
				return false;
			}});
		}
  }

	public static final void prefCheck(Preference p) {
		((CheckBoxPreference)p).setChecked(isActive());
	}

}
