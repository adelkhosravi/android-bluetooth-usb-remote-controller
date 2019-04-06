package doir.bluetoothremotecontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LayoutDataBase extends SQLiteOpenHelper {

	// models name
	public static final String MODEL_1 = "model1";
	public static final String MODEL_2 = "model2";
	public static final String MODEL_3 = "model3";
	public static final String MODEL_4 = "model4";

	private static final String Database_name = "layoutDatabase";
	private static final int Database_version = 1;

	private static final String KEY_ID = "id";
	private static final String KEY_MODEL = "model";
	private static final String KEY_NAME = "cname";

	//
	private static final String KEY_DATABASE_NAME = "dbname";

	public LayoutDataBase(Context context) {
		super(context, Database_name, null, Database_version);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		String CREATE_LAYOUT_TABLE = "CREATE TABLE " + Database_name + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_MODEL + " TEXT,"
				+ KEY_NAME + " TEXT,"
				+ KEY_DATABASE_NAME + " TEXT" + ")";
		db.execSQL(CREATE_LAYOUT_TABLE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS " + Database_name);

		// Create tables again
		onCreate(db);

	}

	public long addModel(String model, String dbName, String rmName) {
		long id;

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_MODEL, model);
		//
		values.put(KEY_DATABASE_NAME, dbName);
		
		values.put(KEY_NAME, rmName);

		id = db.insert(Database_name, null, values);
		db.close();
		return id;
	}

	public String fetchModel(String id) {

		try {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cur = db.query(Database_name, new String[] { KEY_MODEL },
					KEY_ID + "=?", new String[] { id }, null, null, null);
			if (cur != null)
				cur.moveToFirst();
			return cur.getString(0);

		} catch (Exception e) {

			return null;
		}

	}

	public String fetchdbName(String id) {

		try {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cur = db.query(Database_name,
					new String[] { KEY_DATABASE_NAME }, KEY_ID + "=?",
					new String[] { id }, null, null, null);
			if (cur != null)
				cur.moveToFirst();
			return cur.getString(0);

		} catch (Exception e) {

			return null;
		}

	}
	
	
	public String fetchcName(String id) {

		try {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cur = db.query(Database_name,
					new String[] { KEY_NAME }, KEY_ID + "=?",
					new String[] { id }, null, null, null);
			if (cur != null)
				cur.moveToFirst();
			return cur.getString(0);

		} catch (Exception e) {

			return null;
		}

	}
	
	
	

	public boolean deleteRow(String id) {
		// TODO Auto-generated method stub
		boolean res;
		String sql = "UPDATE " + Database_name
				+ " SET ROWID = (ROWID - 1) WHERE ROWID > " + id;
		
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			res = db.delete(Database_name, KEY_ID + "=" + id, null) > 0;
			db.execSQL(sql);
		//	db.execSQL(sql2);

		} catch (Exception e) {
			// TODO: handle exception
			res = false;
		}
		return res;

	}

	public String fetchLastRow() {
		String selectQ = "SELECT * FROM " + Database_name;
		int cnt;

		try {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cur = db.rawQuery(selectQ, null);
			cnt = cur.getCount();
			cur.close();

			return String.valueOf(cnt);

		} catch (Exception e) {

			return null;
		}

	}

}
