package cri.sanity;

import android.os.Bundle;


public class ScreenRecord extends ActivityScreen
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		on(K.REC_BROWSE, new Click(){ boolean on(){ A.alert(A.tr(R.string.msg_browse_beta)); return true; }});
	}

}
