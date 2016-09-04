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
    compile 'com.pddstudio:encrypted-preferences:1.0.0'
}
```
Make sure you're always using the latest version, which can be found on [Maven Central](http://search.maven.org/#artifactdetails%7Ccom.pddstudio%7Cencrypted-preferences).

###Start using EncryptedPreferences
To start using EncryptedPreferences you have to create a new instance using the `EncryptedPreferences.Builder`.

**Example:**

```java
EncryptedPreferences encryptedPreferences = new EncryptedPreferences.Builder(this).withEncryptionPassword("password").build();
```

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

For more information about how to read and write data to SharedPreferences, head over to the [official Android Developer Guide](https://developer.android.com/training/basics/data-storage/shared-preferences.html).

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