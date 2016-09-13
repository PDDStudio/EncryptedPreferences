package com.pddstudio.preferences.encrypted;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Created by pddstudio on 13/09/16.
 */
@RunWith(AndroidJUnit4.class)
public class UtilsTest {

	private static final String VALUE_ENCRYPTED = "2x0P1XxQx0P1XxmRkVOU5DIM1XLHI77PM7eIPXPn4wHzvdmTeIzD8x0P3Xx";
	private static final String VALUE_UNENCRYPTED = "This is an unencrypted message!";

	Context context;
	EncryptedPreferences encryptedPreferences;

	@Before
	public void setup() {
		context = InstrumentationRegistry.getContext();
		encryptedPreferences = new EncryptedPreferences.Builder(context).withEncryptionPassword("test").withPreferenceName(getClass().getSimpleName()).build();
	}

	@Test
	public void testStringEncryption() {
		String encrypted = encryptedPreferences.getUtils().encryptStringValue(VALUE_UNENCRYPTED);
		assertEquals(VALUE_ENCRYPTED, encrypted);
	}

	@Test
	public void testStringDecryption() {
		String decrypted = encryptedPreferences.getUtils().decryptStringValue(VALUE_ENCRYPTED);
		assertEquals(VALUE_UNENCRYPTED, decrypted);
	}

}
