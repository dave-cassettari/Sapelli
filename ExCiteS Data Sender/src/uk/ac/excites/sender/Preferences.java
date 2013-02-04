package uk.ac.excites.sender;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * This class contains various utilities methods
 * 
 * @author Michalis Vitos
 * 
 */
public class Preferences extends PreferenceActivity
{
	
	public static final String PREFERENCES = "backgroundSharedPreferces";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.background_preferences);
		PreferenceManager.setDefaultValues(Preferences.this, R.xml.background_preferences, false);
	}
	
	/**
	 * Get the Phone Number of the centre phone that works as a rely
	 * @param mContext
	 * @return
	 */
	public static String getCenterPhoneNumber(Context mContext){
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		return mSharedPreferences.getString("centerPhoneNumber", "");
	}
	
	/**
	 * Get the number of minutes that the service is checking for connectivity
	 * @param mContext
	 * @return
	 */
	public static int getTimeSchedule(Context mContext){
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		return Integer.parseInt(mSharedPreferences.getString("timeSchedule", "1"));
	}
	
	/**
	 * Get the number of seconds that the service will wait for connectivity.
	 * @param mContext
	 * @return
	 */
	public static int getMaxAttemps(Context mContext){
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		return Integer.parseInt(mSharedPreferences.getString("maxAttemps", "1"));
	}
}