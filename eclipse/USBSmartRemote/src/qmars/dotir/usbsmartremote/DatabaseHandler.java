package qmars.dotir.usbsmartremote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	private String DATABASE_NAME;


	private static final String KEY_ID = "id";
	private static final String KEY_BUTTON = "btn";
	private static final String KEY_CODE = "code";

	public DatabaseHandler(Context context, String name, int version) {
		// TODO Auto-generated constructor stub
		super(context, name, null, version);
		DATABASE_NAME = name;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_REMOTE_TABLE = "CREATE TABLE " + DATABASE_NAME + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_BUTTON + " TEXT,"
				+ KEY_CODE + " TEXT" + ")";
		db.execSQL(CREATE_REMOTE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);

		// Create tables again
		onCreate(db);
	}

	void addDevice(String btn, String code) {
		
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_BUTTON, btn);
		values.put(KEY_CODE, code);
		
		if(fetchRemote(btn)==null){
			db.insert(DATABASE_NAME, null, values);
			db.close();
		} else {
			db.update(DATABASE_NAME, values, KEY_BUTTON + "=?", new String[] {btn});
		}


	}

	public String fetchRemote(String btn) {
		
		try {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cur = db.query(DATABASE_NAME, new String[] { KEY_CODE },
					KEY_BUTTON + "=?", new String[] { btn }, null, null,
					null);
			if (cur != null)
				cur.moveToFirst();
			return cur.getString(0);
			
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	
	}

}
