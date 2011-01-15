package cri.sanity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothClass;


public class BtReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent i)
	{
		final PhoneListener pl = PhoneListener.getActiveInstance();
		final boolean conn     = i.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED);
		final boolean bt       = conn || Dev.isBtOn();
		final int oldCount     = pl==null? A.geti(PhoneListener.BTCOUNT_KEY) : pl.btCount;
		final int newCount     = conn? Math.max(oldCount+1,1) : (bt? Math.max(oldCount-1,0) : 0);
		if(oldCount == newCount) return;
		A.putc(PhoneListener.BTCOUNT_KEY, newCount);		// this is the current count of bt devices connected
		A.logd("BtReceiver: connected devices = "+newCount+"; last one is connected = "+conn);
		if(!bt) A.logd("BtReceiver: bluetooth is disabled");
		if(pl == null) return;
		pl.btCount = newCount;
		// assume: if a bt device is connected during call, that bt device is an audio one (like headset)
		// FIX: use a stronger assumption by checking if bt device is an audio one???
		/*
		final BluetoothDevice btDev = i.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
		if(btDev == null) {
			A.logd("BtReceiver skip: null btDev!");
			return;
		}
		final BluetoothClass btClass = btDev.getBluetoothClass();
		if(btClass == null) {
			A.logd("BtReceiver skip: null btClass!");
			return;
		}
		if(!btClass.hasService(BluetoothClass.Service.AUDIO)) {
			A.logd("BtReceiver skip: no audio service");
			return;
		}
//		if(btClass.getMajorDeviceClass() != BluetoothClass.Device.Major.AUDIO_VIDEO) {
//			A.logd("BtReceiver skip: bt device is not a major audio/video one");
//			return;
//		}
//		final int dc = btClass.getDeviceClass();
//		if(dc != BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER &&
//       dc != BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE) {
//			A.logd("BtReceiver skip: bt device "+dc+" is not audio one");
//			return;
//		}
		*/
		pl.updateHeadsetBt(conn);
	}

}
