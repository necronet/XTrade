package com.xtrade.android.util;

import com.xtrade.android.LoginActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class AccountUtils {
	
	private static final String PREF_CHOSEN_ACCOUNT = "chosen_account";
	public static final String EXTRA_FINISH_INTENT = "finished_intent";
    
	public static boolean isAuthenticated(final Context context) {
        return !TextUtils.isEmpty(getChosenAccountName(context));
    }
	
	public static String getChosenAccountName(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_CHOSEN_ACCOUNT, null);
    }
	
	public static void startAuthenticationFlow(final Context context, final Intent finishIntent) {
        Intent loginFlowIntent = new Intent(context, LoginActivity.class);
        loginFlowIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        loginFlowIntent.putExtra(EXTRA_FINISH_INTENT, finishIntent);
        context.startActivity(loginFlowIntent);
    }
	
}
