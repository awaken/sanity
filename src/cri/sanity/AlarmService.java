package cri.sanity;

import java.util.HashMap;
import java.util.Map;
import android.content.Intent;
import android.os.Bundle;


public final class AlarmService extends WakeService
{
	private static Map<String,Action> actionMap;

	@Override
	public void onCreate() {
		super.onCreate();
		if(actionMap == null) actionMap = new HashMap<String,Action>();
	}

	@Override
	public int onStartCommand(Intent i, int flags, int id) {
		if(i == null) return START_STICKY;
		final String  action  = i.getAction();
		if(action == null) return START_STICKY;
		synchronized(actionMap) {
			final Action a = actionMap.get(action);
			if(a != null) try { a.interrupt(); } catch(Exception e) {}
			actionMap.put(action, new Action(action, i.getExtras()));
		}
		return START_STICKY;
	}

	private class Action extends Thread
	{
		private String action;
		private Bundle extras;

		private Action(String action, Bundle extras) {
			this.action = action;
			this.extras = extras;
			start();
		}

		@Override
		public void run() {
			yield();
			new Alarmer(extras).runAction(action);
			synchronized(actionMap) {
				actionMap.remove(action);
				if(actionMap.isEmpty()) stopSelf();
			}
		}
	}

}
