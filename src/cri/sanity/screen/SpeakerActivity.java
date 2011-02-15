package cri.sanity.screen;

import cri.sanity.*;
import android.os.Bundle;


public class SpeakerActivity extends ScreenActivity
{
	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setupProximity();
  }

	private void setupProximity()
	{
  	if(Dev.sensorProxim() != null) {
  		// disable "loud_speaker" when both "auto_speaker" and "speaker_call" are unchecked
    	on(K.SPEAKER_AUTO, new Click(){ public boolean on(){
    		pref(K.SPEAKER_LOUD).setEnabled(is(pref) || isSpeakerCall());
    		return false;
    	}});
  	}
  	else {
  		// if no proximity sensor found, disable automatic speaker
  		setChecked(K.SPEAKER_AUTO, false);
  		setEnabled(K.SPEAKER_AUTO, false);
  	}
  	on(K.SPEAKER_CALL, new Change(){ public boolean on(){
  		pref(K.SPEAKER_LOUD).setEnabled(is(K.SPEAKER_AUTO) || isSpeakerCall());
  		return true;
  	}});
	}

	private boolean isSpeakerCall() { return A.getsi(K.SPEAKER_CALL) > 0; }

}
