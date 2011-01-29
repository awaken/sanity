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
  	on(K.EULA     , new Click(){ boolean on(){ return A.gotoUrl(Conf.EULA_URL); }});
  	on(K.COMMENT  , new Click(){ boolean on(){ return A.gotoMarketDetails();    }});
  	on(K.CHANGELOG, new Click(){ boolean on(){ return alertChangeLog();         }});
  	on(K.MAIL     , new Click(){ boolean on(){ return mailToDeveloper();        }});
  	on(K.PAYPAL   , new Click(){ boolean on(){ return A.gotoDonateUrl();        }});
    on(K.UNINSTALL, new Click(){ boolean on(){ A.alert(A.tr(R.string.msg_uninstall)); return true; }});
  }

	private boolean mailToDeveloper()
	{
		final Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setType("text/html");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{ Conf.AUTHOR_EMAIL });
		i.putExtra(Intent.EXTRA_SUBJECT, A.fullName());
		i.putExtra(Intent.EXTRA_TEXT   , Html.fromHtml(A.tr(R.string.msg_email_body)+"<br />"));
		startActivity(Intent.createChooser(i, A.tr(R.string.msg_email_choose)));
		return true;
	}

}
