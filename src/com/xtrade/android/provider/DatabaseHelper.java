package com.xtrade.android.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.xtrade.android.provider.DatabaseContract.ContactColumns;
import com.xtrade.android.provider.DatabaseContract.ContactTypeColumns;
import com.xtrade.android.provider.DatabaseContract.TraderColumns;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String database = "xtrade";
	private static final int VERSION = 1;

	/**
	 * Represents all the tables name on the database
	 * 
	 * */
	interface Tables {
		String TRADER = "Trader";
		String CONTACT = "Contact";
		String CONTACT_TYPE = "ContactType";
	}

	public DatabaseHelper(Context context) {
		super(context, database, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Client table
		db.execSQL("CREATE TABLE " + Tables.TRADER + 
				" (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				TraderColumns.TRADER_ID + " TEXT, "
				+ TraderColumns.NAME + " TEXT, "
				+ TraderColumns.WEBSITE + " TEXT, "
				+ TraderColumns.ADDRESS + " TEXT, "
				+ TraderColumns.LATITUDE + " NUMBER, "
				+ TraderColumns.LONGITUDE + " NUMBER, "
				+ TraderColumns.ISFAVORITE + " TEXT, "
				+ TraderColumns.FLAG_STATE + " BOOLEAN DEFAULT TRUE, "+
				"UNIQUE (" + TraderColumns.TRADER_ID + ") ON CONFLICT REPLACE )"
				);
		
		// Contact table
		db.execSQL("CREATE TABLE " + Tables.CONTACT + " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + ContactColumns.CONTACT_ID + " TEXT, "
				+ ContactColumns.FIRST_NAME + " TEXT, "
				+ ContactColumns.LAST_NAME + " TEXT, "
				+ ContactColumns.TYPE + " TEXT, "
				+ ContactColumns.EMAIL + " TEXT, "
				+ ContactColumns.PHONE + " TEXT, "
				+ ContactColumns.FLAG_STATE + " BOOL DEFAULT 1, "//by default the record is dirty
				+ ContactColumns.TRADER_ID + " TEXT," +
				"UNIQUE (" + ContactColumns.CONTACT_ID + ") ON CONFLICT REPLACE )");
		
		// Contact type table
		db.execSQL("CREATE TABLE " + Tables.CONTACT_TYPE + " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + ContactTypeColumns.CONTACT_TYPE_ID + " TEXT, "
				+ ContactTypeColumns.NAME + " TEXT)");
		
		// TODO: We must call this data from web
		String sql1 = "INSERT INTO ContactType (ContactTypeId, Name) VALUES ('1', 'Manager')";
		db.execSQL(sql1);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
