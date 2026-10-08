package app.jacksonjones.extension.youtube.shared;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowInsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Changes the window insets dispatched to the views of an activity.
 * A view has a single insets listener, so the patches share the listener of this class.
 */
public final class WindowInsetsHook {

    public interface Modifier {
        WindowInsets modify(WindowInsets insets);
    }

    private static final String TAG = "WindowInsetsHook";

    private static final Map<Activity, List<Modifier>> activityModifiers = new WeakHashMap<>();

    /**
     * Must be called when the activity is created. The insets listener is set when the activity is started,
     * because getting the views any earlier creates the window before the activity has set its theme.
     */
    public static void addModifier(Activity activity, Modifier modifier) {
        List<Modifier> modifiers = activityModifiers.get(activity);
        if (modifiers == null) {
            modifiers = new ArrayList<>();
            activityModifiers.put(activity, modifiers);

            final List<Modifier> listenerModifiers = modifiers;
            activity.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                public void onActivityStarted(Activity activity) {
                    try {
                        setInsetsListener(activity, listenerModifiers);
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
        }
        modifiers.add(modifier);
    }

    /**
     * Dispatches the insets again, for when a modifier changes what it does.
     */
    public static void requestApplyInsets(Activity activity) {
        View decorView = activity.getWindow().peekDecorView();
        if (decorView != null) {
            decorView.requestApplyInsets();
        }
    }

    /**
     * The listener is set on the view below the decor view, so every view of the activity gets
     * the changed insets, and the listeners the app sets on its own views are not replaced.
     */
    private static void setInsetsListener(Activity activity, List<Modifier> modifiers) {
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
            for (Modifier modifier : modifiers) {
                try {
                    insets = modifier.modify(insets);
                } catch (Exception ex) {
                    Log.e(TAG, "modify failure", ex);
                }
            }
            return view.onApplyWindowInsets(insets);
        });
        root.requestApplyInsets();
    }
}
