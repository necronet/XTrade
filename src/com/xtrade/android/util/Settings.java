package com.xtrade.android.util;

public class Settings {

	public static final boolean DEBUG = true;
	
	public static final String DEFAULT_USERNAME = "xtrade";
	public static final String DEFAULT_PASSWORD = "xtrade";
	public static final String SHARED_PREFERENCES = "XTrade_Prefs";
	public static final String LOGGED_PREF= "Logged";
	public static final String COOKIE_PREF= "Cookie";
	public static final String TEMPFILE_PATH = "/sdcard/tmp";

	public static final String MAP_KEY = null;// this our key for release mode
	public static final String MAP_KEY_DEBUG = "";// this is the key we should use for debugging

	//public static final String REMOTE_SERVER_DEBUG = "http://172.20.129.228:8085/json/";
	public static final String REMOTE_SERVER_DEBUG = "http://x-trade.appspot.com/json/";
	public static final String REMOTE_SERVER = "http://x-trade.appspot.com/json/";
	
	public static String getServerURL() {
		if (DEBUG)
			return REMOTE_SERVER_DEBUG;
		return REMOTE_SERVER;
	}
	
	
	
}
