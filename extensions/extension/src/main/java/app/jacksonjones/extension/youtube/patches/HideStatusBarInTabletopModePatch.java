package app.jacksonjones.extension.youtube.patches;

import android.app.Activity;
import android.app.Application;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import java.lang.ref.WeakReference;

import app.jacksonjones.extension.youtube.shared.WindowInsetsHook;

@SuppressWarnings("unused")
public final class HideStatusBarInTabletopModePatch {

    private static final String TAG = "HideStatusBarInTabletop";

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static WeakReference<Activity> activityRef = new WeakReference<>(null);

    private static boolean tabletopMode;

    /**
     * If the status bar is hidden by this patch, so a status bar the app has hidden
     * for another reason is not shown when the device leaves tabletop mode.
     */
    private static boolean statusBarHidden;

    /**
     * Injection point.
     * <p>
     * Called when the main activity is created. The layout listener is added when the activity is started,
     * because getting the views any earlier creates the window before the activity has set its theme.
     */
    public static void initialize(Activity activity) {
        // The status bar cannot be hidden on its own before Android 11.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return;
        }

        try {
            activityRef = new WeakReference<>(activity);
            statusBarHidden = false;

            WindowInsetsHook.addModifier(activity, HideStatusBarInTabletopModePatch::removeStatusBarInset);

            activity.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                private boolean layoutListenerAdded;

                public void onActivityStarted(Activity activity) {
                    try {
                        if (!layoutListenerAdded) {
                            layoutListenerAdded = true;
                            addLayoutListener(activity);
                        }
                        updateStatusBar();
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
     * Called when the app finds the device is half opened with a horizontal fold.
     */
    public static void enterTabletopMode() {
        setTabletopMode(true);
    }

    /**
     * Injection point.
     * <p>
     * Called when the app finds the device is in any other fold state.
     */
    public static void exitTabletopMode() {
        setTabletopMode(false);
    }

    /**
     * The fold state is not always found on the main thread.
     */
    private static void setTabletopMode(boolean enabled) {
        mainHandler.post(() -> {
            try {
                tabletopMode = enabled;
                updateStatusBar();
            } catch (Exception ex) {
                Log.e(TAG, "setTabletopMode failure", ex);
            }
        });
    }

    /**
     * The app shows the status bar again when it changes the system bars, such as when
     * the player controls are shown, so it is hidden again whenever the window is laid out
     * with it visible in tabletop mode.
     */
    private static void addLayoutListener(Activity activity) {
        View decorView = activity.getWindow().getDecorView();

        decorView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            try {
                if (tabletopMode && activityRef.get() == activity && isStatusBarVisible(decorView)) {
                    updateStatusBar();
                }
            } catch (Exception ex) {
                Log.e(TAG, "onGlobalLayout failure", ex);
            }
        });
    }

    private static void updateStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return;
        }

        Activity activity = activityRef.get();
        if (activity == null) {
            return;
        }

        Window window = activity.getWindow();
        View decorView = window.peekDecorView();
        if (decorView == null) {
            return;
        }

        WindowInsetsController controller = window.getInsetsController();
        if (controller == null) {
            return;
        }

        boolean wasStatusBarHidden = statusBarHidden;
        if (tabletopMode) {
            statusBarHidden = true;
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            controller.hide(WindowInsets.Type.statusBars());
        } else if (statusBarHidden) {
            statusBarHidden = false;
            // The app hides the status bar with the fullscreen flag, such as for a fullscreen video.
            //noinspection deprecation
            if ((decorView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                controller.show(WindowInsets.Type.statusBars());
            }
        }

        if (wasStatusBarHidden != statusBarHidden) {
            WindowInsetsHook.requestApplyInsets(activity);
        }
    }

    /**
     * The app lays out its views with the stable insets, which keep the space of the status bar
     * while it is hidden, so the video and the tabletop controls do not use that space.
     * The inset of the status bar is removed while it is hidden by this patch.
     */
    private static WindowInsets removeStatusBarInset(WindowInsets insets) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || !statusBarHidden) {
            return insets;
        }

        return new WindowInsets.Builder(insets)
                .setInsets(WindowInsets.Type.statusBars(), Insets.NONE)
                .setInsetsIgnoringVisibility(WindowInsets.Type.statusBars(), Insets.NONE)
                .setVisible(WindowInsets.Type.statusBars(), false)
                .build();
    }

    private static boolean isStatusBarVisible(View decorView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return false;
        }

        WindowInsets insets = decorView.getRootWindowInsets();
        return insets != null && insets.isVisible(WindowInsets.Type.statusBars());
    }
}
