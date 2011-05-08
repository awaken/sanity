package cri.sanity.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
	public  static final int COPYSHARE = 6;
	public  static final int REPLY     = 7;
	private static final int DEF       = OKCANC;
	private static final String TITLE  = A.name();

	public  static Activity activity;
	private static String prevOldPwd, prevNewPwd1, prevNewPwd2;

	//---- inner classes

	public static class Click implements DialogInterface.OnClickListener
	{
		protected DialogInterface dlg;
		protected int which;
		@Override
		public final void onClick(DialogInterface dlg, int which) {
			this.dlg   = dlg;
			this.which = which;
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
	
	public static abstract class Timed
	{
		protected DialogInterface dlg;
		protected int hour, mins;
		public Timed() { hour = mins = 0; }
		public Timed(int hour, int mins) { this.hour = hour; this.mins = mins; }
		protected final void dismiss() { dlg.dismiss(); }
		public abstract void on();
	}

	//---- show message (with buttons)

	public static final View layout(int resId) { return LayoutInflater.from(A.app()).inflate(resId, null); }

	public static final AlertDialog msg(String msg) {
		return msg(TITLE, msg, null, null, null, SIMPLE, true);
	}
	public static final AlertDialog msg(String msg, int type) {
		return msg(TITLE, msg, null, null, null, type, true);
	}
	public static final AlertDialog msg(String msg, Click pos, Click neg) {
		return msg(TITLE, msg, pos, neg, null, DEF, true);
	}
	public static final AlertDialog msg(String msg, Click pos, Click neg, int type) {
		return msg(TITLE, msg, pos, neg, null, type, true);
	}
  public static final AlertDialog msg(String msg, Click pos, Click neg, int type, boolean cancelable) {
  	return msg(TITLE, msg, pos, neg, null, type, cancelable);
  }
  public static final AlertDialog msg(String msg, Click pos, Click neg, Click neu, int type) {
  	return msg(TITLE, msg, pos, neg, neu, type, true);
  }
  public static final AlertDialog msg(String msg, Click pos, Click neg, Click neu, int type, boolean cancelable) {
  	return msg(TITLE, msg, pos, neg, neu, type, cancelable);
  }
  public static final AlertDialog msg(String title, String msg, Click pos, Click neg, Click neu, int type, boolean cancelable) {
  	return msg(title, msg, pos, neg, neu, type, cancelable, activity);
  }
  public static final AlertDialog msg(String title, String msg, Click pos, Click neg, Click neu, int type, boolean cancelable, Context ctx) {
  	int idPos=0, idNeg=0, idNeu=0;
  	switch(type) {
  		case SIMPLE   : idPos = R.string.close ;                                                  break;
  		case OKCANC   : idPos = R.string.ok    ; idNeg = R.string.canc   ;                        break;
  		case YESNO    : idPos = R.string.yes   ; idNeg = R.string.no     ;                        break;
  		case YESNOCANC: idPos = R.string.yes   ; idNeu = R.string.no     ; idNeg = R.string.canc; break;
  		case OPENDEL  : idPos = R.string.open  ; idNeg = R.string.del    ;                        break;
  		case BAKRES   : idPos = R.string.backup; idNeg = R.string.restore;                        break;
  		case COPYSHARE: idPos = R.string.copy  ; idNeg = R.string.share  ;                        break;
  		case REPLY    : idPos = R.string.reply ; idNeg = R.string.close  ;                        break;
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

  //---- show edit view
 
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
		final Click negClick = neg==null? new Click() : new Click(){ public void on(){ neg.on(edit.getText().toString(),dlg); }};
		adb.setIcon(R.drawable.ic_bar);
		adb.setTitle(title);
		adb.setView(layout);
		adb.setCancelable(true);
		adb.setPositiveButton(R.string.ok  , pos==null? new Click() : new Click(){ public void on(){ pos.on(edit.getText().toString(),dlg); }});
		adb.setNegativeButton(R.string.canc, negClick);
		if(neg != null) adb.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dlg) {
				negClick.dlg = dlg;
				negClick.on();
			}
		});
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
  
  public static final AlertDialog choose(String title, String[] items, Click click) {
  	return choose(title, items, click, activity);
  }
  public static final AlertDialog choose(String title, String[] items, Click click, Context ctx) {
		final AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
		adb.setIcon(R.drawable.ic_bar);
		adb.setTitle(title);
		adb.setItems(items, click);
		adb.setCancelable(true);
  	return adb.show();
  }
  public static final AlertDialog choose(String title, int[] items, Click click, Context ctx) {
		final int n = items.length;
		String[] labels = new String[n];
		for(int i=0; i<n; i++)
			labels[i] = A.s(items[i]);
		return choose(title, labels, click, ctx);
  }
  public static final AlertDialog choose(String title, int[] items, Click click) {
  	return choose(title, items, click, activity);
  }

  //--- time pick
 
  public static final AlertDialog time(String title, final Timed timed, Context ctx) {
		final View   layout = layout(R.layout.alert_time);
    final TimePicker tp = (TimePicker)layout.findViewById(R.id.alert_time_picker);
    final SeekBar    sb = (SeekBar)layout.findViewById(R.id.alert_time_bar);
    final int      hour = timed.hour;
    final int      mins = timed.mins;
		sb.setMax(24*4 - 1);
		sb.setProgress(hour*4 + mins/15);
		sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar sb) { }
			@Override
			public void onStartTrackingTouch(SeekBar sb) { }			
			@Override
			public void onProgressChanged(SeekBar sb, int progr, boolean user) {
				if(!user) return;
				tp.setCurrentHour   (progr / 4);
				tp.setCurrentMinute((progr % 4) * 15);
			}
		});
		tp.setCurrentHour  (hour);
		tp.setCurrentMinute(mins);
		tp.setIs24HourView(true);
		tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hour, int mins) { sb.setProgress(hour*4 + mins/15); }
		});
		final AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
		if(!A.empty(title)) adb.setTitle(title);
		adb.setIcon(R.drawable.ic_bar);
		adb.setView(layout);
		adb.setCancelable(true);
		adb.setPositiveButton(R.string.ok, new Click(){ public void on(){
			timed.hour = tp.getCurrentHour();
			timed.mins = tp.getCurrentMinute();
			timed.on();
		}});
		adb.setNegativeButton(R.string.canc, new Click());
		timed.dlg = adb.create();
		return adb.show();
  }
  public static final AlertDialog time(String title, Timed timed) {
  	return time(title, timed, activity);
  }
  public static final AlertDialog time(Timed timed) {
  	return time(null, timed, activity);
  }
  public static final AlertDialog time(Timed timed, Context ctx) {
  	return time(null, timed, ctx);
  }

  //---- password

  public static final void resetPwd() { prevOldPwd = prevNewPwd1 = prevNewPwd2 = null; }

  public static final AlertDialog pwdChoose(final String oldPwd, final Edited pos, final Click neg) {
  	return pwdChoose(TITLE, oldPwd, pos, neg, activity);
  }
  public static final AlertDialog pwdChoose(final String oldPwd, final Edited pos) {
  	return pwdChoose(TITLE, oldPwd, pos, null, activity);
  }
  public static final AlertDialog pwdChoose(String title, final String oldPwd, final Edited pos) {
  	return pwdChoose(title, oldPwd, pos, null, activity);
  }
  public static final AlertDialog pwdChoose(String title, final String oldPwd, final Edited pos, final Click neg) {
  	return pwdChoose(title, oldPwd, pos, neg, activity);
  }
  public static final AlertDialog pwdChoose(String title, final String oldPwd, final Edited pos, final Click neg, Context ctx) {
		final View     layout   = layout(R.layout.alert_pwd_choose);
    final EditText editOld  = (EditText)layout.findViewById(R.id.alert_pwd_edit_old);
    final EditText editNew1 = (EditText)layout.findViewById(R.id.alert_pwd_edit_new1);
    final EditText editNew2 = (EditText)layout.findViewById(R.id.alert_pwd_edit_new2);
    final CheckBox checkBox = (CheckBox)layout.findViewById(R.id.alert_pwd_check);
    final boolean  hasOld   = !A.empty(oldPwd);
    editOld.setEnabled(hasOld);
    if(prevOldPwd  != null) editOld .setText(prevOldPwd );
    if(prevNewPwd1 != null) editNew1.setText(prevNewPwd1);
    if(prevNewPwd2 != null) editNew2.setText(prevNewPwd2);
    if(hidePwd())
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton view, boolean checked) {
					final int type = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
					editOld .setInputType(type);
					editNew1.setInputType(type);
					editNew2.setInputType(type);
					checkBox.setEnabled(false);
				}
			});
    else {
			final int type = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
			editOld .setInputType(type);
			editNew1.setInputType(type);
			editNew2.setInputType(type);
			checkBox.setEnabled(false);
			checkBox.setChecked(true);
    }
		final AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
		adb.setIcon(R.drawable.ic_bar);
		adb.setTitle(title);
		adb.setView(layout);
		adb.setCancelable(true);
		adb.setPositiveButton(R.string.ok  , pos==null? new Click() : new Click(){ public void on(){
			prevOldPwd  = editOld .getText().toString();
			prevNewPwd1 = editNew1.getText().toString();
			prevNewPwd2 = editNew2.getText().toString();
			if(hasOld && !prevOldPwd.equals(oldPwd)) {
				msg(A.s(R.string.err_pwd_old));
				return;
			}
			if(!prevNewPwd1.equals(prevNewPwd2)) {
				msg(A.s(R.string.err_pwd_new));
				return;
			}
			final String pwd = prevNewPwd1;
			prevOldPwd = prevNewPwd1 = prevNewPwd2 = null;
			pos.on(pwd, dlg);
		}});
		adb.setNegativeButton(R.string.canc, neg==null? new Click() : neg);
		return adb.show();
  }
  public static final AlertDialog pwdChoose(final String oldPwd, final Edited pos, final Click neg, Context ctx) {
  	return pwdChoose(TITLE, oldPwd, pos, neg, ctx);
  }
  public static final AlertDialog pwdChoose(final String oldPwd, final Edited pos, Context ctx) {
  	return pwdChoose(TITLE, oldPwd, pos, null, ctx);
  }

  public static final AlertDialog pwdAsk(final Edited pos, final Click neg) {
  	return pwdAsk(TITLE, pos, neg, activity);
  }
  public static final AlertDialog pwdAsk(String title, final Edited pos, final Click neg) {
  	return pwdAsk(title, pos, neg, activity);
  }
  public static final AlertDialog pwdAsk(String title, final Edited pos, final Click neg, Context ctx) {
		final View   layout = layout(R.layout.alert_pwd_ask);
    final EditText edit = (EditText)layout.findViewById(R.id.alert_pwd_edit);
    final CheckBox checkBox = (CheckBox)layout.findViewById(R.id.alert_pwd_check);
    if(prevOldPwd != null) edit.setText(prevOldPwd);
    if(hidePwd())
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton view, boolean checked) {
					edit.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					checkBox.setEnabled(false);
				}
			});
    else {
			edit.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    	checkBox.setEnabled(false);
    	checkBox.setChecked(true);
    }
		final AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
		adb.setIcon(R.drawable.ic_bar);
		adb.setTitle(title);
		adb.setView(layout);
		adb.setCancelable(false);
		adb.setPositiveButton(R.string.ok, pos==null? new Click() : new Click(){ public void on(){
			pos.on(prevOldPwd = edit.getText().toString(), dlg);
		}});
		adb.setNegativeButton(R.string.canc, neg==null? new Click() : neg);
		return adb.show();
  }
  public static final AlertDialog pwdAsk(final Edited pos, final Click neg, Context ctx) {
  	return pwdAsk(TITLE, pos, neg, ctx);
  }

  private static boolean hidePwd() { return !A.is(K.PWD_CLEAR); }

}
