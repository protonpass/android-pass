.PHONY: build build-release build-fdroid build-payments install install-release install-fdroid install-payments test test-unit test-android detekt dep-guard clean

# Build
build:
	./gradlew :app:assembleDevBlackDebug

build-release:
	./gradlew :app:assemblePlayProdRelease

build-fdroid:
	./gradlew :app:assembleFdroidProdRelease

# Payments build - uses devProd variant (package: proton.android.pass.dev)
# Host defaults to payments.proton.black, override with: make build-payments PROTON_ENV=scientist
PROTON_ENV ?= payments
build-payments:
	PROD_ENV_URL=$(PROTON_ENV).proton.black ./gradlew :app:assembleDevProdDebug

# Install
install:
	./gradlew :app:installDevBlackDebug

install-release:
	./gradlew :app:installPlayProdRelease

install-fdroid:
	./gradlew :app:installFdroidProdRelease

install-payments:
	PROD_ENV_URL=$(PROTON_ENV).proton.black ./gradlew :app:installDevProdDebug

# Lint
detekt:
	./gradlew multiModuleDetekt

# Dependency guard
dep-guard:
	./gradlew :app:dependencyGuardBaseline

# Test
test: test-unit

test-unit:
	./gradlew testDevDebugUnitTest -x :pass:screenshot-tests:testDevDebugUnitTest --no-configuration-cache

test-android:
	./gradlew allDevicesDebugAndroidTest

# Clean
clean:
	./gradlew clean
