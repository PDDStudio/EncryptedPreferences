package com.pddstudio.preferences.encrypted;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

/**
 * Created by pddstudio on 11/09/16.
 */
@RunWith(AndroidJUnit4.class)
public class SingletonCreationTest {

	Context context;
	EncryptedPreferences encryptedPreferences;

	@Before
	public void setup() {
		context = InstrumentationRegistry.getContext();
		encryptedPreferences = new EncryptedPreferences.Builder(context).withEncryptionPassword("test").withSaveAsSingleton(true).build();
	}

	@Test
	public void testSingletonInstanceExists() {
		EncryptedPreferences encryptedPreferences = EncryptedPreferences.getSingletonInstance();
		assertNotNull(encryptedPreferences);
	}

	@Test(expected = RuntimeException.class)
	public void testSingletonInstanceException() {
		encryptedPreferences = new EncryptedPreferences.Builder(context).withEncryptionPassword("test").build();
		EncryptedPreferences.getSingletonInstance();
	}

}
