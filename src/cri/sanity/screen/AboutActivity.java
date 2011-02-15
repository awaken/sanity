package cri.sanity.screen;

import java.util.Currency;
import java.util.Locale;

import cri.sanity.*;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;


public class AboutActivity extends ScreenActivity
{
	private static final String CURRENCY_VAR = "$CUR";
	private static final String NAME_VAR     = "$NAME";
	private static final String EMAIL_VAR    = "$EMAIL";
	private static final String DONATE_URL   = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business="+EMAIL_VAR+"&item_name="+NAME_VAR+"&currency_code="+CURRENCY_VAR+"&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted";
	private static final String EULA_URL     = "http://www.gnu.org/licenses";

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
		skipAllKeys = true;
    super.onCreate(savedInstanceState);
  	on(K.EULA     , new Click(){ public boolean on(){ return A.gotoUrl(EULA_URL);    }});
  	on(K.COMMENT  , new Click(){ public boolean on(){ return A.gotoMarketDetails();  }});
  	on(K.CHANGELOG, new Click(){ public boolean on(){ return alertChangeLog();       }});
  	on(K.MAIL     , new Click(){ public boolean on(){ return mailToDeveloper();      }});
  	on(K.PAYPAL   , new Click(){ public boolean on(){ return A.gotoUrl(donateUrl()); }});
  	if(A.SDK < 8) setEnabled(K.UNINSTALL, false);
  	else on(K.UNINSTALL, new Click(){ public boolean on(){ A.alert(A.tr(R.string.msg_uninstall)); return true; }});
  }

	private boolean mailToDeveloper()
	{
		final Intent i = new Intent(Intent.ACTION_SEND);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setType("text/html");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{ Conf.AUTHOR_EMAIL });
		i.putExtra(Intent.EXTRA_SUBJECT, appName());
		i.putExtra(Intent.EXTRA_TEXT   , Html.fromHtml(A.tr(R.string.msg_email_body)+"<br />"));
		startActivity(Intent.createChooser(i, A.tr(R.string.msg_email_choose)));
		return true;
	}
	
	private static final String appName() {
		String name = A.fullName();
		String full = A.isFull()? A.FULL? "Full" : "Donate" : "";
		if(full.length() > 0) name += " ("+full+')';
		return name;
	}

	private static final String donateUrl() {
		return DONATE_URL.replace(NAME_VAR    , Uri.encode(appName()))
		                 .replace(EMAIL_VAR   , Uri.encode(Conf.AUTHOR_EMAIL))
		                 .replace(CURRENCY_VAR, Uri.encode(Currency.getInstance(Locale.getDefault()).getCurrencyCode()));
	}

}
