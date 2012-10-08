package com.xtrade.android;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.view.ViewPager;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.xtrade.android.fragment.SectionsPagerAdapter;
import com.xtrade.android.fragment.TraderListFragment;
import com.xtrade.android.fragment.TraderTodayFragment;
import com.xtrade.android.util.ActionConstant;
import com.xtrade.android.util.EventConstant;

public class TraderActivity extends BaseActivity implements ActionBar.TabListener, EventConstant {

	private TraderBroadcastReceiver receiver;
	private ViewPager viewPager;
	private SectionsPagerAdapter sectionsPagerAdapter;
	private final static int TRADER_LIST_INDEX=0;
	
	public void onCreate(Bundle savedIntanceState) {
		upLevel = false;
		super.onCreate(savedIntanceState);
		setContentView(R.layout.trader);
		
		Intent requestData = new Intent(ActionConstant.REQUEST_DATA);
		serviceHelper.invokeService(requestData);
		receiver = new TraderBroadcastReceiver();
		
		// Getting the current action bar
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		 sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),
				 new  Class[]{TraderListFragment.class,TraderTodayFragment.class},
				 new String[]{getString(R.string.traders),getString(R.string.today)}, this);	       

	    // Set up the ViewPager with the sections adapter.
	    viewPager = (ViewPager) findViewById(R.id.pager);
	    viewPager.setAdapter(sectionsPagerAdapter);
	    
	    viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
		
	    for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(sectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_CANCELED)
			return;
		
		if (resultCode == RESULT_OK && (requestCode == TRADER_CREATE_REQUEST_CODE || requestCode == TRADER_UPDATE_REQUEST_CODE)) {
			ListView listView = (ListView) findViewById(R.id.lvwTrader);
			if (listView != null)
				this.getContentResolver().query(com.xtrade.android.provider.DatabaseContract.TraderEntity.CONTENT_URI, null, null, null, null);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.trader_tab_list_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {

		case R.id.mniSettings:
			startActivity(new Intent(ActionConstant.SETTINGS));
			break;
		case R.id.mniAbout:
			startActivity(new Intent(ActionConstant.ABOUT));
			break;
		}
		
		return super.onOptionsItemSelected(menuItem);
	}

	public void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ActionConstant.REQUEST_DATA);
		registerReceiver(receiver, filter);
		super.onResume();
	}
	
	public void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}
	
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
	
	public class TraderBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			LoaderCallbacks<Cursor> callbacks=(LoaderCallbacks<Cursor>) sectionsPagerAdapter.getItem(TRADER_LIST_INDEX);
			
			getSupportLoaderManager().restartLoader(0, null, callbacks);	
		}
		
	}
	
}
