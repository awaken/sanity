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
    	on("auto_speaker", new Click(){ boolean on(){
    		findPref("loud_speaker").setEnabled(is(pref) || is("speaker_call"));
    		return false;
    	}});
    	on("speaker_call", new Click(){ boolean on(){
    		findPref("loud_speaker").setEnabled(is(pref) || is("auto_speaker"));
    		return false;
    	}});
  	}
  	else {
  		// if no proximity sensor found: disable all proximity options
  		setEnabled("auto_speaker", false);
  		setChecked("speaker_call", false);
  		findPref("loud_speaker").setDependency("speaker_call");
  	}
	}

}
