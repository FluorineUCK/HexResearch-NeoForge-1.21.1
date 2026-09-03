package name.dashkal.minecraft.hexresearch.forge;

import java.lang.reflect.InvocationTargetException;

/** Loads opt-in validation probes without linking them into the release JAR. */
final class DevelopmentProbeBootstrap {
    private DevelopmentProbeBootstrap() {
    }

    static void register(String className, String... enablingProperties) {
        boolean enabled = false;
        for (String property : enablingProperties) {
            if (Boolean.getBoolean(property)) {
                enabled = true;
                break;
            }
        }
        if (!enabled) {
            return;
        }

        try {
            Class.forName(className).getMethod("register").invoke(null);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(
                "Development probe was requested but is not present: " + className,
                exception
            );
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Could not register development probe: " + className, exception);
        }
    }
}
