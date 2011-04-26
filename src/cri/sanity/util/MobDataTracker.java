package cri.sanity.util;

import cri.sanity.*;
import com.android.internal.telephony.ITelephony;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;


public final class MobDataTracker extends PhoneStateListener
{
	private static final int ACT_NONE    = 0;
	private static final int ACT_ENABLE  = 1;
	private static final int ACT_DISABLE = 2;
	private static final int ENABLED     = TelephonyManager.DATA_CONNECTED;
	private static final int ENABLING    = TelephonyManager.DATA_CONNECTING;
	private static final int DISABLED    = TelephonyManager.DATA_DISCONNECTED;
	private static final int SUSPENDED   = TelephonyManager.DATA_SUSPENDED;
	private static final int TASK_ACTION = Task.idNew();
	
	private int state;
	private int action = ACT_NONE;
	private boolean waiter = false;
	private ITelephony itel;

	private final Task taskAction = new Task() {
		@Override
		public void run() {
			synchronized(MobDataTracker.this) {
				try {
					if(     action == ACT_ENABLE ) itel. enableDataConnectivity();
					else if(action == ACT_DISABLE) itel.disableDataConnectivity();
				} catch(Exception e) {
					action = ACT_NONE;
					state  = SUSPENDED;
				}
			}
		}
	};

	public MobDataTracker()
	{
		itel = Dev.iTel();
		if(itel == null)
			state = SUSPENDED;
		else {
			state = ENABLED;
			A.telMan().listen(this, LISTEN_DATA_CONNECTION_STATE);
		}
	}

	public synchronized void shutdown()
	{
		try {
			if(action != ACT_NONE) {
				waiter = true;
				wait(Conf.DEVS_MIN_RETRY * 2);
			}
		} catch(Exception e) {}
		try { A.telMan().listen(this, LISTEN_NONE); } catch(Exception e) {}
	}

	//public boolean isOn() { return state==ENABLED  || state==ENABLING;  }

	public synchronized boolean willOn() {
		return (action==ACT_NONE && (state==ENABLED || state==ENABLING)) || (action==ACT_ENABLE && Task.has(TASK_ACTION));
	}

	@Override
	public synchronized void onDataConnectionStateChanged(int state)
	{
		this.state = state;
		if(action != ACT_NONE) enable(action == ACT_ENABLE);
		if(waiter) notifyAll();
	}

	public synchronized void enable(boolean enable)
	{
		switch(state) {
			case SUSPENDED:
				break;
			case ENABLING:
				action = enable? ACT_ENABLE : ACT_DISABLE;
				break;
			case DISABLED:
				if(!enable)
					action = ACT_NONE;
				else {
					action = ACT_ENABLE;
					if(!Task.has(TASK_ACTION)) taskAction.exec(TASK_ACTION, Conf.TRACKER_SWITCH_DELAY);
				}
				break;
			case ENABLED:
				if(enable)
					action = ACT_NONE;
				else {
					action = ACT_DISABLE; 
					if(!Task.has(TASK_ACTION)) taskAction.exec(TASK_ACTION, Conf.TRACKER_SWITCH_DELAY); 
				}
				break;
			default:
				if(!enable)
					action = ACT_NONE;
				else {
					action = ACT_ENABLE; 
					if(!Task.has(TASK_ACTION)) taskAction.exec(TASK_ACTION, Conf.DEVS_MIN_RETRY);
				}
		}
		//A.logd("after mobdata enable ("+enable+") : action="+action+", state="+state);
	}

}
