package com.pddstudio.preferences.encrypted;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

/**
 * An AES-256 encrypted {@linkplain SharedPreferences} class, to read and write encrypted preferences.
 */
public final class EncryptedPreferences {

	private static final String TAG = EncryptedPreferences.class.getSimpleName();
	private static EncryptedPreferences encryptedPreferences;

	/**
	 * Retrieve an {@link EncryptedPreferences} instance with all default settings.
	 * @param context
	 * @return default {@link EncryptedPreferences}
	 */
	public static EncryptedPreferences getInstance(Context context) {
		if (encryptedPreferences == null) {
			encryptedPreferences = new EncryptedPreferences(context);
		}
		return encryptedPreferences;
	}

	private final SharedPreferences sharedPreferences;
	private final String            cryptoKey;
	private final EncryptedEditor   encryptedEditor;
	private final Utils             utils;
	private final boolean           printDebugMessages;

	private EncryptedPreferences(Context context) {
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.cryptoKey = generateEncryptionString(context);
		this.encryptedEditor = new EncryptedEditor(this);
		this.utils = new Utils(this);
		this.printDebugMessages = context.getResources().getBoolean(R.bool.enable_debug_messages);
	}

	private EncryptedPreferences(Builder builder) {
		this.sharedPreferences = TextUtils.isEmpty(builder.prefsName) ? PreferenceManager.getDefaultSharedPreferences(builder.context) : builder.context
				.getSharedPreferences(
				builder.prefsName,
				0);
		this.cryptoKey = TextUtils.isEmpty(builder.encryptionPassword) ? generateEncryptionString(builder.context) : builder.encryptionPassword;
		this.encryptedEditor = new EncryptedEditor(this);
		this.utils = new Utils(this);
		this.printDebugMessages = builder.context.getResources().getBoolean(R.bool.enable_debug_messages);
	}

	private synchronized void log(String logMessage) {
		if (printDebugMessages) {
			Log.d(TAG, logMessage);
		}
	}

	private String generateEncryptionString(Context context) {
		return context.getPackageName();
	}

	private String encryptString(String message) {
		try {
			String encString = AESCrypt.encrypt(cryptoKey, message);
			return encodeCharset(encString);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String decryptString(String message) {
		try {
			String decString = AESCrypt.encrypt(cryptoKey, message);
			return encodeCharset(decString);
		} catch (GeneralSecurityException e) {
			return null;
		}
	}

	private String encodeCharset(String value) {
		String encodedString = value;
		for (int i = 0; i < encodedString.length(); i++) {
			char currentChar = encodedString.charAt(i);
			if (!Character.isLetterOrDigit(currentChar)) {
				encodedString = encodedString.replace(currentChar, '0');
			}
		}
		Log.d(TAG, "encodeCharset() : " + value + " => " + encodedString);
		return encodedString;
	}

	private boolean containsEncryptedKey(String encryptedKey) {
		return sharedPreferences.contains(encryptedKey);
	}

	private <T> Object decryptType(String key, Object type, T defaultType) {
		String encKey = encryptString(key);

		log("decryptType() => encryptedKey => " + encKey);

		if (TextUtils.isEmpty(encKey) || !containsEncryptedKey(encKey)) {
			log("unable to encrypt or find key => " + encKey);
			return defaultType;
		}

		String value = sharedPreferences.getString(encKey, null);

		log("decryptType() => encryptedValue => " + value);

		if (TextUtils.isEmpty(value)) {
			return defaultType;
		}

		String orgValue = decryptString(value);
		log("decryptType() => orgValue => " + orgValue);

		if (TextUtils.isEmpty(orgValue)) {
			return defaultType;
		}

		if (type instanceof String) {
			return orgValue;
		} else if (type instanceof Integer) {
			try {
				return Integer.parseInt(orgValue);
			} catch (NumberFormatException e) {
				return defaultType;
			}
		} else if (type instanceof Long) {
			try {
				return Long.parseLong(orgValue);
			} catch (NumberFormatException e) {
				return defaultType;
			}
		} else if (type instanceof Float) {
			try {
				return Float.parseFloat(orgValue);
			} catch (NumberFormatException e) {
				return defaultType;
			}
		} else if (type instanceof Boolean) {
			return Boolean.parseBoolean(orgValue);
		} else {
			return defaultType;
		}
	}

	/**
	 * Retrieve an int value from the preferences.
	 * @param key - The name of the preference to retrieve
	 * @param defaultValue - Value to return if this preference does not exist
	 * @return int - Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a preference with this name that is not an
	 * int.
	 */
	public int getInt(String key, int defaultValue) {
		return (Integer) decryptType(key, 0, defaultValue);
	}

	/**
	 * Retrieve a long value from the preferences.
	 * @param key - The name of the preference to retrieve
	 * @param defaultValue - Value to return if this preference does not exist
	 * @return long - Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a preference with this name that is not a
	 * long
	 */
	public long getLong(String key, long defaultValue) {
		return (Long) decryptType(key, 0L, defaultValue);
	}

	/**
	 * Retrieve a boolean value from the preferences
	 * @param key - The name of the preference to retrieve
	 * @param defaultValue - Value to return if this preference does not exist
	 * @return - Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a preference with this name that is not a
	 * boolean
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		return (Boolean) decryptType(key, defaultValue, defaultValue);
	}

	/**
	 * Retrieve a float value from the preferences
	 * @param key - The name of the preference to retrieve
	 * @param defaultValue - Value to return if this preference does not exist
	 * @return float - Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a preference with this name that is not a
	 * float
	 */
	public float getFloat(String key, float defaultValue) {
		return (Float) decryptType(key, 0f, defaultValue);
	}

	/**
	 * Retrieve a String value from the preferences
	 * @param key - The name of the preference to retrieve
	 * @param defaultValue - Value to return if this preference does not exist
	 * @return String - Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a preference with this name that is not
	 * a String
	 */
	public String getString(String key, String defaultValue) {
		return (String) decryptType(key, "", defaultValue);
	}

	/**
	 * Checks whether the preferences contains a preference.
	 * @param key - The name of the preference to check
	 * @return Returns true if the preference exists in the preferences, otherwise false.
	 */
	public boolean contains(String key) {
		String encKey = encryptString(key);
		return sharedPreferences.contains(encKey);
	}

	/**
	 * Get the Editor for these preferences, through which you can make modifications to the data in the preferences and atomically commit those changes
	 * back to
	 * the SharedPreferences object.
	 * @return {@link EncryptedEditor}
	 */
	public EncryptedEditor edit() {
		return encryptedEditor;
	}

	/**
	 * Get the {@link Utils} instance for this EncryptedPreferences configuration.
	 * @return The {@link Utils} instance for this EncryptedPreferences configuration.
	 */
	public Utils getUtils() {
		return utils;
	}

	/**
	 * A class for several utility methods.
	 */
	public final class Utils {

		private final EncryptedPreferences encryptedPreferences;

		private Utils(EncryptedPreferences encryptedPreferences) {
			this.encryptedPreferences = encryptedPreferences;
		}

		/**
		 * Utility method to retrieve the encrypted value of a string using the current {@link EncryptedPreferences} configuration.
		 * @param value - String which should be encrypted
		 * @return The encrypted value of the given String
		 */
		public String encryptStringValue(String value) {
			return encryptedPreferences.encryptString(value);
		}

		/**
		 * Utility method to decrypt the given String using the current {@link EncryptedPreferences} configuration.
		 * @param value - String which should be decrypted
		 * @return The decrypted value of the given String
		 */
		public String decryptStringValue(String value) {
			return encryptedPreferences.decryptString(value);
		}

	}

	/**
	 * Class used for modifying values in a {@link EncryptedPreferences} object. All changes you make in an editor are batched, and not copied back to the
	 * original {@link EncryptedPreferences} until you call {@link EncryptedEditor#apply()}.
	 */
	public final class EncryptedEditor {

		private final String TAG = EncryptedEditor.class.getSimpleName();
		private final EncryptedPreferences encryptedPreferences;

		private EncryptedEditor(EncryptedPreferences encryptedPreferences) {
			this.encryptedPreferences = encryptedPreferences;
		}

		private synchronized void log(String logMessage) {
			if (encryptedPreferences.printDebugMessages) {
				Log.d(TAG, logMessage);
			}
		}

		private SharedPreferences.Editor editor() {
			return encryptedPreferences.sharedPreferences.edit();
		}

		private String encryptValue(String value) {
			String encryptedString = encryptedPreferences.encryptString(value);
			log("encryptValue() => " + encryptedString);
			return encryptedString;
		}

		private void putValue(String key, String value) {
			log("putValue() => " + key + " [" + encryptValue(key) + "] || " + value + " [" + encryptValue(value) + "]");
			editor().putString(encryptValue(key), encryptValue(value)).apply();
		}

		/**
		 * Set a String value in the preferences editor, to be written back once apply() is called.
		 * @param key - The name of the preference to modify
		 * @param value - The new value for the preference
		 * @return Returns a reference to the same Editor object, so you can chain put calls together.
		 */
		public EncryptedEditor putString(String key, String value) {
			putValue(key, value);
			return this;
		}

		/**
		 * Set an int value in the preferences editor, to be written back once apply() is called.
		 * @param key - The name of the preference to modify
		 * @param value - The new value for the preference
		 * @return Returns a reference to the same Editor object, so you can chain put calls together.
		 */
		public EncryptedEditor putInt(String key, int value) {
			putValue(key, String.valueOf(value));
			return this;
		}

		/**
		 * Set a long value in the preferences editor, to be written back once apply() is called.
		 * @param key - The name of the preference to modify
		 * @param value - The new value for the preference
		 * @return Returns a reference to the same Editor object, so you can chain put calls together.
		 */
		public EncryptedEditor putLong(String key, long value) {
			putValue(key, String.valueOf(value));
			return this;
		}

		/**
		 * Set a float value in the preferences editor, to be written back once apply() is called.
		 * @param key - The name of the preference to modify
		 * @param value - The new value for the preference
		 * @return Returns a reference to the same Editor object, so you can chain put calls together.
		 */
		public EncryptedEditor putFloat(String key, float value) {
			putValue(key, String.valueOf(value));
			return this;
		}

		/**
		 * Set a boolean value in the preferences editor, to be written back once apply() is called.
		 * @param key - The name of the preference to modify
		 * @param value - The new value for the preference
		 * @return Returns a reference to the same Editor object, so you can chain put calls together.
		 */
		public EncryptedEditor putBoolean(String key, boolean value) {
			putValue(key, String.valueOf(value));
			return this;
		}

		/**
		 * Mark in the editor that a preference value should be removed, which will be done in the actual preferences once apply() is called.
		 * @param key - The name of the preference to remove
		 * @return Returns a reference to the same Editor object, so you can chain put calls together.
		 */
		public EncryptedEditor remove(String key) {
			String encKey = encryptValue(key);
			if (containsEncryptedKey(encKey)) {
				log("remove() => " + key + " [ " + encKey + " ]");
				editor().remove(encKey);
			}
			return this;
		}

		/**
		 * Mark in the editor to remove all values from the preferences. Once commit is called, the only remaining preferences will be any that you have
		 * defined in this editor.
		 * @return Returns a reference to the same Editor object, so you can chain put calls together.
		 */
		public EncryptedEditor clear() {
			log("clear() => clearing preferences.");
			editor().clear();
			return this;
		}

		/**
		 * Commit your preferences changes back from this Editor to the {@link EncryptedPreferences} object it is editing. This atomically performs the
		 * requested
		 * modifications, replacing whatever is currently in the {@link EncryptedPreferences}.
		 */
		public void apply() {
			editor().apply();
		}

	}

	/**
	 * Class for configuring a new {@link EncryptedPreferences} instance.
	 */
	public static final class Builder {

		private final Context context;
		private       String  encryptionPassword;
		private       String  prefsName;

		/**
		 * The Builder's constructor
		 * @param context
		 */
		public Builder(Context context) {
			this.context = context.getApplicationContext();
		}

		/**
		 * Specify the encryption password which should be used when reading and writing values to preferences.
		 * @param encryptionPassword - The encryption password which should be used when reading and writing values
		 * @return
		 */
		public Builder withEncryptionPassword(String encryptionPassword) {
			this.encryptionPassword = encryptionPassword;
			return this;
		}

		/**
		 * Specify the name of the SharedPreferences instance which should be used to read and write values to.
		 * @param preferenceName - The name which will be used as SharedPreferences instance.
		 * @return
		 */
		public Builder withPreferenceName(String preferenceName) {
			this.prefsName = preferenceName;
			return this;
		}

		/**
		 * Build a new {@link EncryptedPreferences} instance with the specified configuration.
		 * @return A new {@link EncryptedPreferences} instance with the specified configuration
		 */
		public EncryptedPreferences build() {
			return new EncryptedPreferences(this);
		}

	}

}
