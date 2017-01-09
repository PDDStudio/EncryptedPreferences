##EncryptedPreferences
An Android Library to securely read and write encrypted values to your SharedPreferences.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.pddstudio/encrypted-preferences/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.pddstudio/encrypted-preferences)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-EncryptedPreferences-green.svg?style=true)](https://android-arsenal.com/details/1/4280)


##Reason & Explanation

When developing an Android application you often save primitive values to your application's internal storage using [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences.html).

All values written to your SharedPreferences are stored unencrypted in a simple and plain `.xml file` inside your application's internal directory. In case you save sensitive or important data here, it can be easily read (and modified) by users with a rooted phone. To avoid this, I created EncryptedPreferences.

EncryptedPreferences is a simple wrapper around the official SharedPreferences API, which saves all data (containing both, keys and values) encrypted using [Advanced Encryption Standard](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) (AES/256 bit key). The library takes care about all heavy lifting, so all you have to do is to read and write your preferences as usual.

EncryptedPreferences aims to have the same API as the official SharedPreferences, to make it as easy as possible to integrate it into your application.

##Getting Started

###Add the library as dependency
Add the library as dependency to your app's `build.gradle` file.

```
dependencies {
    compile 'com.pddstudio:encrypted-preferences:1.4.0'
}
```
Make sure you're always using the latest version, which can be found on [Maven Central](http://search.maven.org/#artifactdetails%7Ccom.pddstudio%7Cencrypted-preferences).

###Start using EncryptedPreferences
To start using EncryptedPreferences you have to create a new instance using the `EncryptedPreferences.Builder`.

**Example:**

```java
EncryptedPreferences encryptedPreferences = new EncryptedPreferences.Builder(this).withEncryptionPassword("password").build();
```

**Note:** Starting with version 1.2.0 the support for default (password/key) configuration is deprecated due to security reasons. Therefore it's now required to specify your own password using the Builder's `.withEncryptionPassword("password")` method. If no password is set a `RuntimeException` will be thrown.

Once you created your `EncryptedPreferences` instance, you can read and write values.

**Saving Values:**

```java
encryptedPreferences.edit()
				.putString(TEST_KEY_VALUE_STRING, "testString")
				.putFloat(TEST_KEY_VALUE_FLOAT, 1.5f)
				.putLong(TEST_KEY_VALUE_LONG, 10L)
				.putBoolean(TEST_KEY_VALUE_BOOLEAN, false)
				.apply();
```

*Note:*
As with the official SharedPreferences API, make sure to call `apply()` in order to save your changes!

**Reading Values:**

```java
private void printValues() {
		Log.d("MainActivity", TEST_KEY_VALUE_STRING + " => " + encryptedPreferences.getString(TEST_KEY_VALUE_STRING, TEST_KEY_VALUE_STRING));
		Log.d("MainActivity", TEST_KEY_VALUE_FLOAT + " => " + encryptedPreferences.getFloat(TEST_KEY_VALUE_FLOAT, 0));
		Log.d("MainActivity", TEST_KEY_VALUE_LONG + " => " + encryptedPreferences.getLong(TEST_KEY_VALUE_LONG, 0));
		Log.d("MainActivity", TEST_KEY_VALUE_BOOLEAN + " => " + encryptedPreferences.getBoolean(TEST_KEY_VALUE_BOOLEAN, true));
	}
```

**Listening for Changes:**

Beginning with version 1.2.0 it is possible to get notified when a value changed (added/updated/removed). 

You can receive change events by implementing the `EncryptedPreferences.OnSharedPreferenceChangeListener` interface into your Activity/Fragment (or custom component). Make sure to respect your component's lifecycle and register/unregister the listener(s) to avoid unwanted behaviours.

```java

	@Override
	protected void onCreate() {
		super.onCreate();
		//some other stuff here...
		
		//register the listener
		encryptedPreferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onDestroy() {
		//unregister the listener before destroying the Activity/Fragment
		encryptedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}
	
	@Override
	public void onSharedPreferenceChanged(EncryptedPreferences encryptedPreferences, String key) {
		//do your stuff with the changed data here
		Log.d("MainActivity", "onSharedPreferenceChanged() => key: " + key);
	}

```

For more information about how to read and write data to SharedPreferences, head over to the [official Android Developer Guide](https://developer.android.com/training/basics/data-storage/shared-preferences.html).

**Utilities:**

Since version 1.0.1 EncryptedPreferences has a utility class, which might come in handy for several usecases beside persisting data. The `EncryptedPreferences.Utils` class is bound to your `EncryptedPreferences` instance and uses the same configuration.

You can retrieve the `EncryptedPreferences.Utils` instance by calling `getUtils()` on your `EncryptedPreferences` object.

*Example:*

```java
private void utilsExample() {
		//get the encrypted value for an api key while debugging, so we don't have to save the original api key as plain text in production.
		String encryptedApiKey = encryptedPreferences.getUtils().encryptStringValue("SOME_API_KEY_HERE");
		Log.d("MainActivity", "encryptedApiKey => " + encryptedApiKey);
		//in production we simply use the utility method with the encrypted value which we got from debugging.
		String decryptedApiKey = encryptedPreferences.getUtils().decryptStringValue(encryptedApiKey);
		Log.d("MainActivity", "decryptedApiKey => " + decryptedApiKey);
	}
```

##Third-Party Libraries
This library is using [AESCrypt-Android](https://github.com/scottyab/AESCrypt-Android) by [scottyab](https://github.com/scottyab).

##About & Contact
- In case you've a question feel free to hit me up via E-Mail (patrick.pddstudio[at]googlemail.com) 
- or [Google+](http://plus.google.com/+PatrickJung42) / Hangouts

##License
```
    Copyright 2016 Patrick J

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
```