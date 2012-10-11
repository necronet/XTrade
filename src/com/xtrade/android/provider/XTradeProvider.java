package com.xtrade.android.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import com.xtrade.android.provider.DatabaseContract.ContactEntity;
import com.xtrade.android.provider.DatabaseContract.ContactTypeEntity;
import com.xtrade.android.provider.DatabaseContract.TraderEntity;
import com.xtrade.android.service.ServiceHelper;
import com.xtrade.android.util.ActionConstant;

public class XTradeProvider extends ContentProvider {

	public static final int TRADER = 1000;
	public static final int TRADER_ID = 1001;
	public static final int CONTACT = 1002;
	public static final int CONTACT_ID = 1003;
	public static final int CONTACT_TYPE = 1004;
	public static final int CONTACT_TYPE_ID = 1005;

//	private final static int LIMIT_CALLS = 10;

	private DatabaseHelper databaseHelper;

	private static final UriMatcher uriMatcher = buildMatcher();

	public static UriMatcher buildMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String CONTENT_AUTHORITY = DatabaseContract.CONTENT_AUTHORITY;

		matcher.addURI(CONTENT_AUTHORITY, DatabaseContract.PATH_TRADER, TRADER);
		matcher.addURI(CONTENT_AUTHORITY, DatabaseContract.PATH_TRADER + "/*", TRADER_ID);
		matcher.addURI(CONTENT_AUTHORITY, DatabaseContract.PATH_CONTACT, CONTACT);
		matcher.addURI(CONTENT_AUTHORITY, DatabaseContract.PATH_CONTACT + "/*", CONTACT_ID);
		matcher.addURI(CONTENT_AUTHORITY, DatabaseContract.PATH_CONTACT_TYPE, CONTACT_TYPE);
		matcher.addURI(CONTENT_AUTHORITY, DatabaseContract.PATH_CONTACT_TYPE + "/*", CONTACT_TYPE_ID);

		return matcher;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		String finalWhere = null;
		String table = null;
		switch (uriMatcher.match(uri)) {
		case TRADER:
			table = DatabaseHelper.Tables.TRADER;
			break;
		case TRADER_ID:
			table = DatabaseHelper.Tables.TRADER;
			finalWhere = BaseColumns._ID + " = " + TraderEntity.getId(uri);
			if (selection != null)
				finalWhere = finalWhere + " AND " + selection;
			break;
		case CONTACT:
			table = DatabaseHelper.Tables.CONTACT;
			break;
		case CONTACT_ID:
			table = DatabaseHelper.Tables.CONTACT;
			finalWhere = BaseColumns._ID + " = " + ContactEntity.getId(uri);
			if (selection != null)
				finalWhere = finalWhere + " AND " + selection;
			break;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		int count = db.delete(table, finalWhere, selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	@Override
	public String getType(Uri uri) {
		final int match = uriMatcher.match(uri);

		switch (match) {
		case TRADER:
			return TraderEntity.CONTENT_TYPE;
		case TRADER_ID:
			return TraderEntity.CONTENT_ITEM_TYPE;
		case CONTACT:
			return ContactEntity.CONTENT_TYPE;
		case CONTACT_ID:
			return ContactEntity.CONTENT_ITEM_TYPE;
		case CONTACT_TYPE:
			return ContactTypeEntity.CONTENT_TYPE;
		case CONTACT_TYPE_ID:
			return ContactTypeEntity.CONTENT_ITEM_TYPE;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		final int match = uriMatcher.match(uri);
		long id=-1;
		switch (match) {
		case TRADER:
			id=db.insertOrThrow(DatabaseHelper.Tables.TRADER, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return TraderEntity.buildUri(id);
		case CONTACT:
			id=db.insertOrThrow(DatabaseHelper.Tables.CONTACT, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			
			Intent intent=new Intent(ActionConstant.SYNC_RECORD);
			intent.putExtra("uri", uri);
			ServiceHelper.getInstance(getContext()).invokeService(intent);
			return ContactEntity.buildUri(id);
		case CONTACT_TYPE:
			id=db.insertOrThrow(DatabaseHelper.Tables.CONTACT_TYPE, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			
			return ContactTypeEntity.buildUri(String.valueOf(id));
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		databaseHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final int match = uriMatcher.match(uri);
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();

		SQLiteQueryBuilder builder = buildExpandedSelection(uri, match);

		sortOrder = buildSortOrder(match, sortOrder);
		
		Cursor c = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

		return c;
	}

	private String buildSortOrder(int match, String sortOrder) {
		if (sortOrder == null) {
			if (match == TRADER || match == TRADER_ID)
				sortOrder = TraderEntity.DEFAULT_SORT;
			if (match == CONTACT || match == CONTACT_ID)
				sortOrder = ContactEntity.DEFAULT_SORT;
			if (match == CONTACT_TYPE || match == CONTACT_TYPE_ID)
				sortOrder = ContactTypeEntity.DEFAULT_SORT;
		}

		return sortOrder;
	}

	private SQLiteQueryBuilder buildExpandedSelection(Uri uri, int match) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		String id = null;

		switch (match) {
		case TRADER:
			builder.setTables(DatabaseHelper.Tables.TRADER);
			break;
		case TRADER_ID:
			id = TraderEntity.getId(uri);
			builder.setTables(DatabaseHelper.Tables.TRADER);
			builder.appendWhere(BaseColumns._ID + " = " + id);
			break;
		case CONTACT:
			builder.setTables(DatabaseHelper.Tables.CONTACT);
			break;
		case CONTACT_ID:
			id = ContactEntity.getId(uri);
			builder.setTables(DatabaseHelper.Tables.CONTACT);
			builder.appendWhere(BaseColumns._ID + " = " + id);
			break;
		case CONTACT_TYPE:
			builder.setTables(DatabaseHelper.Tables.CONTACT_TYPE);
			break;
		case CONTACT_TYPE_ID:
			id = ContactTypeEntity.getId(uri);
			builder.setTables(DatabaseHelper.Tables.CONTACT_TYPE);
			builder.appendWhere(BaseColumns._ID + " = " + id);
			break;
		default:
			// If the URI doesn't match any of the known patterns, throw an
			// exception.
			throw new IllegalArgumentException("Unknown URI on XTradeProvider sure it is implemented?" + uri);
		}

		return builder;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		int count;

		String id = null;
		String finalWhere = null;

		int match = uriMatcher.match(uri);
		switch (match) {
		case TRADER:
			count = db.update(DatabaseHelper.Tables.TRADER, values, selection, selectionArgs);
			break;
		case TRADER_ID:
			id = TraderEntity.getId(uri);
			finalWhere = BaseColumns._ID + " = " + id;
			if (selection != null)
				finalWhere = finalWhere + " AND " + selection;

			count = db.update(DatabaseHelper.Tables.TRADER, values, finalWhere, selectionArgs);
			break;
		case CONTACT:
			count = db.update(DatabaseHelper.Tables.CONTACT, values, selection, selectionArgs);
			break;
		case CONTACT_ID:
			id = ContactEntity.getId(uri);
			finalWhere = BaseColumns._ID + " = " + id;
			if (selection != null)
				finalWhere = finalWhere + " AND " + selection;

			count = db.update(DatabaseHelper.Tables.CONTACT, values, finalWhere, selectionArgs);
			break;
		case CONTACT_TYPE:
			count = db.update(DatabaseHelper.Tables.CONTACT_TYPE, values, selection, selectionArgs);
			break;
		case CONTACT_TYPE_ID:
			id = ContactTypeEntity.getId(uri);
			finalWhere = BaseColumns._ID + " = " + id;
			if (selection != null)
				finalWhere = finalWhere + " AND " + selection;

			count = db.update(DatabaseHelper.Tables.CONTACT_TYPE, values, finalWhere, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}

		return count;
	}

}