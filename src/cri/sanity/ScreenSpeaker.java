package cri.sanity;

import android.os.Bundle;


public class ScreenSpeaker extends ActivityScreen
{
	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setupProximity();
  }

	private void setupProximity()
	{
  	// setup preferences when proximity sensor exists or not
  	if(Dev.sensorProxim() != null) {
  		// disable "loud_speaker" when both "auto_speaker" and "speaker_call" are unchecked
    	on(P.SPEAKER_AUTO, new Click(){ boolean on(){
    		findPref(P.SPEAKER_LOUD).setEnabled(is(pref) || is(P.SPEAKER_CALL));
    		return false;
    	}});
    	on(P.SPEAKER_CALL, new Click(){ boolean on(){
    		findPref(P.SPEAKER_LOUD).setEnabled(is(pref) || is(P.SPEAKER_AUTO));
    		return false;
    	}});
  	}
  	else {
  		// if no proximity sensor found: disable all proximity options
  		setEnabled(P.SPEAKER_AUTO, false);
  		setChecked(P.SPEAKER_CALL, false);
  		findPref(P.SPEAKER_LOUD).setDependency(P.SPEAKER_CALL);
  	}
	}

}
