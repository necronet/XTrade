package com.xtrade.android.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.Gson;
import com.xtrade.android.http.HttpCaller;
import com.xtrade.android.http.HttpCallerFactory;
import com.xtrade.android.http.RestOption;
import com.xtrade.android.http.RestOption.Method;
import com.xtrade.android.http.RestOption.Parameter;
import com.xtrade.android.object.Contact;
import com.xtrade.android.object.User;
import com.xtrade.android.provider.DatabaseContract;
import com.xtrade.android.provider.DatabaseContract.XTradeBaseColumns;
import com.xtrade.android.util.ActionConstant;
import com.xtrade.android.util.Debug;
import com.xtrade.android.util.LoginParameter;
import com.xtrade.android.util.Settings;

public class XTradeBaseService extends IntentService {

	public XTradeBaseService() {
		super(XTradeBaseService.class.getSimpleName());
	}

	// this method run on a different thread
	@Override
	protected void onHandleIntent(Intent intent) {
		// depending on the action we execute an action currently
		// there will be few action so we can handle it here
		HttpCaller httpCaller = HttpCallerFactory.getInstance().createCaller(this);
		if (intent.getAction().equals(ActionConstant.LOGIN)) {
			

			String username = intent.getStringExtra(LoginParameter.USERNAME);
			String password = intent.getStringExtra(LoginParameter.PASSWORD);

			User user = new User(username, password);
			// TODO: create someone that will create parameter for us
			Map<Parameter, String> parameters = new HashMap<Parameter, String>();
			Gson gson = new Gson();
			parameters.put(Parameter.BODY, gson.toJson(user));

			
			try {

				boolean result = httpCaller.call(new URL(Settings.getServerURL() + "login/"), Method.POST, parameters);
				if (result) {

					if (!StringUtils.isEmpty(httpCaller.getResult())) {
						SharedPreferences xTradeSettings = getSharedPreferences(Settings.SHARED_PREFERENCES, MODE_PRIVATE);
						Editor editor = xTradeSettings.edit();
						editor.putBoolean(Settings.LOGGED_PREF, true);
						editor.putString(Settings.COOKIE_PREF, httpCaller.getResult());
						intent.putExtra(LoginParameter.SUCCESS, true);
						editor.commit();
					}

				}
			} catch (MalformedURLException murle) {
				murle.printStackTrace();
			}

			
			
		} else if (intent.getAction().equals(ActionConstant.REQUEST_DATA)) {
			Debug.info("Requesting Data in XTrdeBaseService#onHandleIntent");
			try {
				boolean result = httpCaller.call(new URL(Settings.getServerURL() + "traders/"), Method.GET,null);
				if (result) {
					
					ProcessorBase processor = ProcessorFactory.getProcessor(intent.getAction(), this);
					
					processor.process(httpCaller.getResult());
				}
			} catch (MalformedURLException murle) {
				murle.printStackTrace();
			}
		}else if(intent.getAction().equals(ActionConstant.SYNC_RECORD)){
			Uri uri= (Uri)intent.getParcelableExtra("uri");
			//get all dirty records FLAG_STATE=1
			//TODO: make this method more generic currently will work only for contacts
			Cursor cursor=getContentResolver().query(uri, null, XTradeBaseColumns.FLAG_STATE+"=?", new String[]{"1"}, null);
			Map<RestOption.Parameter,String> parameters=new HashMap<RestOption.Parameter,String>();
			while(cursor.moveToNext()){
				Gson gson = new Gson();
				Contact contact = new Contact();
				contact.traderId=cursor.getString(cursor.getColumnIndex(DatabaseContract.ContactColumns.TRADER_ID));
				contact.email=cursor.getString(cursor.getColumnIndex(DatabaseContract.ContactColumns.EMAIL));
				contact.lastName=cursor.getString(cursor.getColumnIndex(DatabaseContract.ContactColumns.LAST_NAME));
				contact.phone=cursor.getString(cursor.getColumnIndex(DatabaseContract.ContactColumns.PHONE));
				//contact.role=cursor.getString(cursor.getColumnIndex(DatabaseContract.ContactColumns.TYPE));
				Debug.info("JSON body "+gson.toJson(contact));
				parameters.put(Parameter.BODY, gson.toJson(contact));
				//update or insert records
				try {
					boolean result = httpCaller.call(new URL(Settings.getServerURL() + "contact/"), Method.POST,parameters);
					Debug.info("Result from call "+result);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			
		}
		
		
		
		sendBroadcast(intent);
	}

}