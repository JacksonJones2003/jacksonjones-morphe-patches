package app.jacksonjones.patches.youtube.layout.tabletop

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string

/**
 * The fold states of the app. The clamshell state is the tabletop mode,
 * where the device is half opened and the fold is horizontal.
 */
internal object ClamshellStateToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    filters = listOf(
        string("ClamshellState{foldPositionY=")
    )
)

internal object BookStateToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    filters = listOf(
        string("BookState{foldPositionX=")
    )
)

internal object FlatStateToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    filters = listOf(
        string("FlatState{}")
    )
)

internal object NonFoldingStateToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    filters = listOf(
        string("NonFoldingState{}")
    )
)
