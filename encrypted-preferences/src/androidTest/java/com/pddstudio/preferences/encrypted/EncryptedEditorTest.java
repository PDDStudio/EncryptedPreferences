package com.pddstudio.preferences.encrypted;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Created by pddstudio on 13/09/16.
 */
@RunWith(AndroidJUnit4.class)
public class EncryptedEditorTest {

	Context context;
	EncryptedPreferences encryptedPreferences;

	@Before
	public void setup() {
		context = InstrumentationRegistry.getContext();
		encryptedPreferences = new EncryptedPreferences.Builder(context).withEncryptionPassword("test").withPreferenceName(getClass().getSimpleName()).build();
		encryptedPreferences.edit().clear().apply();
	}

	@Test
	public void testPutInt() {
		encryptedPreferences.edit().putInt("INTEGER", 99).apply();
		assertEquals(encryptedPreferences.getInt("INTEGER",-1), 99);
	}

	@Test
	public void testPutFloat() {
		encryptedPreferences.edit().putFloat("FLOAT", 15.2F).apply();
		assertEquals(encryptedPreferences.getFloat("FLOAT", 1F), 15.2F, 1e-8);
	}

	@Test
	public void testPutLong() {
		encryptedPreferences.edit().putLong("LONG", 76L).apply();
		assertEquals(encryptedPreferences.getLong("LONG", 0L), 76L);
	}

	@Test
	public void testPutString() {
		encryptedPreferences.edit().putString("STRING", getClass().getName()).apply();
		assertEquals(encryptedPreferences.getString("STRING", null), getClass().getName());
	}

	@Test
	public void testPutBoolean() {
		encryptedPreferences.edit().putBoolean("BOOLEAN", false).apply();
		assertFalse(encryptedPreferences.getBoolean("BOOLEAN", true));
	}

	@Test
	public void testClearValues() {
		encryptedPreferences.edit().clear().apply();
		assertTrue(encryptedPreferences.getAllKeys().isEmpty());
	}

	@Test
	public void testRemoveValue() {
		String key = "TEST_REMOVE_VALUE";
		encryptedPreferences.edit().putString(key, key).apply();
		assertTrue(encryptedPreferences.contains(key));
		encryptedPreferences.edit().remove(key).apply();
		assertFalse(encryptedPreferences.contains(key));
	}

}
