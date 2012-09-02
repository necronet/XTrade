package com.xtrade.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.xtrade.android.service.ServiceHelper;
import com.xtrade.android.util.AccountUtils;
import com.xtrade.android.util.Settings;

public class BaseActivity extends SherlockFragmentActivity {

	protected boolean upLevel=true;
	protected ActionBar actionBar;
	protected ServiceHelper serviceHelper;

	@Override
	public void onCreate(Bundle savedIntanceState) {
		super.onCreate(savedIntanceState);
		
		if (!AccountUtils.isAuthenticated(this) && !(this instanceof LoginActivity)) {
            AccountUtils.startAuthenticationFlow(this, getIntent());
            finish();
        }
		
		// Getting the action bar
		actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayUseLogoEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(upLevel);
			actionBar.setIcon(getResources().getDrawable(R.drawable.ic_logo_text));
		}
		
		serviceHelper = ServiceHelper.getInstance(this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if(upLevel)
				finish();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Parent method for activities call through actions on the application
	 * 
	 */
	protected void startActivity(String action) {
		startActivity(new Intent(action));
	}

	/**
	 * SharedPreferences object
	 * 
	 * */
	protected SharedPreferences getAppSharedPreference() {
		SharedPreferences xTradeSettings = getSharedPreferences(Settings.SHARED_PREFERENCES, MODE_PRIVATE);
		return xTradeSettings;
	}

}
