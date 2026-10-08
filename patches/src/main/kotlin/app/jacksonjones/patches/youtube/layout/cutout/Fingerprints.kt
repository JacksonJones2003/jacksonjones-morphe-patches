package app.jacksonjones.patches.youtube.layout.cutout

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string

/**
 * The app's own display cutout class, which holds the cutout safe insets the layouts
 * read from the root window insets instead of the insets dispatched to the views.
 */
internal object DisplayCutoutToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    filters = listOf(
        string("DisplayCutoutCompat{safeInsets=")
    )
)

internal object DisplayCutoutConstructorFingerprint : Fingerprint(
    classFingerprint = DisplayCutoutToStringFingerprint,
    name = "<init>",
    returnType = "V",
    parameters = listOf("Landroid/graphics/Rect;", "L")
)
