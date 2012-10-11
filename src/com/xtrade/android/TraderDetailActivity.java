package com.xtrade.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.xtrade.android.fragment.TraderAboutFragment;
import com.xtrade.android.provider.DatabaseContract.ContactColumns;
import com.xtrade.android.provider.DatabaseContract.ContactEntity;
import com.xtrade.android.provider.DatabaseContract.TraderColumns;
import com.xtrade.android.util.ActionConstant;
import com.xtrade.android.util.Debug;
import com.xtrade.android.util.EventConstant;

public class TraderDetailActivity extends BaseActivity implements EventConstant {

	TraderPagerAdapter mtraderPagerAdapter;
	ViewPager mViewPager;
	private static int currentPage;
	static long[] tradersId;

	@Override
	public void onCreate(Bundle savedIntanceState) {

		super.onCreate(savedIntanceState);
		setContentView(R.layout.trader_detail);
		tradersId = getIntent().getLongArrayExtra(TraderColumns.TRADER_ID);
		currentPage= getIntent().getIntExtra("position", -1);

		mtraderPagerAdapter = new TraderPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager, attaching the adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);

		mViewPager.setAdapter(mtraderPagerAdapter);
		mViewPager.setCurrentItem(currentPage);
		mViewPager.setOnPageChangeListener(new PageListener());
	}

	public static class TraderPagerAdapter extends FragmentStatePagerAdapter {

		private TraderAboutFragment[] fragments;

		public TraderPagerAdapter(FragmentManager fm) {
			super(fm);
			fragments = new TraderAboutFragment[tradersId.length];
		}

		@Override
		public Fragment getItem(int i) {
			Debug.info("Traders id is " + tradersId[i] + " for position " + i);
			fragments[i] = TraderAboutFragment.newInstance(tradersId[i]);

			return fragments[i];
		}

		@Override
		public int getCount() {
			return tradersId.length;

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.trader_tab_contact_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem _menuItem) {
		switch (_menuItem.getItemId()) {
		case R.id.mniNewContact:
			Intent intent = new Intent(ActionConstant.CONTACT_CREATE_UPDATE);
			intent.putExtra("ACTION_TYPE", CONTACT_CREATE_REQUEST_CODE);
			intent.putExtra(TraderColumns.TRADER_ID, tradersId[currentPage]);
			startActivityForResult(intent, CONTACT_CREATE_REQUEST_CODE);
			return true;
		default:
			return super.onOptionsItemSelected(_menuItem);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && (requestCode == CONTACT_CREATE_REQUEST_CODE || requestCode == CONTACT_UPDATE_REQUEST_CODE)) {
			ListView lvwContact = (ListView) findViewById(R.id.lvwContact);
			if (lvwContact != null){
				getContentResolver().query(ContactEntity.CONTENT_URI, null, ContactColumns.TRADER_ID + " = '" + tradersId[currentPage] + "'", null, null);
				
			}
		}
	}

	private static class PageListener extends SimpleOnPageChangeListener {
		public void onPageSelected(int position) {
			currentPage = position;
		}
	}

}