-keep class proton.android.pass.ui.PassShowkaseModuleCodegen { *; }

-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite* {
  <fields>;
}

# Material bottomsheet invoked through reflection
-keepclassmembernames class androidx.compose.material.ModalBottomSheetState {
    <methods>;
}
