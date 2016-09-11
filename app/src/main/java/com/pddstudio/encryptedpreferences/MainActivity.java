package com.pddstudio.encryptedpreferences;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.pddstudio.preferences.encrypted.EncryptedPreferences;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EncryptedPreferences.OnSharedPreferenceChangeListener {

	private static final String TEST_KEY_VALUE_STRING  = "testValueString";
	private static final String TEST_KEY_VALUE_FLOAT   = "testValueFloat";
	private static final String TEST_KEY_VALUE_LONG    = "testValueLong";
	private static final String TEST_KEY_VALUE_BOOLEAN = "testValueBoolean";

	private EncryptedPreferences encryptedPreferences;
	
	private Button addRandomValueButton;
	private Button registerListenerButton;
	private Button unregisterListenerButton;
	private Button importMockPreferencesButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//register demo buttons
		addRandomValueButton = (Button) findViewById(R.id.add_random_value_button);
		addRandomValueButton.setOnClickListener(this);
		registerListenerButton = (Button) findViewById(R.id.register_listener_button);
		registerListenerButton.setOnClickListener(this);
		unregisterListenerButton = (Button) findViewById(R.id.unregister_listener_button);
		unregisterListenerButton.setOnClickListener(this);
		importMockPreferencesButton = (Button) findViewById(R.id.import_preferences_button);
		importMockPreferencesButton.setOnClickListener(this);

		//create EncryptedPreferences instance
		encryptedPreferences = new EncryptedPreferences.Builder(this).withEncryptionPassword("example").withOnSharedPreferenceChangeListener(this).build();

		//save the preferences
		saveValues();
		printSeparatorLine();
		//validate the preferences
		validateValues();
		printSeparatorLine();
		//print the values
		printValues();
		printSeparatorLine();
		utilsExample();
		printSeparatorLine();
	}

	@Override
	protected void onDestroy() {
		encryptedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	private void printSeparatorLine() {
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.add_random_value_button:
				MockUtils.addRandomValue(encryptedPreferences);
				break;
			case R.id.register_listener_button:
				encryptedPreferences.registerOnSharedPreferenceChangeListener(this);
				Log.d("MainActivity", "registered listener!");
				break;
			case R.id.unregister_listener_button:
				encryptedPreferences.unregisterOnSharedPreferenceChangeListener(this);
				Log.d("MainActivity", "unregistered listener!");
				break;
			case R.id.import_preferences_button:
				encryptedPreferences.importSharedPreferences(MockUtils.createMockSharedPreferences(this), true, true);
				Log.d("MainActivity", "created and imported mocked preferences!");
				break;
		}
	}

	@Override
	public void onSharedPreferenceChanged(EncryptedPreferences encryptedPreferences, String key) {
		Log.d("MainActivity", "onSharedPreferenceChanged() => key: " + key);
	}

}
