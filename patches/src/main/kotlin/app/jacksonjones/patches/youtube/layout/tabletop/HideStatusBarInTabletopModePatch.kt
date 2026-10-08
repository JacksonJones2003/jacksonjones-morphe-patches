package app.jacksonjones.patches.youtube.layout.tabletop

import app.jacksonjones.patches.youtube.shared.Constants.COMPATIBILITY_YOUTUBE
import app.jacksonjones.patches.youtube.shared.MainActivityOnCreateFingerprint
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.methodCall
import app.morphe.patcher.newInstance
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val EXTENSION_CLASS =
    "Lapp/jacksonjones/extension/youtube/patches/HideStatusBarInTabletopModePatch;"

@Suppress("unused")
val hideStatusBarInTabletopModePatch = bytecodePatch(
    name = "Hide status bar in tabletop mode",
    description = "Hides the status bar when a foldable device is half opened into tabletop mode, " +
        "and shows it again when the device leaves tabletop mode.",
    default = true
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)

    extendWith("extensions/extension.mpe")

    execute {
        MainActivityOnCreateFingerprint.method.addInstruction(
            0,
            // The register of 'this' is above v15 in this method, so the range format is needed.
            "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS->initialize(Landroid/app/Activity;)V"
        )

        val clamshellStateType = ClamshellStateToStringFingerprint.originalMethod.definingClass
        val otherStateTypes = setOf(
            BookStateToStringFingerprint.originalMethod.definingClass,
            FlatStateToStringFingerprint.originalMethod.definingClass,
            NonFoldingStateToStringFingerprint.originalMethod.definingClass
        )

        // The method that maps the folding features of the window to the fold state of the app.
        val foldStateMethod = Fingerprint(
            returnType = "Ljava/lang/Object;",
            parameters = listOf("Ljava/lang/Object;"),
            filters = listOf(
                methodCall(definingClass = "Landroid/graphics/Rect;", name = "height"),
                newInstance(clamshellStateType)
            )
        ).method

        val stateIndexes = foldStateMethod.implementation!!.instructions.withIndex().filter { (_, instruction) ->
            instruction.opcode == Opcode.NEW_INSTANCE
        }.mapNotNull { (index, instruction) ->
            when (((instruction as ReferenceInstruction).reference as TypeReference).type) {
                clamshellStateType -> index to "enterTabletopMode"
                in otherStateTypes -> index to "exitTabletopMode"
                else -> null
            }
        }

        if (stateIndexes.none { it.second == "exitTabletopMode" }) {
            throw PatchException("Could not find where the app leaves tabletop mode")
        }

        // The states are created at branch targets, so the calls are added after the constructor
        // calls instead of before the states are created. Added in reverse to keep the indexes.
        stateIndexes.asReversed().forEach { (index, extensionMethod) ->
            foldStateMethod.addInstruction(
                index + 2,
                "invoke-static { }, $EXTENSION_CLASS->$extensionMethod()V"
            )
        }
    }
}
