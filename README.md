# Proton Pass

This repository contains the source code for the Proton Pass Android application.

## How to build

After cloning this repo, make sure that you have the required submodules:

```
$ git submodule update --init
```

Also make sure you have installed [git-lfs](https://git-lfs.github.com/) to be able to run screenshot tests.

If you want to build the APK via command line, run the following command:

```
$ ./gradlew :app:assembleDevDebug
```

The APK will be in `app/build/outputs/apk/dev/debug/`.

## How to record new screenshots with Paparazzi

To be able to record new images for screenshot testing you will need to run the following task:

```
$ ./gradlew :pass:screenshot-tests:recordPaparazziDevDebug
```

## How to create modules

To ease modularization we have a task that will create modules for us, we can run it with the following command:

```
$ ./gradlew genModule --module=:pass:my-new-module --conf=api,impl,fakes
```

Where as a module we specify the path of the module and as configuration we pass which submodules should contain. `api` for the exposed contract, `impl` for the actual implementation and `fakes` for the exposed fake implementations for tests.