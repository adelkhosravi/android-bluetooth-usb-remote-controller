package doir.bluetoothremotecontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class IrHandler {

	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final byte RTYPE = (byte) 0xA1;
	private static final byte TTYPE = (byte) 0x21;
	private static final byte TYPE0 = (byte) 0x00;
	private static final byte TYPE1 = (byte) 0x01;
	private static final byte TYPE2 = (byte) 0x02;
	private static final byte ENDF = (byte) 0x66;

	Context context;
	private BluetoothAdapter btAdapter = null;
	private BluetoothDevice btDevice = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private InputStream inputStream = null;
	private boolean stopWorker;
	Thread workerThread;

	public IrHandler(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		btAdapter = BluetoothAdapter.getDefaultAdapter();

	}

	public void enableBT() {
		// TODO Auto-generated constructor stub
		if (btAdapter != null) {
			btAdapter.enable();
		}
	}

	public boolean isEnable() {
		// TODO Auto-generated constructor stub
		return btAdapter.isEnabled();
	}

	public void findDevice() {
		// TODO Auto-generated constructor stub
		if (btAdapter != null && !btAdapter.isDiscovering()) {
			btAdapter.startDiscovery();
		}
	}

	public BluetoothDevice thisDevice() {
		// TODO Auto-generated constructor stub
		return btDevice;
	}

	public boolean connect(String address) {
		// TODO Auto-generated constructor stub
		boolean result = false;
		btDevice = btAdapter.getRemoteDevice(address);

		try {
			btSocket = btDevice.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {

		}

		btAdapter.cancelDiscovery();
		Log.d("SPP", "...Connecting to Remote...");
		try {
			btSocket.connect();
			Log.d("SPP", "...Connection established and data link opened...");

		} catch (IOException e) {

		}

		Log.d("SPP", "...Creating Socket...");

		try {
			outStream = btSocket.getOutputStream();
			inputStream = btSocket.getInputStream();
			result = true;
		} catch (IOException e) {

		}

		return result;
	}

	public void cancelDis() {
		// TODO Auto-generated constructor stub
		if (btAdapter != null && btAdapter.isDiscovering()) {
			btAdapter.cancelDiscovery();
		}
	}

	public void irTransmit(String code) {
		int j = 0;

		byte[] irCode = stringToIr(code, TTYPE, TYPE0);

		sendBT(irCode);

	}

	public void testSend(String str) {
		byte b[] = stringToIr("dummy", TTYPE, TYPE1);
		sendBT(b);
		//beginListenForData();
	}

	private void sendBT(byte[] b) {
		if (btAdapter != null) {
			try {
				outStream.write(b);
			} catch (IOException e) {

			}
		}
		if (!btSocket.isConnected()) {
			Toast.makeText(context, "not connected", Toast.LENGTH_SHORT).show();
		}
	}

	private byte[] stringToIr(String code, byte bmType, byte type) {
		// TODO Auto-generated method stub
		byte[] irCode = new byte[400];
		int i = 0;
		int j = 0;
		irCode[i++] = bmType;
		irCode[i++] = type;
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(code);
		while (matcher.find()) {
			irCode[i++] = (byte) ((Integer.valueOf(matcher.group(1)) >> 8) & 0x00ff);
			irCode[i++] = (byte) (Integer.valueOf(matcher.group(1)) & 0x00ff);
		}
		irCode[i++] = ENDF;
		irCode[i++] = ENDF;
		irCode[i] = ENDF;
		byte[] rsp = new byte[i];
		for (j = 0; j < i; j++) {
			rsp[j] = irCode[j];
		}

		return rsp;

	}

	public String findIr() {
		int numByte = 0;
		byte[] b = new byte[256];
		byte tmp[] = stringToIr("dummy", TTYPE, TYPE1);
		sendBT(tmp);
		SystemClock.sleep(5000);
		tmp = stringToIr("dummy", RTYPE, TYPE0);
		sendBT(tmp);
		try {
			numByte = inputStream.available();
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (numByte > 0) {
			try {
				inputStream.read(b);
			} catch (Exception e) {

			}
		}

		int count = 0;
		String str = "";
		tmp = stringToIr("dummy", RTYPE, TYPE0);
		return "";
	}

	void beginListenForData() {

		stopWorker = false;

		workerThread = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted() && !stopWorker) {
					try {
						int bytesAvailable = inputStream.available();
						if (bytesAvailable > 0) {
							Toast.makeText(context, "num" + bytesAvailable,
									Toast.LENGTH_SHORT).show();
						}
					} catch (IOException ex) {
						stopWorker = true;
					}
				}
			}
		});

		workerThread.start();
	}
}
