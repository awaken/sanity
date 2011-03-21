package cri.sanity;

import java.util.LinkedList;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;


public class BlankActivity extends Activity
{
	private static BlankActivity singleton;
	private static final LinkedList<Runnable> posts = new LinkedList<Runnable>();
	private Handler handler;

	public static final BlankActivity getInstance() { return singleton; }

	public static final void postSingleton(Runnable r)
	{
		synchronized(posts) {
			if(singleton == null) posts.add(r);
			else singleton.handler.post(r);
		}
	}

	public final void postFinish()
	{
		handler.post(new Runnable(){ public void run(){
			singleton = null;
			finish();
		}});
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		handler = new Handler();
		if(!PhoneListener.isRunning() || Intent.ACTION_MAIN.equals(getIntent().getAction())) {
			startActivity(new Intent(A.app(), MainActivity.class));
			finish();
			return;
		}
		if(singleton != null) {
			finish();
			return;
		}
		singleton = this;
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		synchronized(posts) {
			if(posts.isEmpty()) return;
			for(Runnable r : posts)
				handler.post(r);
			posts.clear();
		}
	}
	
	@Override
	public void onDestroy()
	{
		if(singleton == this) singleton = null;
		super.onDestroy();
	}

	@Override
	public void onBackPressed() { }

	@Override
	public boolean onKeyDown(int code, KeyEvent evt)
	{
		if(isBlockedKey(code)) return true;
		return super.onKeyDown(code, evt);
	}

	@Override
	public boolean onKeyUp(int code, KeyEvent evt)
	{
		if(isBlockedKey(code)) return true;
		return super.onKeyUp(code, evt);
	}

	private boolean isBlockedKey(int code)
	{
		switch(code) {
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_MENU:
			case KeyEvent.KEYCODE_SEARCH:
			case KeyEvent.KEYCODE_FOCUS:
			case KeyEvent.KEYCODE_CALL:
			case KeyEvent.KEYCODE_CLEAR:
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_EXPLORER:
			case KeyEvent.KEYCODE_ENVELOPE:
			case KeyEvent.KEYCODE_NOTIFICATION:
				return true;
		}
		return false;
	}

}
