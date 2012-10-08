package com.xtrade.android.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.xtrade.android.R;
import com.xtrade.android.provider.DatabaseContract.TraderColumns;
import com.xtrade.android.provider.DatabaseContract.TraderEntity;
import com.xtrade.android.util.ActionConstant;
import com.xtrade.android.util.EventConstant;

public class TraderListFragment extends SherlockFragment implements EventConstant, LoaderManager.LoaderCallbacks<Cursor> {

	private CursorAdapter adapter;
	private long[] ids;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View fragmentView = inflater.inflate(R.layout.trader_tab_list, container, false);

		adapter = new TraderAdapter(getActivity());

		ListView listView = (ListView) fragmentView.findViewById(R.id.lvwTrader);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setAdapter(adapter);
		listView.setItemsCanFocus(true);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				Intent intent = new Intent(ActionConstant.TRADER_DETAIL);

				intent.putExtra(TraderColumns.TRADER_ID, ids);
				intent.putExtra("position", position);
				startActivity(intent);
			}
		});

		return fragmentView;
	}

	@Override
	public void onResume() {
		super.onResume();
		// Force start background query to load sessions
		getLoaderManager().restartLoader(0, null, this);
	}

	// LoaderCallbacks interface
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle data) {
		Loader<Cursor> loader = new CursorLoader(getActivity(), TraderEntity.CONTENT_URI, null, null, null, TraderEntity.DEFAULT_SORT);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (getActivity() == null)
			return;

		//load all the ids to array for use the PageAdapter in detail
		ids=new long[cursor.getCount()];
		int index=0;
		while(cursor.moveToNext()){
			ids[index++] = cursor.getLong(cursor.getColumnIndex(TraderEntity._ID));
		}

		adapter.changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	public class TraderAdapter extends CursorAdapter implements EventConstant {

		public TraderAdapter(Context context) {
			super(context, null, false);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = getActivity().getLayoutInflater().inflate(R.layout.trader_tab_list_item, parent, false);
			return view;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = super.getView(position, convertView, parent);

//			if (position % 2 == 0)
//				convertView.setBackgroundResource(R.drawable.list_bg_odd);
//			else
//				convertView.setBackgroundResource(R.drawable.list_bg);

			return convertView;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final String traderId = cursor.getString(cursor.getColumnIndex(TraderEntity._ID));
			if (traderId == null) {
				return;
			}

			TextView textViewTraderName = (TextView) view.findViewById(R.id.tvwTraderName);
			textViewTraderName.setText(cursor.getString(cursor.getColumnIndex(TraderEntity.NAME)));

			TextView tvwTraderAddress = (TextView) view.findViewById(R.id.tvwTraderAddress);
			tvwTraderAddress.setText(cursor.getString(cursor.getColumnIndex(TraderEntity.ADDRESS)));
			
			final ImageButton chbFavorite = (ImageButton) view.findViewById(R.id.chbFavorite);
			chbFavorite.setClickable(true);
			chbFavorite.setFocusable(true);

			final boolean isFavorite = cursor.getInt(cursor.getColumnIndex(TraderEntity.ISFAVORITE)) == 1;
			if (isFavorite)
				chbFavorite.setImageResource(R.drawable.ic_star_on);
			else
				chbFavorite.setImageResource(R.drawable.ic_star_off);

			chbFavorite.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ContentValues contentValues = new ContentValues();
					contentValues.put(TraderColumns.ISFAVORITE, isFavorite ? 0 : 1);
					boolean updated = getActivity().getContentResolver().update(TraderEntity.buildUri(traderId), contentValues, null, null) > 0;
					if (updated)
						getLoaderManager().restartLoader(0, null, TraderListFragment.this);
				}
			});
		}
	}

}