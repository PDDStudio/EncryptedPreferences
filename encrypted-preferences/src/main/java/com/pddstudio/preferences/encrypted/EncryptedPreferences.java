package com.pddstudio.preferences.encrypted;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An AES-256 encrypted {@linkplain SharedPreferences} class, to read and write encrypted preferences.
 */
public final class EncryptedPreferences {

	private static final String TAG = EncryptedPreferences.class.getSimpleName();
	private static EncryptedPreferences encryptedPreferences;
	private static EncryptedPreferences singletonInstance;

	/**
	 * Interface definition for a callback to be invoked when a shared
	 * preference is changed.
	 */
	public interface OnSharedPreferenceChangeListener {

		/**
		 * Called when a shared preference is changed, added, or removed. This
		 * may be called even if a preference is set to its existing value.
		 *
		 * <p>This callback will be run on your main thread.
		 *
		 * @param encryptedPreferences The {@link EncryptedPreferences} that received
		 *            the change.
		 * @param key The key of the preference that was changed, added, or
		 *            removed.
		 */
		void onSharedPreferenceChanged(EncryptedPreferences encryptedPreferences, String key);
	}

	/**
	 * Retrieve an {@link EncryptedPreferences} instance with all default settings.
	 * @deprecated Due to security reasons it's recommended to use {@link Builder} for instance creation instead.
	 * @param context
	 * @return default {@link EncryptedPreferences}
	 */
	@Deprecated
	public static EncryptedPreferences getInstance(Context context) {
		if (encryptedPreferences == null) {
			encryptedPreferences = new EncryptedPreferences.Builder(context).build();
		}
		return encryptedPreferences;
	}

	/**
	 * Retrieve the configured {@link EncryptedPreferences} instance.
	 * Make sure to call {@link Builder#withSaveAsSingleton(boolean)} to initialize the singleton, otherwise a {@link RuntimeException} will be thrown.
	 * @return The configured {@link EncryptedPreferences} instance.
	 */
	public static EncryptedPreferences getSingletonInstance() {
		if (singletonInstance == null) {
			throw new RuntimeException("Singleton instance doesn't exist. Did you forget to set Builder.withSaveAsSingleton(true) ?");
		}
		return singletonInstance;
	}

	private final SharedPreferences                          sharedPreferences;
	private final String                                     cryptoKey;
	private final EncryptedEditor                            encryptedEditor;
	private final Utils                                      utils;
	private final boolean                                    printDebugMessages;
	private final List<OnSharedPreferenceChangeListenerImpl> listeners;

	private EncryptedPreferences(Builder builder) {
		this.sharedPreferences = TextUtils.isEmpty(builder.prefsName) ? PreferenceManager.getDefaultSharedPreferences(builder.context) : builder.context
				.getSharedPreferences(
				builder.prefsName,
				0);
		if (TextUtils.isEmpty(builder.encryptionPassword)) {
			throw new RuntimeException("Unable to initialize EncryptedPreferences! Did you forget to set a password using Builder.withEncryptionPassword" + "" +
											   "(encryptionKey) ?");
		} else {
			this.cryptoKey = builder.encryptionPassword;
		}
		this.encryptedEditor = new EncryptedEditor(this);
		this.utils = new Utils(this);
		this.printDebugMessages = builder.context.getResources().getBoolean(R.bool.enable_debug_messages);
		this.listeners = new ArrayList<>();
		if (!builder.listeners.isEmpty()) {
			for (OnSharedPreferenceChangeListener listener : builder.listeners) {
				registerListener(listener);
			}
		}
		singletonInstance = builder.singleton ? this : null;
	}

	private synchronized void log(String logMessage) {
		if (printDebugMessages) {
			Log.d(TAG, logMessage);
		}
	}

	private void registerListener(OnSharedPreferenceChangeListener listener) {
		if (checkIfListenerExist(listener)) {
			log("registerListener() : " + listener + " is already registered - skip adding.");
		} else {
			OnSharedPreferenceChangeListenerImpl listenerImpl = new OnSharedPreferenceChangeListenerImpl(this, listener);
			sharedPreferences.registerOnSharedPreferenceChangeListener(listenerImpl);
			listeners.add(listenerImpl);
			log("registerListener() : interface registered: " + listener + " ");
		}
	}

	private void unregisterListener(OnSharedPreferenceChangeListener listener) {
		if (checkIfListenerExist(listener)) {
			OnSharedPreferenceChangeListenerImpl listenerImpl = getListenerImpl(listener);
			sharedPreferences.unregisterOnSharedPreferenceChangeListener(listenerImpl);
			removeListenerImpl(listener);
			log("unregisterListener() : " + listenerImpl + " ( interface: " + listener + " )");
		} else {
			log("unregisterListener() : unable to find registered listener ( " + listener + ")");
		}
	}

	private OnSharedPreferenceChangeListenerImpl getListenerImpl(OnSharedPreferenceChangeListener listener) {
		for (OnSharedPreferenceChangeListenerImpl listenerImpl : listeners) {
			if (listener.equals(listenerImpl.getListenerInterface())) {
				return listenerImpl;
			}
		}
		return null;
	}

	private boolean checkIfListenerExist(OnSharedPreferenceChangeListener changeListener) {
		for (OnSharedPreferenceChangeListenerImpl listenerImpl : listeners) {
			if (changeListener.equals(listenerImpl.getListenerInterface())) {
				log("checkListener() : " + changeListener + " found implementation: " + listenerImpl);
				return true;
			}
		}
		return false;
	}

	private void removeListenerImpl(OnSharedPreferenceChangeListener listener) {
		log("removeListenerImpl() : requested for " + listener);
		for (int i = 0; i < listeners.size(); i++) {
			OnSharedPreferenceChangeListenerImpl listenerImpl = listeners.get(i);
			if (listener.equals(listenerImpl.getListenerInterface())) {
				listeners.remove(i);
				log("removeListenerImpl() : removed listener at position: " + i);
			}
		}
	}

	private void printListeners() {
		if (listeners.isEmpty()) {
			log("printListeners() => no listeners found");
		} else {
			for (OnSharedPreferenceChangeListenerImpl listenerImpl : listeners) {
				log("printListeners() => " + listenerImpl);
			}
		}
	}

	private void removeExistingPreferenceKey(String preferenceKey) {
		removeExistingPreferenceKeys(preferenceKey);
	}

	private void removeExistingPreferenceKeys(String... preferenceKeys) {
		Set<String> targetEntries = new HashSet<>();
		for(String key : preferenceKeys) {
			if (sharedPreferences.contains(key)) {
				targetEntries.add(key);
			} else {
				log("removeExistingPreferenceKey() : Couldn't find key '" + key + "' ! Skipping...");
			}
		}
		SharedPreferences.Editor batchEditor = sharedPreferences.edit();
		for(String targetKey : targetEntries) {
			batchEditor.remove(targetKey);
		}
		batchEditor.apply();
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
			String decString = removeEncoding(message);
			return AESCrypt.decrypt(cryptoKey, decString);
		} catch (GeneralSecurityException e) {
			return null;
		}
	}

	private String removeEncoding(String value) {
		String encodedString = value;
		encodedString = encodedString.replaceAll("x0P1Xx", "\\+").replaceAll("x0P2Xx", "/").replaceAll("x0P3Xx", "=");
		log("removeEncoding() : " + value + " => " + encodedString);
		return encodedString;
	}

	private String encodeCharset(String value) {
		String encodedString = value;
		encodedString = encodedString.replaceAll("\\+", "x0P1Xx").replaceAll("/", "x0P2Xx").replaceAll("=", "x0P3Xx");
		log("encodeCharset() : " + value + " => " + encodedString);
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
	 * Retrieve a {@linkplain Set<String>} of all currently stored keys.
	 * @param decrypt - Whether to decrypt stored keys before returning them or not.
	 * @return {@linkplain Set<String>} - Set with all stored keys.
	 */
	public Set<String> getAllKeys(boolean decrypt) {
		if(decrypt) {
			Set<String> decryptedKeySet = new HashSet<>();
			for(String key : sharedPreferences.getAll().keySet()) {
				decryptedKeySet.add(decryptString(key));
			}
			return decryptedKeySet;
		}
		return sharedPreferences.getAll().keySet();
	}

	/**
	 * Retrieve a {@linkplain Set<String>} of all currently stored keys.
	 * @return {@linkplain Set<String>} - Set with all stored keys.
	 */
	public Set<String> getAllKeys() {
		return getAllKeys(true);
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
	 * Allows you to import your unencrypted {@linkplain SharedPreferences}.
	 * This will immediately encrypt all your values without modifying them.
	 *
	 * @param sharedPreferences - The unencrypted {@linkplain SharedPreferences} instance
	 * @param override - Whether to override existing keys (and their values) or not
	 */
	public void importSharedPreferences(SharedPreferences sharedPreferences, boolean override) {
		importSharedPreferences(sharedPreferences, override, false);
	}

	/**
	 * Allows you to import your unencrypted {@linkplain SharedPreferences}.
	 * This will immediately encrypt all your values without modifying them.
	 *
	 * @param sharedPreferences - The unencrypted {@linkplain SharedPreferences} instance
	 * @param override - Whether to override existing keys (and their values) or not
	 * @param removeAfter - Whether to remove the old unencrypted entries after encrypting or not
	 */
	public void importSharedPreferences(SharedPreferences sharedPreferences, boolean override, boolean removeAfter) {
		if (sharedPreferences != null) {
			Map<String, ?> values = sharedPreferences.getAll();
			int importCount = 0;
			for (String key : values.keySet()) {
				if (!contains(key) || (contains(key) && override)) {
					log("-> Importing key: " + key);
					encryptedEditor.putValue(key, String.valueOf(values.get(key)));
					encryptedEditor.apply();
					++importCount;
					if(removeAfter && contains(key)) {
						sharedPreferences.edit().remove(key).apply();
						log("-> Deleted entry for key : " + key);
					}
				} else {
					log("-> Skip import for " + key + " : key already exist");
				}
			}
			log("Import finished! (" + importCount + "/" + values.size() + " entries imported)");
		}
	}

	/**
	 * Deletes all existing preference entries stored in its underlying {@linkplain SharedPreferences} instance.
	 * This should be used with caution as because {@link EncryptedPreferences} won't respect non-encrypted values during deletion.
	 * The operation is completely key-based, therefore no encryption key is required.
	 * <b>Removed values cannot be restored!</b>
	 *
	 * @see {@linkplain SharedPreferences#getAll()}
	 */
	public void forceDeleteExistingPreferences() {
		Set<String> storedKeys = sharedPreferences.getAll().keySet();
		removeExistingPreferenceKeys(storedKeys.toArray(new String[storedKeys.size()]));
	}

	/**
	 * Registers a callback to be invoked when a change happens to a preference.
	 * @param listener The callback that will run.
	 * @see #unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener)
	 */
	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		if (listener != null) {
			registerListener(listener);
		}
	}

	/**
	 * Unregisters a previous callback.
	 * @param listener The callback that should be unregistered.
	 * @see #registerOnSharedPreferenceChangeListener
	 */
	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		if (listener != null) {
			unregisterListener(listener);
		}
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



	private class OnSharedPreferenceChangeListenerImpl implements SharedPreferences.OnSharedPreferenceChangeListener {

		private final OnSharedPreferenceChangeListener listener;
		private final EncryptedPreferences             encryptedPreferences;

		private OnSharedPreferenceChangeListenerImpl(EncryptedPreferences encryptedPreferences, OnSharedPreferenceChangeListener listener) {
			this.listener = listener;
			this.encryptedPreferences = encryptedPreferences;
		}

		protected OnSharedPreferenceChangeListener getListenerInterface() {
			return listener;
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (checkIfListenerExist(listener)) {
				log("onSharedPreferenceChanged() : found listener " + listener);
				listener.onSharedPreferenceChanged(encryptedPreferences, encryptedPreferences.getUtils().decryptStringValue(key));
			} else {
				log("onSharedPreferenceChanged() : couldn't find listener (" + listener + ")");
			}
		}

	}

	/**
	 * Class used for modifying values in a {@link EncryptedPreferences} object. All changes you make in an editor are batched, and not copied back to the
	 * original {@link EncryptedPreferences} until you call {@link EncryptedEditor#apply()}.
	 */
	public final class EncryptedEditor {

		private final String TAG = EncryptedEditor.class.getSimpleName();
		private final EncryptedPreferences     encryptedPreferences;
		private final SharedPreferences.Editor editor;

		private EncryptedEditor(EncryptedPreferences encryptedPreferences) {
			this.encryptedPreferences = encryptedPreferences;
			this.editor = encryptedPreferences.sharedPreferences.edit();
		}

		private synchronized void log(String logMessage) {
			if (encryptedPreferences.printDebugMessages) {
				Log.d(TAG, logMessage);
			}
		}

		private SharedPreferences.Editor editor() {
			return editor;
		}

		private String encryptValue(String value) {
			String encryptedString = encryptedPreferences.encryptString(value);
			log("encryptValue() => " + encryptedString);
			return encryptedString;
		}

		private void putValue(String key, String value) {
			log("putValue() => " + key + " [" + encryptValue(key) + "] || " + value + " [" + encryptValue(value) + "]");
			editor().putString(encryptValue(key), encryptValue(value));
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

		/**
		 * Commit your preferences changes back from this Editor to the
		 * {@link EncryptedPreferences} object it is editing.  This atomically
		 * performs the requested modifications, replacing whatever is currently
		 * in the EncryptedPreferences.
		 *
		 * <p>Note that when two editors are modifying preferences at the same
		 * time, the last one to call commit wins.
		 *
		 * <p>If you don't care about the return value and you're
		 * using this from your application's main thread, consider
		 * using {@link #apply} instead.
		 *
		 * @return Returns true if the new values were successfully written
		 * to persistent storage.
		 */
		public boolean commit() {
			return editor().commit();
		}

	}

	/**
	 * Class for configuring a new {@link EncryptedPreferences} instance.
	 */
	public static final class Builder {

		private final Context context;
		private       String  encryptionPassword;
		private       String  prefsName;
		private boolean singleton = false;
		private final List<OnSharedPreferenceChangeListener> listeners;

		/**
		 * The Builder's constructor
		 * @param context
		 */
		public Builder(Context context) {
			this.context = context.getApplicationContext();
			this.listeners = new ArrayList<>();
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
		 * Specify the {@link EncryptedPreferences} instance to be configured as Singleton.
		 * This allows you to retrieve this configured
		 * {@link EncryptedPreferences} instance from wherever you need it inside your application by calling {@link EncryptedPreferences#getSingletonInstance()}.
		 * @param singleton - Whether to configure the configured {@link EncryptedPreferences} instance to be a singleton or not.
		 * @return
		 * @see {@link EncryptedPreferences#getSingletonInstance()}
		 */
		public Builder withSaveAsSingleton(boolean singleton) {
			this.singleton = singleton;
			return this;
		}

		/**
		 * Specify an {@link OnSharedPreferenceChangeListener} which will be registered immediately once the EncryptedPreference instance is initialized.
		 * This method can be called multiple times to register multiple {@link OnSharedPreferenceChangeListener}.
		 * @param listener - The {@link OnSharedPreferenceChangeListener} which should be registered
		 * @return
		 */
		public Builder withOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
			if (listener != null) {
				this.listeners.add(listener);
			}
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
