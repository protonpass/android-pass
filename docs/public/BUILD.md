# How to build

The most straightforward way to build and run this application locally is to:

- Install Android Studio: https://developer.android.com/studio/install
- Clone the repository. You have two options:
    - Use the `Project from version control` in Android Studio, or
    - Use the `git clone` command and import it into Android Studio
- Build and run the app directly in Android Studio

Alternatively, if you want to build the app directly from the command line (or using a different IDE, etc.), you will first need to install the command line tools from: https://developer.android.com/studio#cmdline-tools. Then you will need to install the SDK using the `sdkmanager` tool. After cloning the repository with `git clone` you will need to edit the `local.properties` file so that it points to the location of the SDK. Depending on which operating systems you use, the location of the SDK is usually:

- Windows: `C:\Users\<username>\AppData\Local\Android\sdk`
- MacOS: `/Users/<username>/Library/Android/Sdk/`
- Linux: `/home/<username>/Android/Sdk/`

Then, go to the app’s root directory in the command line tool and run:

```shell
$ ./gradlew :app:assemblePlayProdDebug
```

The APK will be in `app/build/outputs/apk/play/prod/debug/`.

## How to run the tests

To run the unit tests, run:

```shell
$ ./gradlew testDebugUnitTest
```

To run the instrumentation tests, run:

```shell
$ ./gradlew testDebugAndroidTest
```
