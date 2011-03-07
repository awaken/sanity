package cri.sanity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;


public final class WifiTracker extends BroadcastReceiver
{
	private static final int ACT_NONE    = 0;
	private static final int ACT_ENABLE  = 1;
	private static final int ACT_DISABLE = 2;
	private static final int ENABLED     = WifiManager.WIFI_STATE_ENABLED;
	private static final int ENABLING    = WifiManager.WIFI_STATE_ENABLING;
	private static final int DISABLING   = WifiManager.WIFI_STATE_DISABLING;
	private static final int DISABLED    = WifiManager.WIFI_STATE_DISABLED;
	private static final int TIME_RETRY  = Conf.DEVS_MIN_RETRY;
	private static final int TASK_ACTION = Task.idNew();

	private int state;
	private int action = ACT_NONE;
	private boolean waiter = false;

	private final Task taskAction = new Task() {
		@Override
		public void run() {
			synchronized(WifiTracker.this) {
				if(action == ACT_NONE)
					return;
				A.wifiMan().setWifiEnabled(action == ACT_ENABLE);
			}
		}
	};

	public WifiTracker()
	{
		state = ENABLED;
		// state = A.wifiMan().getWifiState();
		A.app().registerReceiver(this, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
	}

	public synchronized void shutdown()
	{
		try {
			if(action != ACT_NONE) {
				waiter = true;
				wait(TIME_RETRY * 3);
			}
		} catch(Exception e) { }
		try { A.app().unregisterReceiver(this); } catch(Exception e) {}
	}

	public boolean   isOn() { return state == ENABLED || state == ENABLING; }
	public boolean willOn() { return action == ACT_ENABLE || (action == ACT_NONE && (state == ENABLED || state == ENABLING)); }

	@Override
	public synchronized void onReceive(Context ctx, Intent i)
	{
		state = i.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
		if(action == ACT_NONE) return;
		enable(action == ACT_ENABLE);
		if(waiter) notifyAll();
	}

	public synchronized void enable(boolean enable)
	{
		switch(state) {
			case DISABLING:
			case ENABLING:
				action = enable ? ACT_ENABLE : ACT_DISABLE;
				break;
			case DISABLED:
				if(!enable) action = ACT_NONE;
				else { action = ACT_ENABLE; taskAction.exec(TASK_ACTION, 0); }
				break;
			case ENABLED:
				if(enable) action = ACT_NONE;
				else { action = ACT_DISABLE; taskAction.exec(TASK_ACTION, 0); }
				break;
			default:
				if(!enable) action = ACT_NONE;
				else { action = ACT_ENABLE; taskAction.exec(TASK_ACTION, TIME_RETRY); }
		}
		// A.logd("after wifi enable ("+enable+") : action="+action+", state="+state);
	}

}
