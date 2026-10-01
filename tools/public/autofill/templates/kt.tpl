package proton.android.pass.autofill

val BROWSERS = setOf(
{{CONTENT}}
)

/**
 * Gecko-based browsers that use Android's native autofill framework instead of
 * exposing a third-party-mode ContentProvider. Consumed by autofill health
 * coverage resolution to report them as ready.
 */
val NATIVE_AUTOFILL_BROWSERS = setOf(
{{NATIVE_CONTENT}}
)
