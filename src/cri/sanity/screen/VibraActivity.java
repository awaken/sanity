package cri.sanity.screen;

import android.os.Bundle;
import cri.sanity.*;
import cri.sanity.util.Vibra;


public class VibraActivity extends ScreenActivity
{
	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    on("vibrate_test", new Click(){ public boolean on(){
    	Vibra.setMode();
    	Vibra.vibra();
    	return true;
    }});

    fullOnly(K.VIBRATE_MODE);
  }

}
