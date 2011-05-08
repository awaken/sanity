package cri.sanity.screen;

import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import cri.sanity.*;
import cri.sanity.util.*;


public class AboutActivity extends ScreenActivity
{
	private static final String PRICE        = "3";
	private static final String CURRENCY     = "EUR";
	private static final String NAME_VAR     = "$NAME";
	private static final String EMAIL_VAR    = "$EMAIL";
	private static final String DONATE_URL   = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business="+EMAIL_VAR+"&item_name="+NAME_VAR+"&currency_code="+CURRENCY+"&amount="+PRICE+"&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted";
	private static final String EULA_URL     = "http://www.gnu.org/licenses";
	private static final String FORUM_URL    = "http://tagliamonte.net/forum";
	private static final String AUTHOR_EMAIL = "cristiano@tagliamonte.net";

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
		secure      = false;
		skipAllKeys = true;
    super.onCreate(savedInstanceState);
  	on("changelog", new Click(){ public boolean on(){ return alertChangeLog();            }});
  	on("eula"     , new Click(){ public boolean on(){ return Goto.url(EULA_URL);          }});
  	on("forum"    , new Click(){ public boolean on(){ return Goto.url(FORUM_URL);         }});
  	on("paypal"   , new Click(){ public boolean on(){ return Goto.url(donateUrl());       }});
  	on("comment"  , new Click(){ public boolean on(){ return Goto.marketDetails(A.pkg()); }});
  	on("mail"     , new Click(){ public boolean on(){ return mailToDeveloper();           }});
  }

	private boolean mailToDeveloper()
	{
		final Intent i = new Intent(Intent.ACTION_SEND);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setType("text/html");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{ AUTHOR_EMAIL });
		i.putExtra(Intent.EXTRA_SUBJECT, subject());
		i.putExtra(Intent.EXTRA_TEXT   , Html.fromHtml(A.s(R.string.msg_email_body)+"<br />"));
		startActivity(Intent.createChooser(i, A.s(R.string.msg_email_choose)));
		return true;
	}
	
	private static String subject() {
		return fullName()+(A.isFull()?Conf.FULL?" (Full)":" (Donate)":"")+"  -  id:"+A.telMan().getDeviceId()+"  ["+googleAccount()+']';
	}

	private static String donateUrl() {
		return DONATE_URL.replace(NAME_VAR , Uri.encode(subject()))
		                 .replace(EMAIL_VAR, Uri.encode(AUTHOR_EMAIL));
	}
	
	private static String googleAccount() {
		try {
		 return AccountManager.get(A.app()).getAccountsByType("com.google")[0].name.trim().toLowerCase();
		} catch(Exception e) {
			return null;
		}
	}

}
