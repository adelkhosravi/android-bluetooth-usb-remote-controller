package qmars.dotir.usbsmartremote;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;

public class IrHandler {

	private static final int VID = 5824;
	private static final int RTYPE = 0xa1;
	private static final int TTYPE = 0x21;
	
	private static final String ACTION_USB_PERMISSION = "com.example.USB_PERMISSION";

	public static String getActionUsbPermission() {
		return ACTION_USB_PERMISSION;
	}
	
	

	public static int getVid() {
		return VID;
	}

	public static int getRtype() {
		return RTYPE;
	}

	public static int getTtype() {
		return TTYPE;
	}



	Context context;
	PendingIntent permissionIntent;
	UsbManager mManager;

	public IrHandler(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		permissionIntent  = PendingIntent.getBroadcast(context, 0,
				new Intent(ACTION_USB_PERMISSION), 0);
	}

	public void irTransmit(String code) {
		int j = 0;

		byte[] irCode = stringToIr(code);
		while (irCode[j] != 0 || irCode[j + 1] != 0) {
			j++;
		}
		for (UsbDevice device : mManager.getDeviceList().values()) {
			int vId = device.getVendorId();
			
			if(vId == VID && !mManager.hasPermission(device)){
				mManager.requestPermission(device, permissionIntent);
			}

			if (vId == VID && mManager.hasPermission(device)) {
				UsbInterface intf = device.getInterface(0);
				byte b[] = new byte[8];
				UsbDeviceConnection connection = mManager.openDevice(device);
				connection.claimInterface(intf, true);
				connection.controlTransfer(TTYPE, 0x00, 0x00, 0, null, 0, 50);
				for (int i = 0; i < j/4; i++) {
					b[0] = irCode[8 * i + 0];
					b[1] = irCode[8 * i + 1];
					b[2] = irCode[8 * i + 2];
					b[3] = irCode[8 * i + 3];
					b[4] = irCode[8 * i + 4];
					b[5] = irCode[8 * i + 5];
					b[6] = irCode[8 * i + 6];
					b[7] = irCode[8 * i + 7];

					connection.controlTransfer(TTYPE, 0x03, 0x00, 0, b, 8, 50);

				}
				connection.controlTransfer(TTYPE, 0x02, 0x00, 0, null, 0, 50);
			}
		}

	}

	private byte[] stringToIr(String code) {
		// TODO Auto-generated method stub
		byte[] irCode = new byte[400];
		int i = 0;
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(code);
		while (matcher.find()) {
			irCode[i++] = (byte) ((Integer.valueOf(matcher.group(1)) >> 8) & 0x00ff);
			irCode[i++] = (byte) (Integer.valueOf(matcher.group(1)) & 0x00ff);
		}
		return irCode;

	}

	public String findIr() {
		for (UsbDevice device : mManager.getDeviceList().values()) {
			int vId = device.getVendorId();
			
			if(vId == VID && !mManager.hasPermission(device)){
				mManager.requestPermission(device, permissionIntent);
				break;
			}
			
			if(vId == VID && mManager.hasPermission(device)){
				UsbInterface intf = device.getInterface(0);
				UsbDeviceConnection connection = mManager.openDevice(device);
				connection.claimInterface(intf, true);
				connection.controlTransfer(TTYPE, 0x01, 0x00, 0, null, 0, 50);
				SystemClock.sleep(2000);
				
			}
			
			if (vId == VID && mManager.hasPermission(device)) {
				UsbInterface intf = device.getInterface(0);
				byte b[] = new byte[8];
				int count = 0;
				String str = "";
				UsbDeviceConnection connection = mManager.openDevice(device);
				connection.claimInterface(intf, true);
				connection.controlTransfer(RTYPE, 0x00, 0x00, 0, b, 1, 50);
				count = b[0] / 4;
				for (int i = 0; i < count; i++) {
					connection.controlTransfer(RTYPE, (4 * i + 1), 0x00, 0, b,
							8, 50);
					for (int j = 0; j < 8; j += 2) {
						int temp = ((b[j] << 8) & 0xff00) | (b[j + 1] & 0x00ff);
						str = str + temp + ",";
					}
				}
				if (str.equals(""))
					return null;
				else
					return str;
			}
		}
		return null;

	}
}
