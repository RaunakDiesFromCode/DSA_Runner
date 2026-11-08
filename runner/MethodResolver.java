package runner;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resolves the target solution method from the Solution class using Reflection.
 */
public class MethodResolver {

    public static class ResolvedMethod {
        private final Method method;
        private final String signatureString;

        public ResolvedMethod(Method method, String signatureString) {
            this.method = method;
            this.signatureString = signatureString;
        }

        public Method getMethod() {
            return method;
        }

        public Class<?>[] getParameterTypes() {
            return method.getParameterTypes();
        }

        public Type[] getGenericParameterTypes() {
            return method.getGenericParameterTypes();
        }

        public Class<?> getReturnType() {
            return method.getReturnType();
        }

        public Type getGenericReturnType() {
            return method.getGenericReturnType();
        }

        public String getSignatureString() {
            return signatureString;
        }

        public boolean isStatic() {
            return Modifier.isStatic(method.getModifiers());
        }
    }

    private static final Set<String> OBJECT_METHODS = Set.of(
        "equals", "hashCode", "toString", "getClass", "notify", "notifyAll", "wait", "clone", "finalize"
    );

    public static ResolvedMethod resolve(Class<?> solutionClass, String specifiedMethodName, int argumentCount) {
        // Collect candidate methods declared in Solution or inherited public methods
        List<Method> candidates = Arrays.stream(solutionClass.getMethods())
            .filter(m -> !OBJECT_METHODS.contains(m.getName()))
            .filter(m -> !m.isSynthetic() && !m.isBridge())
            .filter(m -> Modifier.isPublic(m.getModifiers()))
            .filter(m -> m.getDeclaringClass() != Object.class)
            .collect(Collectors.toList());

        // Also check declared public or non-private methods in Solution class directly
        for (Method m : solutionClass.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && !m.isSynthetic() && !m.isBridge() && !candidates.contains(m)) {
                candidates.add(m);
            }
        }

        if (candidates.isEmpty()) {
            throw new IllegalStateException(
                "No public method found in class " + solutionClass.getSimpleName() +
                ". Ensure your solution method is declared as 'public'."
            );
        }

        // Case 1: Specific method name provided in testcases.json
        if (specifiedMethodName != null && !specifiedMethodName.isEmpty()) {
            List<Method> matchingName = candidates.stream()
                .filter(m -> m.getName().equals(specifiedMethodName))
                .collect(Collectors.toList());

            if (matchingName.isEmpty()) {
                String available = candidates.stream()
                    .map(MethodResolver::formatSignature)
                    .collect(Collectors.joining(", "));
                throw new IllegalArgumentException(
                    "Method '" + specifiedMethodName + "' not found in class " + solutionClass.getSimpleName() +
                    ". Available public methods: [" + available + "]"
                );
            }

            if (matchingName.size() == 1) {
                Method m = matchingName.get(0);
                validateParameterCount(m, argumentCount);
                m.setAccessible(true);
                return new ResolvedMethod(m, formatSignature(m));
            }

            // Multiple overloads with the same name - filter by argument count
            List<Method> matchingArity = matchingName.stream()
                .filter(m -> m.getParameterCount() == argumentCount)
                .collect(Collectors.toList());

            if (matchingArity.size() == 1) {
                Method m = matchingArity.get(0);
                m.setAccessible(true);
                return new ResolvedMethod(m, formatSignature(m));
            }

            if (matchingArity.isEmpty()) {
                String overloads = matchingName.stream()
                    .map(MethodResolver::formatSignature)
                    .collect(Collectors.joining(", "));
                throw new IllegalArgumentException(
                    "Found method '" + specifiedMethodName + "' but none accept " + argumentCount +
                    " argument(s). Available overloads: [" + overloads + "]"
                );
            }

            String ambiguousOverloads = matchingArity.stream()
                .map(MethodResolver::formatSignature)
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                "Ambiguous overloads for method '" + specifiedMethodName + "' with " + argumentCount +
                " argument(s): [" + ambiguousOverloads + "]"
            );
        }

        // Case 2: Method name NOT provided - auto-discovery
        if (candidates.size() == 1) {
            Method m = candidates.get(0);
            validateParameterCount(m, argumentCount);
            m.setAccessible(true);
            return new ResolvedMethod(m, formatSignature(m));
        }

        // Multiple public methods exist - attempt disambiguation by parameter count
        List<Method> matchingArity = candidates.stream()
            .filter(m -> m.getParameterCount() == argumentCount)
            .collect(Collectors.toList());

        if (matchingArity.size() == 1) {
            Method m = matchingArity.get(0);
            m.setAccessible(true);
            return new ResolvedMethod(m, formatSignature(m));
        }

        if (matchingArity.isEmpty()) {
            String available = candidates.stream()
                .map(MethodResolver::formatSignature)
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                "None of the public methods in " + solutionClass.getSimpleName() +
                " accept " + argumentCount + " argument(s). Available methods: [" + available + "]"
            );
        }

        // Still ambiguous: multiple methods accept argumentCount
        String candidateSignatures = matchingArity.stream()
            .map(MethodResolver::formatSignature)
            .collect(Collectors.joining(", "));
        throw new IllegalArgumentException(
            "Multiple candidate methods found in " + solutionClass.getSimpleName() +
            " matching " + argumentCount + " argument(s): [" + candidateSignatures + "].\n" +
            "To resolve this ambiguity, specify the target method in testcases.json:\n" +
            "  {\n" +
            "    \"method\": \"" + matchingArity.get(0).getName() + "\",\n" +
            "    \"testCases\": [ ... ]\n" +
            "  }"
        );
    }

    private static void validateParameterCount(Method method, int argumentCount) {
        if (method.getParameterCount() != argumentCount) {
            throw new IllegalArgumentException(
                "Method " + formatSignature(method) + " expects " + method.getParameterCount() +
                " argument(s), but test case provides " + argumentCount + "."
            );
        }
    }

    public static String formatSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getName()).append("(");
        Class<?>[] params = method.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(formatTypeName(params[i]));
        }
        sb.append(")");
        return sb.toString();
    }

    public static String formatTypeName(Class<?> type) {
        if (type.isArray()) {
            return formatTypeName(type.getComponentType()) + "[]";
        }
        return type.getSimpleName();
    }
}
