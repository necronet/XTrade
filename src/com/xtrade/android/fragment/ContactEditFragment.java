package com.xtrade.android.fragment;

import org.apache.commons.lang.StringUtils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.xtrade.android.PictureDialog;
import com.xtrade.android.R;
import com.xtrade.android.provider.DatabaseContract;
import com.xtrade.android.provider.DatabaseContract.ContactColumns;
import com.xtrade.android.provider.DatabaseContract.ContactEntity;
import com.xtrade.android.provider.DatabaseContract.ContactTypeColumns;
import com.xtrade.android.provider.DatabaseContract.TraderColumns;
import com.xtrade.android.util.Debug;
import com.xtrade.android.util.EventConstant;
import com.xtrade.android.util.Settings;

public class ContactEditFragment extends SherlockFragment implements EventConstant, LoaderManager.LoaderCallbacks<Cursor> {

	private Intent intent;

	private SimpleCursorAdapter adapter;
	private Cursor cursor;
	private long contactId;
	private ImageButton ibtContactPhoto;
	private EditText etxFirstContactName;
	private EditText etxLastContactName;
	private Spinner spnContactType;
	private EditText etxContactEmail;
	private EditText etxContactPhone;

	@Override
	public View onCreateView(LayoutInflater layoutInflater,ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(layoutInflater, container, savedInstanceState);
		View view = layoutInflater.inflate(R.layout.contact_edit_fragment, container, false);

		cursor = getActivity().getContentResolver().query(DatabaseContract.ContactTypeEntity.CONTENT_URI, 
				new String[] {BaseColumns._ID, ContactTypeColumns.CONTACT_TYPE_ID, ContactTypeColumns.NAME},
				null, 
				null,
				null);

		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, cursor, new String[] {ContactTypeColumns.NAME}, new int[] {android.R.id.text1}, 0);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spnContactType = (Spinner) view.findViewById(R.id.spnContactType);
		spnContactType.setAdapter(adapter);

		ibtContactPhoto = (ImageButton) view.findViewById(R.id.ibtContactPhoto);
		ibtContactPhoto.setClickable(true);
		ibtContactPhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				Fragment f = getFragmentManager().findFragmentByTag("picture_dialog");
				if (f != null)
					ft.remove(f);
				ft.addToBackStack(null);

				DialogFragment dialogFragment = PictureDialog.newInstance(ContactEditFragment.this);
				dialogFragment.show(ft, "picture_dialog");
			}
		});

		etxFirstContactName = (EditText) view.findViewById(R.id.etxFirstContactName);
		etxLastContactName = (EditText) view.findViewById(R.id.etxLastContactName);
		etxContactEmail = (EditText) view.findViewById(R.id.etxContactEmail);
		etxContactPhone = (EditText) view.findViewById(R.id.etxContactPhone);

		// We get the intent which calls the activity
		intent = getActivity().getIntent();

		// We get the ACTION_TYPE extra which tells us what operation we should perform (Save or Update)
		int extra = intent.getIntExtra("ACTION_TYPE", -1);

		// Setting default values while we're on developer mode
		if (Settings.DEBUG && extra == CONTACT_CREATE_REQUEST_CODE) {
			etxFirstContactName.setText("Jos\u00E9 Luis");
			etxLastContactName.setText("Ayerdis Espinoza");
			
			etxContactEmail.setText("joserayerdis@gmail.com");
			etxContactPhone.setText("86727076");
		}

		if (extra == CONTACT_UPDATE_REQUEST_CODE) 
			contactId = intent.getLongExtra(ContactColumns.CONTACT_ID, -1);

		//this view should handle its menus
		setHasOptionsMenu(true);
		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == FragmentActivity.RESULT_OK)
			if (requestCode == CONTACT_PHOTO_GALLERY_REQUEST)
				ibtContactPhoto.setImageURI(data.getData());
			else if (requestCode == CONTACT_PHOTO_CAMERA_REQUEST)
				ibtContactPhoto.setImageBitmap((Bitmap) data.getExtras().get("data"));
	}

	public void onResume() {
		super.onResume();
		getActivity().getSupportLoaderManager().restartLoader(0, null, this);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		menuInflater.inflate(R.menu.contact_menu, menu);
	}

	public boolean onOptionsItemSelected(MenuItem _menuItem) {
		switch (_menuItem.getItemId()) {
		case R.id.mniSaveContact:
			// We get the ACTION_TYPE extra which tells us what operation we should perform (Save or Update)
			int extra = intent.getIntExtra("ACTION_TYPE", -1);

			CursorWrapper cursorWrapper = (CursorWrapper) spnContactType.getSelectedItem();

			String contactFirstName = etxFirstContactName.getText().toString();
			String contactLastName = etxLastContactName.getText().toString();
			String contactType = cursorWrapper.getString(cursorWrapper.getColumnIndexOrThrow(ContactTypeColumns.NAME));
			String contactEmail = etxContactEmail.getText().toString();
			String contactPhone = etxContactPhone.getText().toString();

			cursorWrapper.close();

			// Evaluating if the EditTexts' content is empty or not
			if (!StringUtils.isEmpty(contactFirstName) && !StringUtils.isEmpty(contactType) && !StringUtils.isEmpty(contactEmail) && !StringUtils.isEmpty(contactPhone)) {
				// We get the trader id in which the contacts will belong
				long traderId = intent.getLongExtra(TraderColumns.TRADER_ID, -1);

				ContentValues contentValues = new ContentValues();
				contentValues.put(ContactColumns.FIRST_NAME, contactFirstName);
				contentValues.put(ContactColumns.LAST_NAME, contactLastName);
				contentValues.put(ContactColumns.TYPE, contactType);
				contentValues.put(ContactColumns.EMAIL, contactEmail);
				contentValues.put(ContactColumns.PHONE, contactPhone);
				contentValues.put(ContactColumns.TRADER_ID, traderId);

				Uri contactUri = null;

				// We build a result variable which will be set on default value for canceled
				int result = Activity.RESULT_CANCELED;

				if (extra == CONTACT_CREATE_REQUEST_CODE) {
					contactUri = getActivity().getContentResolver().insert(DatabaseContract.ContactEntity.CONTENT_URI, contentValues);
					result = contactUri == null ? Activity.RESULT_CANCELED : Activity.RESULT_OK;
				} else if (extra == CONTACT_UPDATE_REQUEST_CODE) {
					contactUri = DatabaseContract.ContactEntity.buildUri(contactId);
					result = getActivity().getContentResolver().update(contactUri, contentValues, null, null) == 0 ? Activity.RESULT_CANCELED : Activity.RESULT_OK;
				}

				getActivity().setResult(result);
				getActivity().finish();
			}

			return true;
		default:
			return super.onOptionsItemSelected(_menuItem);
		}
	}

	private int getCursorAdapterPosition(String _contactType) {
		cursor.moveToFirst();

		for (int i = 0; i < cursor.getCount() - 1; i++) {
			cursor.moveToNext();
			String contactType = cursor.getString(cursor.getColumnIndexOrThrow(ContactColumns.TYPE));
			if (contactType.equals(_contactType))
				return i + 1;
			else
				return 0;
		}

		return -1;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Loader<Cursor> loader = new CursorLoader(getActivity(), ContactEntity.buildUri(contactId), null, null, null, ContactEntity.DEFAULT_SORT);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor.moveToNext()) {
			etxFirstContactName.setText(cursor.getString(cursor.getColumnIndexOrThrow(ContactColumns.FIRST_NAME)));
			etxLastContactName.setText(cursor.getString(cursor.getColumnIndexOrThrow(ContactColumns.LAST_NAME)));
			spnContactType.setSelection(getCursorAdapterPosition(cursor.getString(cursor.getColumnIndexOrThrow(ContactColumns.TYPE))));
			etxContactEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(ContactColumns.EMAIL)));
			etxContactPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow(ContactColumns.PHONE)));
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) { }

}