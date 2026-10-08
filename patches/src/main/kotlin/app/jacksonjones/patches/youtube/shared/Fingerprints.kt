package app.jacksonjones.patches.youtube.shared

import app.jacksonjones.patches.youtube.shared.Constants.YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
import app.morphe.patcher.Fingerprint

internal object MainActivityOnCreateFingerprint : Fingerprint(
    definingClass = YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE,
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;")
)
