package com.pddstudio.encryptedpreferences;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.pddstudio.preferences.encrypted.EncryptedPreferences;

public class MainActivity extends AppCompatActivity {

	private static final String TEST_KEY_VALUE_STRING  = "testValueString";
	private static final String TEST_KEY_VALUE_FLOAT   = "testValueFloat";
	private static final String TEST_KEY_VALUE_LONG    = "testValueLong";
	private static final String TEST_KEY_VALUE_BOOLEAN = "testValueBoolean";

	private EncryptedPreferences encryptedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		encryptedPreferences = EncryptedPreferences.getInstance(this);
		//save the preferences
		saveValues();
		printSeperatorLine();
		//validate the preferences
		validateValues();
		printSeperatorLine();
		//print the values
		printValues();
		printSeperatorLine();
		utilsExample();
		printSeperatorLine();
	}

	private void printSeperatorLine() {
		Log.d("MainActivity", "= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
	}

	private void saveValues() {
		encryptedPreferences.edit()
							.putString(TEST_KEY_VALUE_STRING, "testString")
							.putFloat(TEST_KEY_VALUE_FLOAT, 1.5f)
							.putLong(TEST_KEY_VALUE_LONG, 10L)
							.putBoolean(TEST_KEY_VALUE_BOOLEAN, false)
							.apply();
		Log.d("MainActivity", "done saving preferences!");
	}

	private void validateValues() {
		Log.d("MainActivity", TEST_KEY_VALUE_STRING + " : " + encryptedPreferences.contains(TEST_KEY_VALUE_STRING));
		Log.d("MainActivity", TEST_KEY_VALUE_FLOAT + " : " + encryptedPreferences.contains(TEST_KEY_VALUE_FLOAT));
		Log.d("MainActivity", TEST_KEY_VALUE_LONG + " : " + encryptedPreferences.contains(TEST_KEY_VALUE_LONG));
		Log.d("MainActivity", TEST_KEY_VALUE_BOOLEAN + " : " + encryptedPreferences.contains(TEST_KEY_VALUE_BOOLEAN));
	}

	private void printValues() {
		Log.d("MainActivity", TEST_KEY_VALUE_STRING + " => " + encryptedPreferences.getString(TEST_KEY_VALUE_STRING, TEST_KEY_VALUE_STRING));
		Log.d("MainActivity", TEST_KEY_VALUE_FLOAT + " => " + encryptedPreferences.getFloat(TEST_KEY_VALUE_FLOAT, 0));
		Log.d("MainActivity", TEST_KEY_VALUE_LONG + " => " + encryptedPreferences.getLong(TEST_KEY_VALUE_LONG, 0));
		Log.d("MainActivity", TEST_KEY_VALUE_BOOLEAN + " => " + encryptedPreferences.getBoolean(TEST_KEY_VALUE_BOOLEAN, true));
	}

	private void utilsExample() {
		//get the encrypted value for an api key while debugging, so we don't have to save the original api key as plain text in production.
		String encryptedApiKey = encryptedPreferences.getUtils().encryptStringValue("SOME_API_KEY_HERE");
		Log.d("MainActivity", "encryptedApiKey => " + encryptedApiKey);
		//in production we simply use the utility method with the encrypted value which we got from debugging.
		String decryptedApiKey = encryptedPreferences.getUtils().decryptStringValue(encryptedApiKey);
		Log.d("MainActivity", "decryptedApiKey => " + decryptedApiKey);
	}

}
