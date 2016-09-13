package com.pddstudio.preferences.encrypted;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Created by pddstudio on 12/09/16.
 */
@RunWith(AndroidJUnit4.class)
public class EncryptedPreferencesTest {

	Context              context;
	EncryptedPreferences encryptedPreferences;
	Random               random;

	@Before
	public void setup() {
		context = InstrumentationRegistry.getContext();
		encryptedPreferences = new EncryptedPreferences.Builder(context).withEncryptionPassword("test").withPreferenceName(getClass().getSimpleName()).build();
		random = new Random();
		addTestValues(true);
	}

	private void addRandomValue(SharedPreferences preferences) {
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

	private String getRandomKey(int identifier) {
		String base = "RANDOM_KEY_BASE_";
		return base + identifier + "_" + System.currentTimeMillis();
	}

	private SharedPreferences createMockPreferences(String prefName, boolean fillWithMockData) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(prefName, 0);
		if (fillWithMockData) {
			for (int i = 0; i < 10; i++) {
				synchronized (sharedPreferences) {
					addRandomValue(sharedPreferences);
				}
			}
		}
		return sharedPreferences;
	}

	private void addTestValues(boolean clearBeforeApplying) {
		if (clearBeforeApplying) {
			encryptedPreferences.edit().clear().apply();
		}
		encryptedPreferences.edit()
							.putFloat("FLOAT", 1.5f)
							.putBoolean("BOOLEAN", true)
							.putString("STRING", "Some Test String")
							.putLong("LONG", 181123L)
							.putInt("INTEGER", 99121)
							.apply();
	}

	@Test
	public void testInstanceExist() {
		assertNotNull(encryptedPreferences);
	}

	@Test
	public void testNonExistingKey() {
		boolean keyExist = encryptedPreferences.getBoolean("SOME_RANDOM_KEY_THAT_DOES_NOT_EXIST", false);
		assertFalse(keyExist);
	}

	@Test
	public void testEditorInstanceExist() {
		assertNotNull(encryptedPreferences.edit());
	}

	@Test
	public void testUtilsInstanceExist() {
		assertNotNull(encryptedPreferences.getUtils());
	}

	@Test
	public void testReturnTypeFloat() {
		assertEquals(encryptedPreferences.getFloat("FLOAT", 10f), 1.5f);
	}

	@Test
	public void testReturnTypeBoolean() {
		assertEquals(encryptedPreferences.getBoolean("BOOLEAN", false), true);
	}

	@Test
	public void testReturnTypeString() {
		assertEquals(encryptedPreferences.getString("STRING", null), "Some Test String");
	}

	@Test
	public void testReturnTypeLong() {
		assertEquals(encryptedPreferences.getLong("LONG", 0L), 181123L);
	}

	@Test
	public void testReturnTypeInteger() {
		assertEquals(encryptedPreferences.getInt("INTEGER", -1), 99121);
	}

	@Test
	public void testSharedPreferencesImport() {
		encryptedPreferences.edit().clear().apply();
		SharedPreferences preferences = createMockPreferences("mock_preferences", true);
		encryptedPreferences.importSharedPreferences(preferences, true);
		assertEquals(encryptedPreferences.getAllKeys().size(), preferences.getAll().size());
	}

}
