package qmars.dotir.usbsmartremote;

import java.util.ArrayList;
import java.util.List;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	// pref
	public static final String KEY_FIRST_RUN = "firstRun";
	public static final String KEY_CURRENT_LAYOUT = "clayout";
	public static final String KEY_REMOTE_COUNT = "remotecounter";
	public static final String KEY_CURRENT_DATABASE = "database";
	public static final String KEY_CURRENT_CNAME = "cname";

	SharedPreferences pref;

	// DataBases
	LayoutDataBase ldb;
	public RemoteClass remote;

	// views
	RelativeLayout mainLayout;
	View view;
	TextView remName_txt;
	Typeface face;
	public static final String fntName = "irdast.ttf";

	//
	boolean edit_btn;
	boolean finilized = false;
	private boolean backPressedToExitOnce = false;
	private Toast toast = null;

	//
	IrHandler irhandler;
	Vibrator kVib;
	Menu menu;
	PowerManager.WakeLock wl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FontsOverride.setDefaultFont(this, "DEFAULT", "font/" + fntName);
		FontsOverride.setDefaultFont(this, "MONOSPACE", "font/" + fntName);
		FontsOverride.setDefaultFont(this, "SANS_SERIF", "font/" + fntName);

		new LoadingDevice().execute();

		// setContentView(R.layout.main);

	}

	@Override
	public void onBackPressed() {
		if (backPressedToExitOnce) {
			super.onBackPressed();
		} else {
			this.backPressedToExitOnce = true;
			showToast(this.getResources().getString(R.string.back_str));
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					backPressedToExitOnce = false;
				}
			}, 2000);
		}

	}

	private void showToast(String message) {
		if (this.toast == null) {
			// Create toast if found null, it would he the case of first call
			// only
			this.toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);

		} else if (this.toast.getView() == null) {
			// Toast not showing, so create new one
			this.toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);

		} else {
			// Updating toast message is showing
			this.toast.setText(message);
		}

		// Showing toast finally
		this.toast.show();
	}

	private class LoadingDevice extends AsyncTask<Void, Integer, Void> {

		UsbManager mManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		PendingIntent permissionIntent = PendingIntent.getBroadcast(
				MainActivity.this, 0,
				new Intent(IrHandler.getActionUsbPermission()), 0);

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub

			LinearLayout wl = (LinearLayout) LayoutInflater.from(
					MainActivity.this).inflate(R.layout.first_screen_layout,
					null);
			setContentView(wl);

		}

		@Override
		protected Void doInBackground(Void... params) {

			synchronized (this) {
				boolean loop = true;

				while (loop) {
					for (UsbDevice device : mManager.getDeviceList().values()) {
						int vId = device.getVendorId();

						if (vId == IrHandler.getVid()
								&& !mManager.hasPermission(device)) {
							mManager.requestPermission(device, permissionIntent);
							loop = false;
						}
						if (vId == IrHandler.getVid() && mManager.hasPermission(device))
							loop = false;
					}

				}

				try {
					this.wait(1500);
				} catch (Exception e) {

				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			setContentView(R.layout.activity_main);
			//
			prefContent();
		}

	}

	private void prefContent() {
		// TODO Auto-generated method stub
		face = Typeface.createFromAsset(getAssets(), "font/" + fntName);

		ldb = new LayoutDataBase(this);
		pref = getPreferences(MODE_PRIVATE);
		kVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

		if (pref.getBoolean(KEY_FIRST_RUN, true)) {

			try {
				pref.edit().putBoolean(KEY_FIRST_RUN, false).apply();
				pref.edit().putInt(KEY_REMOTE_COUNT, 1).apply();

			} catch (Exception e) {

			}

		}

		addView(pref.getString(KEY_CURRENT_LAYOUT, KEY_FIRST_RUN),
				pref.getString(KEY_CURRENT_CNAME, ""));
		irhandler = new IrHandler(getApplicationContext());

		
		edit_btn = false;
		finilized = true;
		menu.setGroupEnabled(0, true);
		menu.setGroupVisible(0, true);

	}

	// main function to run expected key press
	public void onClickContent(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.new_dev_btn: // initial new device button from the first
								// screen
			newDeviceScreen();
			break;

		case R.id.help_btn: // help screen should be shown

			break;
		case R.id.keypad_btn:
			numPadShow(); //
			break;
		case R.id.g2_menu_button:
			numPadShow(); //
			break;
		default:
			if (edit_btn) {

				irRet(view.getId());
				edit_btn = !edit_btn;
			} else {
				kVib.vibrate(20);
				irTrans(view.getId());
			}
			break;
		}

	}

	private void irTrans(int id) {

		remote = new RemoteClass(getApplicationContext(), databaseName());
		remote.fetchCodes();

		class IrTrans implements Runnable {
			int _id;

			public IrTrans(int _id) {
				this._id = _id;
			}

			public void run() {
				String code = new String();
				switch (_id) {
				case R.id.power:
					code = remote.getPowerCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.volume_up:
					code = remote.getVolUpCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.volume_dn:
					code = remote.getVolDownCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.channel_up:
					code = remote.getChUpCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.channel_dn:
					code = remote.getChDownCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.mute_btn:
					code = remote.getMuteCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.source:
					code = remote.getSrcCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.keypad_btn:
					// has its code above

					break;

				case R.id.num_0_btn:
					code = remote.getNum0Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.num_1_btn:
					code = remote.getNum1Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.num_2_btn:
					code = remote.getNum2Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.num_3_btn:
					code = remote.getNum3Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.num_4_btn:
					code = remote.getNum4Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.num_5_btn:
					code = remote.getNum5Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.num_6_btn:
					code = remote.getNum6Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.num_7_btn:
					code = remote.getNum7Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.num_8_btn:
					code = remote.getNum8Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.num_9_btn:
					code = remote.getNum9Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.num_dash_btn:
					code = remote.getNumdashCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.num_enter_btn:
					code = remote.getNumretCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;

				// g2 section

				case R.id.g2_blue_buton:
					code = remote.getG2blueCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.g2_info_button:
					code = remote.getG2infoCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.g2_home_button:
					code = remote.getG2homeCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.g2_guide_button:
					code = remote.getG2guideCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;

				case R.id.g2_red_button:
					code = remote.getG2redCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.g2_yellow_button:
					code = remote.getG2yellowCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.g2_green_button:
					code = remote.getG2greenCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.g2_left_button:
					code = remote.getG2voldownCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.g2_right_button:
					code = remote.getG2volupCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.g2_up_button:
					code = remote.getG2chupCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.g2_down_button:
					code = remote.getG2chdownCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.g2_ok_button:
					code = remote.getG2okCode();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;

				case R.id.g2_g1_button:
					code = remote.getG2g1Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.g2_g2_button:
					code = remote.getG2g2Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.g2_g3_button:
					code = remote.getG2g3Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.g2_g4_button:
					code = remote.getG2g4Code();
					if (code != null) {
						irhandler.irTransmit(code);
						// txt.setText("Sended");

					} else {
						// txt.setText("Failed!!");
					}
					break;

				}
			}
		}
		Handler handler = new Handler();
		handler.post(new IrTrans(id));
	}

	private String databaseName() {
		// TODO Auto-generated method stub
		String dataBaseName = pref.getString(KEY_CURRENT_DATABASE, null);
		if (dataBaseName == null) {

			Toast.makeText(getApplicationContext(),
					"Error in DataBase assigning", Toast.LENGTH_SHORT).show();
			return null;
		} else {
			// dataBaseName = "db" + dataBaseName;
			return dataBaseName;
		}

	}

	private void irRet(int id) {

		remote = new RemoteClass(getApplicationContext(), databaseName());

		class RetIr extends AsyncTask<Void, Integer, Void> {

			Dialog wlp;
			boolean state;
			int _id;

			public RetIr(int _id) {
				this._id = _id;
			}

			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				wlp = new Dialog(MainActivity.this);

				LinearLayout wl = (LinearLayout) LayoutInflater.from(
						MainActivity.this).inflate(R.layout.waiting_layout,
						null);

				wlp.requestWindowFeature(Window.FEATURE_NO_TITLE);
				wlp.getWindow().setBackgroundDrawable(
						new ColorDrawable(Color.TRANSPARENT));
				wlp.setCancelable(false);
				wlp.setContentView(wl);
				wlp.show();
			}

			@Override
			protected Void doInBackground(Void... params) {

				String code = irhandler.findIr();
				switch (_id) {
				case R.id.power:
					if (code != null) {
						remote.setPowerCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.volume_up:
					if (code != null) {
						remote.setVolUpCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.volume_dn:
					if (code != null) {
						remote.setVolDownCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.channel_up:
					if (code != null) {
						remote.setChUpCode(code);
						state = true;

					} else {
						// txt.setText("Failed!!");
					}
					break;
				case R.id.channel_dn:
					if (code != null) {
						remote.setChDownCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.mute_btn:
					if (code != null) {
						remote.setMuteCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.source:
					if (code != null) {
						remote.setSrcCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.keypad_btn:
					if (code != null) {
						remote.setChListCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.num_0_btn:
					if (code != null) {
						remote.setNum0Code(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.num_1_btn:
					if (code != null) {
						remote.setNum1Code(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.num_2_btn:
					if (code != null) {
						remote.setNum2Code(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.num_3_btn:
					if (code != null) {
						remote.setNum3Code(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.num_4_btn:
					if (code != null) {
						remote.setNum4Code(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.num_5_btn:
					if (code != null) {
						remote.setNum5Code(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.num_6_btn:
					if (code != null) {
						remote.setNum6Code(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.num_7_btn:
					if (code != null) {
						remote.setNum7Code(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.num_8_btn:
					if (code != null) {
						remote.setNum8Code(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.num_9_btn:
					if (code != null) {
						remote.setNum9Code(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.num_dash_btn:
					if (code != null) {
						remote.setNumdashCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.num_enter_btn:
					if (code != null) {
						remote.setNumretCode(code);
						state = true;

					} else {
						state = false;
					}
					break;

				// g2 section

				case R.id.g2_green_button:
					if (code != null) {
						remote.setG2greenCode(code);
						state = true;

					} else {
						state = false;
					}
					break;

				case R.id.g2_red_button:
					if (code != null) {
						remote.setG2redCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.g2_blue_buton:
					if (code != null) {
						remote.setG2blueCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.g2_yellow_button:
					if (code != null) {
						remote.setG2yellowCode(code);
						state = true;

					} else {
						state = false;
					}
					break;

				case R.id.g2_info_button:
					if (code != null) {
						remote.setG2infoCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.g2_home_button:
					if (code != null) {
						remote.setG2homeCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.g2_guide_button:
					if (code != null) {
						remote.setG2guideCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.g2_up_button:
					if (code != null) {
						remote.setG2chupCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.g2_down_button:
					if (code != null) {
						remote.setG2chdownCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.g2_left_button:
					if (code != null) {
						remote.setG2voldownCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.g2_right_button:
					if (code != null) {
						remote.setG2volupCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.g2_ok_button:
					if (code != null) {
						remote.setG2okCode(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.g2_g1_button:
					if (code != null) {
						remote.setG2g1Code(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.g2_g2_button:
					if (code != null) {
						remote.setG2g2Code(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.g2_g3_button:
					if (code != null) {
						remote.setG2g3Code(code);
						state = true;

					} else {
						state = false;
					}
					break;
				case R.id.g2_g4_button:
					if (code != null) {
						remote.setG2g4Code(code);
						state = true;

					} else {
						state = false;
					}
					break;

				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				// TODO Auto-generated method stub
				if (state == true)
					Toast.makeText(
							getApplicationContext(),
							MainActivity.this.getResources().getText(
									R.string.rec_txt), Toast.LENGTH_LONG)
							.show();

				else
					Toast.makeText(
							getApplicationContext(),
							MainActivity.this.getResources().getText(
									R.string.nrec_txt), Toast.LENGTH_LONG)
							.show();

				wlp.dismiss();
			}

		}

		new RetIr(id).execute();
	}

	private void newDeviceScreen() {
		// TODO Auto-generated method stub
		CustomDialogClass cdd = new CustomDialogClass(this,
				getApplicationContext());
		cdd.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		cdd.show();
	}

	private void deviceSelectScreen() {
		// TODO Auto-generated method stub

		final List<String> listItems = new ArrayList<String>();

		if (ldb.fetchLastRow() != null
				&& ldb.fetchLastRow().equals("0") != true) {

			int cCount = Integer.valueOf(ldb.fetchLastRow());
			for (int i = 1; i <= cCount; i++) {
				listItems.add(ldb.fetchcName(String.valueOf(i)));
			}
			final CharSequence[] items = listItems
					.toArray(new CharSequence[listItems.size()]);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("دستگاه را انتخاب کنید");

			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					addView(ldb.fetchModel(String.valueOf(item + 1)),
							ldb.fetchcName(String.valueOf(item + 1)));

					pref.edit()
							.putString(KEY_CURRENT_LAYOUT,
									ldb.fetchModel(String.valueOf(item + 1)))
							.apply();

					pref.edit()
							.putString(KEY_CURRENT_DATABASE,
									ldb.fetchdbName(String.valueOf(item + 1)))
							.apply();

					pref.edit()
							.putString(KEY_CURRENT_CNAME,
									ldb.fetchcName(String.valueOf(item + 1)))
							.apply();

				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			Toast.makeText(getApplicationContext(), "دستگاهی وجود ندارد",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void addView(String mode, String remName) {
		// TODO Auto-generated method stub
		RelativeLayout rl;
		View view;
		rl = (RelativeLayout) findViewById(R.id.mainLayout);

		rl.setBackgroundResource(R.drawable.controller_bg_nowater);

		rl.removeAllViews();

		if (mode.equals(LayoutDataBase.MODEL_1)) {

			rl.setBackgroundResource(R.drawable.controller_bg_nowater);

			view = getLayoutInflater().inflate(R.layout.g1_layout, rl, false);
			rl.addView(view);

			remName_txt = (TextView) view.findViewById(R.id.textView12);
			remName_txt.setTypeface(face);
			remName_txt.setText(FontReshape.reshape(remName));

		} else if (mode.equals(LayoutDataBase.MODEL_2)) {
			rl.setBackgroundResource(R.drawable.controller_bg_nowater);
			view = getLayoutInflater().inflate(R.layout.g2_layout, rl, false);
			rl.addView(view);

			remName_txt = (TextView) view.findViewById(R.id.textView12);
			remName_txt.setTypeface(face);
			remName_txt.setText(FontReshape.reshape(remName));

		} else {
			view = getLayoutInflater().inflate(R.layout.first_run, rl, false);
			rl.addView(view);
			rl.setBackgroundResource(R.drawable.controller_bg);

		}

	}

	private void deleteDevice() {
		// TODO Auto-generated method stub

		if (ldb.fetchLastRow() != null
				&& ldb.fetchLastRow().equals("0") != true) {

			final List<String> listItems = new ArrayList<String>();

			int cCount = Integer.valueOf(ldb.fetchLastRow());
			for (int i = 1; i <= cCount; i++) {
				listItems.add(ldb.fetchcName(String.valueOf(i)));
			}
			final CharSequence[] items = listItems
					.toArray(new CharSequence[listItems.size()]);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("دستگاه را انتخاب کنید");
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					String cdbName = ldb.fetchdbName(String.valueOf(item + 1));

					if (cdbName.equals(pref.getString(KEY_CURRENT_DATABASE,
							null))) {
						if (item > 0) {

							addView(ldb.fetchModel(String.valueOf(item)),
									ldb.fetchcName(String.valueOf(item)));

							// remName_txt.setText(listItems.get(item));

							pref.edit()
									.putString(
											KEY_CURRENT_LAYOUT,
											ldb.fetchModel(String.valueOf(item)))
									.apply();
							pref.edit()
									.putString(
											KEY_CURRENT_DATABASE,
											ldb.fetchdbName(String
													.valueOf(item))).apply();
							pref.edit()
									.putString(
											KEY_CURRENT_CNAME,
											ldb.fetchcName(String.valueOf(item)))
									.apply();

						} else {
							if (ldb.fetchModel(String.valueOf(item + 2)) == null) {

								addView(KEY_FIRST_RUN, "");
								pref.edit()
										.putString(KEY_CURRENT_LAYOUT,
												KEY_FIRST_RUN).apply();
								pref.edit().putString(KEY_CURRENT_CNAME, "")
										.apply();

							} else {

								addView(ldb
										.fetchModel(String.valueOf(item + 2)),
										ldb.fetchcName(String.valueOf(item + 2)));

								// remName_txt.setText(listItems.get(item + 2));

								pref.edit()
										.putString(
												KEY_CURRENT_LAYOUT,
												ldb.fetchModel(String
														.valueOf(item + 2)))
										.apply();
								pref.edit()
										.putString(
												KEY_CURRENT_DATABASE,
												ldb.fetchdbName(String
														.valueOf(item + 2)))
										.apply();
								pref.edit()
										.putString(
												KEY_CURRENT_CNAME,
												ldb.fetchcName(String
														.valueOf(item + 2)))
										.apply();
							}
						}
					}

					getApplicationContext().deleteDatabase(
							ldb.fetchdbName(String.valueOf(item + 1)));
					ldb.deleteRow(String.valueOf(item + 1));

				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			Toast.makeText(getApplicationContext(), "دستگاهی وجود ندارد",
					Toast.LENGTH_SHORT).show();
		}

	}

	private void numPadShow() {
		// TODO Auto-generated method stub
		Dialog numpad = new Dialog(this);

		LinearLayout pad = (LinearLayout) LayoutInflater.from(this).inflate(
				R.layout.controlpad, null);

		numpad.requestWindowFeature(Window.FEATURE_NO_TITLE);
		numpad.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		numpad.setContentView(pad);
		numpad.show();

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		this.menu = menu;
		if (!finilized) {
			menu.setGroupEnabled(0, false);
			menu.setGroupVisible(0, false);
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.device_select_menu:
			deviceSelectScreen();

			break;

		case R.id.new_device_menu:
			newDeviceScreen();
			break;

		case R.id.button_edit_menu:
			edit_btn = true;
			break;
		case R.id.button_delete_menu:
			deleteDevice();
			break;

		}

		return super.onOptionsItemSelected(item);
	}
}
