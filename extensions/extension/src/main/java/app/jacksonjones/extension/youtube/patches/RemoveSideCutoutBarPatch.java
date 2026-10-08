package app.jacksonjones.extension.youtube.patches;

import android.app.Activity;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.WindowInsets;

import app.jacksonjones.extension.youtube.shared.WindowInsetsHook;

@SuppressWarnings("unused")
public final class RemoveSideCutoutBarPatch {

    private static final String TAG = "RemoveSideCutoutBar";

    /**
     * Injection point.
     * <p>
     * Called when the main activity is created.
     */
    public static void initialize(Activity activity) {
        // Insets of a single type cannot be replaced before Android 11.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return;
        }

        try {
            WindowInsetsHook.addModifier(activity, RemoveSideCutoutBarPatch::removeSideCutout);
        } catch (Exception ex) {
            Log.e(TAG, "initialize failure", ex);
        }
    }

    /**
     * Injection point.
     * <p>
     * Called with the cutout safe insets the app reads from the root window insets,
     * which do not pass thru the insets listener.
     */
    public static void removeSideCutout(Rect safeInsets) {
        if (safeInsets != null) {
            safeInsets.left = 0;
            safeInsets.right = 0;
        }
    }

    /**
     * A cutout is at the side only when the device is in landscape,
     * so a cutout at the top in portrait keeps its inset.
     */
    private static WindowInsets removeSideCutout(WindowInsets insets) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return insets;
        }

        DisplayCutout cutout = insets.getDisplayCutout();
        if (cutout == null || (cutout.getSafeInsetLeft() == 0 && cutout.getSafeInsetRight() == 0)) {
            return insets;
        }

        Insets safeInsets = Insets.of(0, cutout.getSafeInsetTop(), 0, cutout.getSafeInsetBottom());
        return new WindowInsets.Builder(insets)
                .setInsets(WindowInsets.Type.displayCutout(), safeInsets)
                .setDisplayCutout(new DisplayCutout(safeInsets, null,
                        cutout.getBoundingRectTop(), null, cutout.getBoundingRectBottom()))
                .build();
    }
}
