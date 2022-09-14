# Proton Pass

This repository contains the source code for the Proton Pass Android application.

## How to build

After cloning this repo, make sure that you have the required submodules:

```
$ git submodule update --init
```

Then, either open the project in Android Studio, or create manually a `local.properties` file with the following contents:

```
sdk.dir=PATH_TO_YOUR_ANDROID_SDK_DIR
```

Once you have that file available, copy it into the `proton-libs/` directory (this is required in order to build the Proton Core Android libraries from source).

By default, Android Studio will select the Alpha variant. For using it against our internal servers, open the "Build variants" selector in the lower-left corner and select `devDebug` for the `:app` module.

Otherwise, if you want to build the APK via command line, run the following command:

```
$ ./gradlew assembleDevDebug
```

The APK will be in `app/build/outputs/apk/dev/debug/`.
