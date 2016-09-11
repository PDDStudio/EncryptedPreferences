package com.pddstudio.encryptedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.pddstudio.preferences.encrypted.EncryptedPreferences;

import java.util.Random;

/**
 * Created by pddstudio on 11/09/16.
 */
public class MockUtils {

	private static Random random = new Random();

	public static void addRandomValue(EncryptedPreferences encryptedPreferences) {
		int identifier = random.nextInt(5);
		String key = getRandomKey(identifier);
		EncryptedPreferences.EncryptedEditor editor = encryptedPreferences.edit();
		switch (identifier) {
			case 0:
				editor.putBoolean(key, random.nextBoolean());
				break;
			case 1:
				editor.putFloat(key, random.nextFloat());
				break;
			case 2:
				editor.putInt(key, random.nextInt());
				break;
			case 3:
				editor.putLong(key, random.nextLong());
				break;
			case 4:
				editor.putString(key, key);
				break;
		}
		editor.apply();
	}

	public static void addRandomValue(SharedPreferences preferences) {
		int identifier = random.nextInt(5);
		String key = getRandomKey(identifier);
		SharedPreferences.Editor editor = preferences.edit();
		switch (identifier) {
			case 0:
				editor.putBoolean(key, random.nextBoolean());
				break;
			case 1:
				editor.putFloat(key, random.nextFloat());
				break;
			case 2:
				editor.putInt(key, random.nextInt());
				break;
			case 3:
				editor.putLong(key, random.nextLong());
				break;
			case 4:
				editor.putString(key, key);
				break;
		}
		editor.apply();
	}

	public static SharedPreferences createMockSharedPreferences(Context context) {
		//create a dummy preference
		String prefName = getRandomPreferenceName(context);
		SharedPreferences preferences = context.getSharedPreferences(prefName, 0);
		//fill it with some random values
		for(int i = 0; i < 20; i++) {
			addRandomValue(preferences);
		}
		Log.d("MainActivity", "Created mock preferences! Name: " + prefName + " Item count: " + preferences.getAll().size());
		return preferences;
	}

	public static String getRandomPreferenceName(Context context) {
		return context.getPackageName() + "_random_preference_" + System.currentTimeMillis();
	}

	public static String getRandomKey(int identifier) {
		String base = "RANDOM_STRING_BASE_";
		return base + identifier + "_" + System.currentTimeMillis();
	}
}
