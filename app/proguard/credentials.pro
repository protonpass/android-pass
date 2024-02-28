# Extracted from https://developer.android.com/training/sign-in/passkeys#proguard
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}
