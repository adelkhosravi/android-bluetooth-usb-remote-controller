package doir.bluetoothremotecontrol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import doir.bluetoothremotecontrol.CustomDialogClass;
import doir.bluetoothremotecontrol.FontReshape;
import doir.bluetoothremotecontrol.FontsOverride;
import doir.bluetoothremotecontrol.IrHandler;
import doir.bluetoothremotecontrol.LayoutDataBase;
import doir.bluetoothremotecontrol.MainActivity;
import doir.bluetoothremotecontrol.R;
import doir.bluetoothremotecontrol.R.id;
import doir.bluetoothremotecontrol.RemoteClass;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	// Well known SPP UUID
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public String address;
	int bCounter;

	public IrHandler irhandler;

	public BluetoothAdapter btAdapter = null;
	private BluetoothDevice btDevice = null;
	private BluetoothSocket btSocket = null;
	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

	// pref
	public static final String KEY_FIRST_RUN = "firstRun";
	public static final String KEY_CURRENT_LAYOUT = "clayout";
	public static final String KEY_REMOTE_COUNT = "remotecounter";
	public static final String KEY_CURRENT_DATABASE = "database";
	public static final String KEY_CURRENT_CNAME = "cname";
	public static final String KEY_DEV_ADDRESS = "devaddress";
	public static final String KEY_ADDRESS_EX = "addressex";

	SharedPreferences pref;

	// DataBases
	LayoutDataBase ldb;
	public RemoteClass remote;

	// views
	RelativeLayout mainLayout;
	LinearLayout st_l;
	View view;
	TextView remName_txt;
	Typeface face;
	Button cnt_btn, hlp_btn, man_btn;

	public static final String fntName = "irdast.ttf";

	//
	boolean edit_btn;
	boolean finilized = false;
	private boolean backPressedToExitOnce = false;
	private Toast toast = null;

	//
	Vibrator kVib;
	Menu menu;
	PowerManager.WakeLock wl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FontsOverride.setDefaultFont(this, "DEFAULT", "font/" + fntName);
		FontsOverride.setDefaultFont(this, "MONOSPACE", "font/" + fntName);
		FontsOverride.setDefaultFont(this, "SANS_SERIF", "font/" + fntName);

		irhandler = new IrHandler(getApplicationContext());
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		registerReceiver(mReceiver, filter);
		chAddress();
		loadingMenu();

		// setContentView(R.layout.main);

	}

	@Override
	public void onBackPressed() {
		if (backPressedToExitOnce) {
			unregisterReceiver(mReceiver);
			try {
				btSocket.close();
			} catch (IOException e2) {

			}
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

	private void loadingMenu() {
		// TODO Auto-generated method stub
		bCounter = 1;
		mDeviceList = new ArrayList<BluetoothDevice>();
		st_l = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(
				R.layout.first_screen_layout, null);
		cnt_btn = (Button) st_l.findViewById(R.id.connect_init);
		hlp_btn = (Button) st_l.findViewById(R.id.help_init);
		man_btn = (Button) st_l.findViewById(R.id.manBlue_init);
		setContentView(st_l);
		cnt_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				irhandler.findDevice();
				bCounter--;
				address = "-1";
			}
		});

		man_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LayoutInflater li = LayoutInflater.from(MainActivity.this);
				View promptsView = li.inflate(R.layout.man_mac, null);

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						MainActivity.this);

				// set prompts.xml to alertdialog builder
				alertDialogBuilder.setView(promptsView);

				final EditText mc1 = (EditText) promptsView
						.findViewById(R.id.mc_et1);
				mc1.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
				final EditText mc2 = (EditText) promptsView
						.findViewById(R.id.mc_et2);
				mc2.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
				final EditText mc3 = (EditText) promptsView
						.findViewById(R.id.mc_et3);
				mc3.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
				final EditText mc4 = (EditText) promptsView
						.findViewById(R.id.mc_et4);
				mc4.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
				final EditText mc5 = (EditText) promptsView
						.findViewById(R.id.mc_et5);
				mc5.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
				final EditText mc6 = (EditText) promptsView
						.findViewById(R.id.mc_et6);
				mc6.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

				alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										address = mc1.getText() + ":"
												+ mc2.getText() + ":"
												+ mc3.getText() + ":"
												+ mc4.getText() + ":"
												+ mc5.getText() + ":"
												+ mc6.getText();
										// "00:BA:55:57:6B:93" 00:BA:55:57:6B:93
										irhandler.findDevice();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				alertDialog.getWindow().setSoftInputMode(
						WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

				// show it
				alertDialog.show();

			}
		});

		if (irhandler.isEnable()) {
			irhandler.findDevice();
		} else {
			irhandler.enableBT();
		}

	}

	private void errorExit(String title, String message) {
		Toast msg = Toast.makeText(getBaseContext(), title + " - " + message,
				Toast.LENGTH_SHORT);
		msg.show();
		finish();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				if (irhandler.isEnable()) {
					irhandler.findDevice();
				} else {
					irhandler.enableBT();
				}
			}
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				irhandler.cancelDis();
				mDeviceList.add((BluetoothDevice) intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
			}
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				cnt_btn.setText("در حال جستجو...");
				cnt_btn.setEnabled(false);

			}
			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				cnt_btn.setText("جستجو");
				cnt_btn.setEnabled(true);
				blueList();

			}

		}
	};

	private void blueList() {
		// TODO Auto-generated method stub

		if (address == "-1") {
			if (!mDeviceList.isEmpty()) {

				final List<String> listItems = new ArrayList<String>();

				int cCount = mDeviceList.size();
				for (int i = 0; i < cCount; i++) {
					listItems.add(mDeviceList.get(i).getName());
				}
				final CharSequence[] items = listItems
						.toArray(new CharSequence[listItems.size()]);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("دستگاه را انتخاب کنید");
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (!irhandler.connect(mDeviceList.get(item)
								.getAddress())) {
							showToast("امکان ارتباط با دستگاه وجود ندارد");
						} else {
							putAddress(mDeviceList.get(item).getAddress());
							setContentView(R.layout.activity_main);
							//
							prefContent();
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			} else {
				Toast.makeText(getApplicationContext(), "دستگاهی پیدا نشد!",
						Toast.LENGTH_SHORT).show();
				if (bCounter < 1) {
					man_btn.setVisibility(View.VISIBLE);
				}
			}
		} else {
			address = address.substring(0, 17);
			if (!irhandler.connect(address)) {
				showToast("امکان ارتباط با دستگاه وجود ندارد");
				if (bCounter < 1)
					man_btn.setVisibility(View.VISIBLE);
			} else {
				setContentView(R.layout.activity_main);
				prefContent();
				
			}

		}

	}

	@Override
	public void onPause() {
		if (btAdapter != null) {
			if (btAdapter.isDiscovering()) {
				btAdapter.cancelDiscovery();
			}
		}

		super.onPause();
	}

	@Override
	public void onDestroy() {
		/*
		 * unregisterReceiver(mReceiver); try { btSocket.close(); } catch
		 * (IOException e2) {
		 * 
		 * }
		 */
		super.onDestroy();
	}

	private void putAddress(String address) {

		this.address = address;
		try {
			pref.edit().putString(KEY_DEV_ADDRESS, address).apply();
			pref.edit().putBoolean(KEY_ADDRESS_EX, true).apply();
			showToast(address);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void chAddress() {
		// TODO Auto-generated method stub
		pref = getPreferences(MODE_PRIVATE);
		if (pref.getBoolean(KEY_ADDRESS_EX, true)) {

			try {
				address = pref.getString(KEY_DEV_ADDRESS, "-1");
			} catch (Exception e) {
				address = "-1";
			}

		} else {
			address = "-1";
		}

	}

	private void prefContent() {
		// TODO Auto-generated method stub
		face = Typeface.createFromAsset(getAssets(), "font/" + fntName);

		ldb = new LayoutDataBase(this);
		// pref = getPreferences(MODE_PRIVATE);
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
			//newDeviceScreen();
			irhandler.testSend("salam baba");
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
