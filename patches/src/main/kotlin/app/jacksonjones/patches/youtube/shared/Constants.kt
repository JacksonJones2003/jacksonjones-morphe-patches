package app.jacksonjones.patches.youtube.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    const val YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE =
        "Lcom/google/android/apps/youtube/app/watchwhile/MainActivity;"

    val COMPATIBILITY_YOUTUBE = Compatibility(
        name = "YouTube",
        packageName = "com.google.android.youtube",
        apkFileType = ApkFileType.APK_REQUIRED,
        appIconColor = 0xFF0033,
        targets = listOf(
            // Other app targets are untested.
            AppTarget(
                version = null,
                isExperimental = true
            ),
            // App version used to develop the patches.
            AppTarget(
                version = "21.38.123",
                minSdk = 29
            )
        )
    )
}
