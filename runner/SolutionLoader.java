package runner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Loads and instantiates the user's Solution class via Reflection.
 */
public class SolutionLoader {

    public static Class<?> loadSolutionClass() throws ClassNotFoundException {
        try {
            // First try default package "Solution"
            return Class.forName("Solution");
        } catch (ClassNotFoundException e) {
            // Fallback to thread context class loader
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                return cl.loadClass("Solution");
            }
            throw e;
        }
    }

    public static Object createInstance(Class<?> solutionClass) {
        try {
            Constructor<?> constructor = solutionClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                "Solution class must have a no-argument constructor (default constructor).", e
            );
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(
                "Failed to instantiate Solution class: " + e.getMessage(), e
            );
        }
    }
}
