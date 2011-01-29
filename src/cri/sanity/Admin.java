package cri.sanity;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public final class Admin
{
	private static ComponentName compName;

	//---- inner classes
	
	public static class Receiver extends DeviceAdminReceiver {
		@Override
	  public CharSequence onDisableRequested(Context ctx, Intent i) { return A.tr(R.string.admin_disable); }
	}

  //---- static methods

	public static final ComponentName compName() {
		if(compName == null) compName = new ComponentName(A.app(), Receiver.class);
		return compName;
	}
	
  public static final boolean isActive() { return A.SDK>=8 && A.devpolMan().isAdminActive(compName()); }

  public static final void request(Context ctx)
  {
  	if(A.SDK < 8) return;
    final Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
    i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName());
    i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, A.tr(R.string.admin_explanation));
    ctx.startActivity(i);
  }

  public static final void remove()
  {
  	if(!isActive()) return;
  	A.devpolMan().removeActiveAdmin(compName());
  }

}
