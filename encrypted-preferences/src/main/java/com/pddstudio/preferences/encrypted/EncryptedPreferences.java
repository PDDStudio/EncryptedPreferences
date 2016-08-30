package com.pddstudio.preferences.encrypted;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

/**
 * Created by pddstudio on 30/08/16.
 */
public final class EncryptedPreferences {

	private static EncryptedPreferences encryptedPreferences;

	public static EncryptedPreferences getInstance(Context context) {
		if (encryptedPreferences == null) {
			encryptedPreferences = new EncryptedPreferences(context);
		}
		return encryptedPreferences;
	}

	private final SharedPreferences sharedPreferences;
	private final String            cryptoKey;
	private final EncryptedEditor   encryptedEditor;

	private EncryptedPreferences(Context context) {
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.cryptoKey = "testPassword";
		this.encryptedEditor = new EncryptedEditor(this);
	}

	private String encryptString(String message) {
		try {
			return AESCrypt.encrypt(cryptoKey, message);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String decryptString(String message) {
		try {
			return AESCrypt.encrypt(cryptoKey, message);
		} catch (GeneralSecurityException e) {
			return null;
		}
	}

	private <T> Object decryptType(String key, Object type, T defaultType) {
		String encKey = encryptString(key);

		if (TextUtils.isEmpty(encKey) || !contains(encKey)) {
			return defaultType;
		}

		String value = sharedPreferences.getString(encKey, null);

		if (TextUtils.isEmpty(value)) {
			return defaultType;
		}

		String orgValue = decryptString(value);

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

	public int getInt(String key, int defaultValue) {
		return (Integer) decryptType(key, 0, defaultValue);
	}

	public long getLong(String key, long defaultValue) {
		return (Long) decryptType(key, 0L, defaultValue);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return (Boolean) decryptType(key, defaultValue, defaultValue);
	}

	public float getFloat(String key, float defaultValue) {
		return (Float) decryptType(key, 0f, defaultValue);
	}

	public String getString(String key, String defaultValue) {
		return (String) decryptType(key, "", defaultValue);
	}

	public boolean contains(String key) {
		String encKey = encryptString(key);
		return sharedPreferences.contains(encKey);
	}

	public EncryptedEditor edit() {
		return encryptedEditor;
	}

	public final class EncryptedEditor {

		private final EncryptedPreferences encryptedPreferences;

		private EncryptedEditor(EncryptedPreferences encryptedPreferences) {
			this.encryptedPreferences = encryptedPreferences;
		}

		private SharedPreferences.Editor editor() {
			return encryptedPreferences.sharedPreferences.edit();
		}

		private String encryptValue(String value) {
			return encryptedPreferences.encryptString(value);
		}

		private void putValue(String key, String value) {
			editor().putString(encryptValue(key), encryptValue(value));
		}

		public EncryptedEditor putString(String key, String value) {
			putValue(key, value);
			return this;
		}

		public EncryptedEditor putInt(String key, int value) {
			putValue(key, String.valueOf(value));
			return this;
		}

		public EncryptedEditor putLong(String key, long value) {
			putValue(key, String.valueOf(value));
			return this;
		}

		public EncryptedEditor putFloat(String key, float value) {
			putValue(key, String.valueOf(value));
			return this;
		}

		public EncryptedEditor putBoolean(String key, boolean value) {
			putValue(key, String.valueOf(value));
			return this;
		}

		public EncryptedEditor remove(String key) {
			//TODO: implement function
			return this;
		}

		public EncryptedEditor clear() {
			//TODO: implement function
			return this;
		}

		public void apply() {
			editor().apply();
		}

	}

}
