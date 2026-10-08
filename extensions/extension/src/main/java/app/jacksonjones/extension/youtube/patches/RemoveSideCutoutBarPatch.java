package app.jacksonjones.extension.youtube.patches;

import android.app.Activity;
import android.app.Application;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowInsets;

@SuppressWarnings("unused")
public final class RemoveSideCutoutBarPatch {

    private static final String TAG = "RemoveSideCutoutBar";

    /**
     * Injection point.
     * <p>
     * Called when the main activity is created. The insets listener is set when the activity is started,
     * because getting the views any earlier creates the window before the activity has set its theme.
     */
    public static void initialize(Activity activity) {
        // Insets of a single type cannot be replaced before Android 11.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return;
        }

        try {
            activity.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                public void onActivityStarted(Activity activity) {
                    try {
                        setInsetsListener(activity);
                    } catch (Exception ex) {
                        Log.e(TAG, "onActivityStarted failure", ex);
                    }
                }

                public void onActivityCreated(Activity a, Bundle b) {}
                public void onActivityResumed(Activity a) {}
                public void onActivityPaused(Activity a) {}
                public void onActivityStopped(Activity a) {}
                public void onActivitySaveInstanceState(Activity a, Bundle b) {}
                public void onActivityDestroyed(Activity a) {}
            });
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
     * The listener is set on the view below the decor view, so every view of the activity gets
     * the changed insets, and the listeners the app sets on its own views are not replaced.
     */
    private static void setInsetsListener(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        View root = decorView.findViewById(android.R.id.content);
        if (root == null) {
            return;
        }

        ViewParent parent = root.getParent();
        while (parent instanceof View && parent != decorView) {
            root = (View) parent;
            parent = root.getParent();
        }

        root.setOnApplyWindowInsetsListener((view, insets) -> {
            try {
                insets = removeSideCutout(insets);
            } catch (Exception ex) {
                Log.e(TAG, "removeSideCutout failure", ex);
            }
            return view.onApplyWindowInsets(insets);
        });
        root.requestApplyInsets();
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
