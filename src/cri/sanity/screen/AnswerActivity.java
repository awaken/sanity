package cri.sanity.screen;

import android.os.Bundle;
import cri.sanity.*;


public class AnswerActivity extends ScreenActivity
{
	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    fullOnly(K.ANSWER_HEADSET, K.ANSWER_SKIP, K.ANSWER_DELAY+K.WS);
  }
}
