package cri.sanity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;


public class ScreenAbout extends ActivityScreen
{
	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
  	on(P.EULA     , new Click(){ boolean on(){ return A.gotoUrl(Conf.EULA_URL); }});
  	on(P.COMMENT  , new Click(){ boolean on(){ return A.gotoMarketDetails();    }});
  	on(P.CHANGELOG, new Click(){ boolean on(){ return alertChangeLog();         }});
  	on(P.MAIL     , new Click(){ boolean on(){ return mailToDeveloper();        }});
  	on(P.PAYPAL   , new Click(){ boolean on(){ return A.gotoDonateUrl();        }});
  }

	private boolean mailToDeveloper()
	{
		final Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setType("text/html");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{ Conf.AUTHOR_EMAIL });
		i.putExtra(Intent.EXTRA_SUBJECT, getAppFullName());
		i.putExtra(Intent.EXTRA_TEXT   , Html.fromHtml(A.tr(R.string.msg_email_body)+"<br />"));
		startActivity(Intent.createChooser(i, A.tr(R.string.msg_email_choose)));
		return true;
	}

}
