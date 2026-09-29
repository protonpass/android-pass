-keep class proton.android.pass.ui.PassShowkaseModuleCodegen { *; }

-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite* {
  <fields>;
}

# Material bottomsheet invoked through reflection (SheetContentHost)
-keep class kotlin.Metadata { *; }
-keep class androidx.compose.material.ModalBottomSheetState { <methods>; }
-keep class androidx.compose.material.ModalBottomSheetValue { *; }

# Generated kotlin bindings for Rust library
-keep class proton.android.pass.commonrust.** { *; }
