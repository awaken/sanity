package cri.sanity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class RecActivity extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if(RecService.isRunning())
			A.app().startService(new Intent(A.app(), RecService.class));		// there is a phone call: let's record
		else {
			final Intent i = new Intent(A.app(), MainActivity.class);						// no phone call now: run main activity
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			A.app().startActivity(i);
		}
		finish();
	}

}
