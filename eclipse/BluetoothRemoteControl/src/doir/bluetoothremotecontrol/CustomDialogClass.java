package doir.bluetoothremotecontrol;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CustomDialogClass extends Dialog implements OnClickListener {

	SharedPreferences pref;

	LayoutDataBase ldb;

	String model = LayoutDataBase.MODEL_1;

	public Button yes, no;
	EditText cName;
	RadioButton rem_temp1, rem_temp2;
	Context context;
	Activity activity;

	public CustomDialogClass(Activity a, Context contex) {
		super(a);

		this.context = contex;
		activity = a;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_device_screen);

		rem_temp1 = (RadioButton) findViewById(R.id.rem_temp_1);
		rem_temp2 = (RadioButton) findViewById(R.id.rem_temp_2);

		rem_temp1.setOnClickListener(this);
		rem_temp2.setOnClickListener(this);

		yes = (Button) findViewById(R.id.btn_yes);
		no = (Button) findViewById(R.id.btn_no);
		cName = (EditText) findViewById(R.id.cName);
		cName.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if ((event.getAction() == KeyEvent.ACTION_UP)
						&& (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
					editFinish();
					return true;
				}
				return false;
			}
		});
		yes.setOnClickListener(this);
		no.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.btn_yes:
			editFinish();
			break;
		case R.id.btn_no:

			dismiss();

			break;

		case R.id.rem_temp_1:
			model = LayoutDataBase.MODEL_1;
			rem_temp2.setChecked(false);
			break;

		case R.id.rem_temp_2:
			model = LayoutDataBase.MODEL_2;
			rem_temp1.setChecked(false);

			break;
		}
	}

	private void addView(String mode, String remName) {
		// TODO Auto-generated method stub
		Typeface face = Typeface.createFromAsset(context.getAssets(), "font/"
				+ MainActivity.fntName);

		TextView remName_txt;
		RelativeLayout rl;
		View view;
		rl = (RelativeLayout) activity.findViewById(R.id.mainLayout);

		rl.setBackgroundResource(R.drawable.controller_bg_nowater);
		rl.removeAllViews();

		if (mode.equals(LayoutDataBase.MODEL_1)) {
			view = activity.getLayoutInflater().inflate(R.layout.g1_layout, rl,
					false);
			rl.addView(view);

			remName_txt = (TextView) activity.findViewById(R.id.textView12);
			remName_txt.setTypeface(face);
			remName_txt.setText(FontReshape.reshape(remName));

		} else if (mode.equals(LayoutDataBase.MODEL_2)) {
			view = activity.getLayoutInflater().inflate(R.layout.g2_layout, rl,
					false);
			rl.addView(view);

			remName_txt = (TextView) activity.findViewById(R.id.textView12);
			remName_txt.setTypeface(face);
			remName_txt.setText(FontReshape.reshape(remName));

		}

	}

	private void editFinish() {
		// TODO Auto-generated method stub
		int count = 0;
		long id;
		String cNameText = cName.getText().toString();

		try {
			pref = activity.getPreferences(Context.MODE_PRIVATE);

			//
			count = pref.getInt(MainActivity.KEY_REMOTE_COUNT, 1);
			String str = String.valueOf(count);
			String txtS = context.getResources().getString(R.string.remote_n);

			if (cNameText.equals(""))
				cNameText = txtS + FontReshape.reshape(str);

			str = "db" + str;

			ldb = new LayoutDataBase(context);

			id = ldb.addModel(model, str, cNameText);

			// Toast.makeText(context, str, Toast.LENGTH_SHORT).show();

			pref.edit()
					.putString(MainActivity.KEY_CURRENT_LAYOUT,
							ldb.fetchModel(String.valueOf(id))).apply();

			pref.edit().putString(MainActivity.KEY_CURRENT_DATABASE, str)
					.apply();
			//
			pref.edit().putInt(MainActivity.KEY_REMOTE_COUNT, count + 1)
					.apply();
			pref.edit().putString(MainActivity.KEY_CURRENT_CNAME, cNameText)
					.apply();

		} catch (Exception e) {

			Toast.makeText(context, "Error in DataBase", Toast.LENGTH_SHORT)
					.show();
		}

		addView(model, cNameText);

		dismiss();
	}

}
