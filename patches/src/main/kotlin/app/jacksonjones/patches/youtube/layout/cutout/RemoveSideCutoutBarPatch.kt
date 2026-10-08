package app.jacksonjones.patches.youtube.layout.cutout

import app.jacksonjones.patches.youtube.shared.Constants.COMPATIBILITY_YOUTUBE
import app.jacksonjones.patches.youtube.shared.MainActivityOnCreateFingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch

private const val EXTENSION_CLASS =
    "Lapp/jacksonjones/extension/youtube/patches/RemoveSideCutoutBarPatch;"

@Suppress("unused")
val removeSideCutoutBarPatch = bytecodePatch(
    name = "Remove side cutout bar",
    description = "Removes the black bar reserved for the camera cutout at the side of the screen " +
        "in landscape. The space reserved for a cutout at the top in portrait is not changed.",
    default = true
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)

    extendWith("extensions/extension.mpe")

    execute {
        // Insets dispatched to the views of the main activity.
        MainActivityOnCreateFingerprint.method.addInstruction(
            0,
            // The register of 'this' is above v15 in this method, so the range format is needed.
            "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS->initialize(Landroid/app/Activity;)V"
        )

        // Cutout safe insets the app reads from the root window insets.
        DisplayCutoutConstructorFingerprint.method.addInstruction(
            0,
            "invoke-static/range { p1 .. p1 }, $EXTENSION_CLASS->removeSideCutout(Landroid/graphics/Rect;)V"
        )
    }
}
