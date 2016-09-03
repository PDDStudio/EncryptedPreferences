##EncryptedPreferences
An Android Library to securely read and write encrypted values to your SharedPreferences.

##Reason & Explanation

When developing an Android application you often save primitive values to your application's internal folder, using [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences.html).

All values written to your SharedPreferences are stored unencrypted in a simple `.xml` file inside your application's internal directory. In case you save sensitive or important data here, it can be easily read (and modified) by users with a rooted phone. To avoid this, I created EncryptedPreferences.

EncryptedPreferences is a simple wrapper around the oficial SharedPreferences, which saves all data encrypted using an AES 256-bit key. The library takes care about all de- and encryption, so all you have to care about is to read and write your primitive types.

EncryptedPreferences aims to have the same API as the offical SharedPreferences, to make it as easy as possible to switch over.

##Getting Started

###Add the library as dependency
Add the library as dependency to your app's `build.gradle` file.

`
dependencies {
    compile 'com.pddstudio:encrypted-preferences:X.X.X'
}
`
Replace X.X.X with the current version - which can be found on [Maven Central]()

###Start using EncryptedPreferences
To start using EncryptedPreferences you have to create a new instance using the `EncryptedPreferences.Builder`.

**Example:**

```java
EncryptedPreferences encryptedPreferences = new EncryptedPreferences.Builder(this).withEncryptionPassword("password").build();
```
