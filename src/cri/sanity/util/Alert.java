package cri.sanity.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import cri.sanity.*;


public final class Alert
{
	public  static final int NONE      = -1;
	public  static final int SIMPLE    = 0;
	public  static final int OKCANC    = 1;
	public  static final int YESNO     = 2;
	public  static final int YESNOCANC = 3;
	public  static final int OPENDEL   = 4;
	public  static final int BAKRES    = 5;
	private static final int DEF       = OKCANC;

	public  static Activity activity;
	private static final String name = A.name();

	//---- inner classes

	public static class Click implements DialogInterface.OnClickListener
	{
		protected DialogInterface dlg;
		protected int id;
		@Override
		public void onClick(DialogInterface dlg, int id) {
			this.dlg = dlg;
			this.id  = id;
			on();
		}
		protected final void dismiss(){ dlg.dismiss(); }
		// just override this method (default action is to close dialog)
		public void on() { dlg.cancel(); }
	}

	public static abstract class Edited
	{
		protected DialogInterface dlg;
		protected final void dismiss(){ dlg.dismiss(); }
		private void on(String text, DialogInterface dlg) { this.dlg = dlg; on(text); }
		public abstract void on(String text);
	}
	
	//---- public api

	public static final View layout(int resId) { return LayoutInflater.from(A.app()).inflate(resId, null); }

	public static final AlertDialog msg(String msg) {
		return msg(name, msg, null, null, null, SIMPLE, true);
	}
	public static final AlertDialog msg(String msg, int type) {
		return msg(name, msg, null, null, null, type, true);
	}
	public static final AlertDialog msg(String msg, Click pos, Click neg) {
		return msg(name, msg, pos, neg, null, DEF, true);
	}
	public static final AlertDialog msg(String msg, Click pos, Click neg, int type) {
		return msg(name, msg, pos, neg, null, type, true);
	}
  public static final AlertDialog msg(String msg, Click pos, Click neg, int type, boolean cancelable) {
  	return msg(name, msg, pos, neg, null, type, cancelable);
  }
  public static final AlertDialog msg(String msg, Click pos, Click neg, Click neu, int type) {
  	return msg(name, msg, pos, neg, neu, type, true);
  }
  public static final AlertDialog msg(String msg, Click pos, Click neg, Click neu, int type, boolean cancelable) {
  	return msg(name, msg, pos, neg, neu, type, cancelable);
  }
  public static final AlertDialog msg(String title, String msg, Click pos, Click neg, Click neu, int type, boolean cancelable) {
  	return msg(title, msg, pos, neg, neu, type, cancelable, activity);
  }
  public static final AlertDialog msg(String title, String msg, Click pos, Click neg, Click neu, int type, boolean cancelable, Context ctx) {
  	int idPos=0, idNeg=0, idNeu=0;
  	switch(type) {
  		case SIMPLE:    idPos = R.string.ok    ;                                                  break;
  		case OKCANC:    idPos = R.string.ok    ; idNeg = R.string.canc   ;                        break;
  		case YESNO:     idPos = R.string.yes   ; idNeg = R.string.no     ;                        break;
  		case YESNOCANC: idPos = R.string.yes   ; idNeu = R.string.no     ; idNeg = R.string.canc; break;
  		case OPENDEL:   idPos = R.string.open  ; idNeg = R.string.del    ;                        break;
  		case BAKRES:    idPos = R.string.backup; idNeg = R.string.restore;                        break;
  	}
		final AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
		adb.setIcon(R.drawable.ic_bar);
		adb.setTitle(title);
		adb.setMessage(msg);
		adb.setCancelable(cancelable);
		if(idPos > 0) adb.setPositiveButton(idPos, pos==null? new Click() : pos);
		if(idNeg > 0) adb.setNegativeButton(idNeg, neg==null? new Click() : neg);
		if(idNeu > 0) adb.setNeutralButton (idNeu, neu==null? new Click() : neu);
		return adb.show();
	}
  public static final AlertDialog msg(String title, String msg, Click pos, Click neg, Click neu, int type) {
  	return msg(title, msg, pos, neg, neu, type, true);
  }
  public static final AlertDialog msg(String title, String msg, Click pos, Click neg, int type, boolean cancelable) {
  	return msg(title, msg, pos, neg, null, type, cancelable);
  }
  public static final AlertDialog msg(String title, String msg, Click pos, Click neg, int type) {
  	return msg(title, msg, pos, neg, null, type, true);
  }
  public static final AlertDialog msg(String title, String msg, Click pos, Click neg) {
  	return msg(title, msg, pos, neg, null, DEF, true);
  }
  public static final AlertDialog msg(String title, String msg, int type) {
  	return msg(title, msg, null, null, null, type, true);
  }
  public static final AlertDialog msg(String title, String msg) {
  	return msg(title, msg, null, null, null, SIMPLE, true);
  }

  public static final EditText edit(String title, final Edited pos, Context ctx) {
  	return edit(title, pos, null, ctx);
  }
  public static final EditText edit(String title, String text, final Edited pos, Context ctx) {
  	return edit(title, text, pos, null, ctx);
  }
  public static final EditText edit(String title, String text, final Edited pos, final Edited neg, Context ctx) {
  	EditText edit = edit(title, pos, neg, ctx);
		if(text != null) {
			edit.setText(text);
			edit.selectAll();
		}
		return edit;
  }
  public static final EditText edit(String title, final Edited pos, final Edited neg, Context ctx) {
		final View   layout = layout(R.layout.alert_text);
    final EditText edit = (EditText)layout.findViewById(R.id.alert_text_edit);
		final AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
		adb.setIcon(R.drawable.ic_bar);
		adb.setTitle(title);
		adb.setView(layout);
		adb.setCancelable(true);
		adb.setPositiveButton(R.string.ok  , pos==null? new Click() : new Click(){ public void on(){ pos.on(edit.getText().toString(),dlg); }});
		adb.setNegativeButton(R.string.canc, neg==null? new Click() : new Click(){ public void on(){ neg.on(edit.getText().toString(),dlg); }});
		adb.show();
		return edit;
	}
  public static final EditText edit(String title, String text, final Edited pos, final Edited neg) {
  	return edit(title, text, pos, neg, activity);
  }
  public static final EditText edit(String title, final Edited pos, final Edited neg) {
  	return edit(title, pos, neg, activity);
  }
  public static final EditText edit(String title, String text, final Edited pos) {
  	return edit(title, text, pos, null, activity);
  }
  public static final EditText edit(String title, final Edited pos) {
  	return edit(title, pos, null, activity);
  }

}
