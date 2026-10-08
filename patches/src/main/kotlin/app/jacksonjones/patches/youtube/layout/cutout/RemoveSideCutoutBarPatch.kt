package app.jacksonjones.patches.youtube.layout.cutout

import app.jacksonjones.patches.youtube.shared.Constants.COMPATIBILITY_YOUTUBE
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
            "invoke-static { p0 }, $EXTENSION_CLASS->initialize(Landroid/app/Activity;)V"
        )

        // Cutout safe insets the app reads from the root window insets.
        DisplayCutoutConstructorFingerprint.method.addInstruction(
            0,
            "invoke-static { p1 }, $EXTENSION_CLASS->removeSideCutout(Landroid/graphics/Rect;)V"
        )
    }
}
