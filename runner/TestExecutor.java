package runner;

import com.google.gson.JsonArray;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes test cases against the Solution class using Reflection.
 */
public class TestExecutor {

    public static List<TestResult> execute(
        Class<?> solutionClass,
        MethodResolver.ResolvedMethod resolvedMethod,
        TestSuite testSuite
    ) {
        List<TestResult> results = new ArrayList<>();

        for (TestCase tc : testSuite.getTestCases()) {
            results.add(executeTestCase(solutionClass, resolvedMethod, tc));
        }

        return results;
    }

    private static TestResult executeTestCase(
        Class<?> solutionClass,
        MethodResolver.ResolvedMethod resolvedMethod,
        TestCase tc
    ) {
        String formattedInput = formatRawInput(tc.getRawInput());
        Type[] paramTypes = resolvedMethod.getGenericParameterTypes();
        Class<?>[] rawParamTypes = resolvedMethod.getParameterTypes();
        Class<?> returnType = resolvedMethod.getReturnType();
        Type genericReturnType = resolvedMethod.getGenericReturnType();

        Object[] javaArgs;
        try {
            javaArgs = new Object[paramTypes.length];
            JsonArray inputArr = tc.getRawInput();
            for (int i = 0; i < paramTypes.length; i++) {
                javaArgs[i] = ArgumentConverter.convert(inputArr.get(i), paramTypes[i]);
            }
        } catch (Exception e) {
            return new TestResult(
                tc,
                TestResult.Status.ERROR,
                0.0,
                formattedInput,
                ResultComparator.formatValue(null),
                "Error converting input: " + e.getMessage(),
                e,
                null
            );
        }

        // Convert expected output
        Object expectedObj = null;
        if (tc.hasExpected()) {
            try {
                Type expectedType = (returnType == void.class && paramTypes.length > 0)
                    ? paramTypes[0]
                    : genericReturnType;
                expectedObj = ArgumentConverter.convert(tc.getRawExpected(), expectedType);
            } catch (Exception e) {
                return new TestResult(
                    tc,
                    TestResult.Status.ERROR,
                    0.0,
                    formattedInput,
                    "Invalid expected value in JSON: " + e.getMessage(),
                    "",
                    e,
                    null
                );
            }
        }
        String formattedExpected = ResultComparator.formatValue(expectedObj);

        // Instantiate Solution class (fresh instance for isolation)
        Object instance = null;
        if (!resolvedMethod.isStatic()) {
            try {
                instance = SolutionLoader.createInstance(solutionClass);
            } catch (Exception e) {
                return new TestResult(
                    tc,
                    TestResult.Status.ERROR,
                    0.0,
                    formattedInput,
                    formattedExpected,
                    "Failed to create Solution instance: " + e.getMessage(),
                    e,
                    null
                );
            }
        }

        // Measure execution time & invoke method
        long start = System.nanoTime();
        Object actualObj = null;
        Throwable thrown = null;
        String errorLocation = null;

        try {
            actualObj = resolvedMethod.getMethod().invoke(instance, javaArgs);
            if (returnType == void.class && javaArgs.length > 0) {
                // In-place modification: take the first modified argument
                actualObj = javaArgs[0];
            }
        } catch (InvocationTargetException ite) {
            thrown = ite.getCause() != null ? ite.getCause() : ite;
            errorLocation = findSolutionStackTraceLocation(thrown);
        } catch (Throwable t) {
            thrown = t;
            errorLocation = findSolutionStackTraceLocation(t);
        }
        long end = System.nanoTime();
        double durationMs = (end - start) / 1_000_000.0;

        if (thrown != null) {
            String actualMessage = thrown.getClass().getName();
            if (thrown.getMessage() != null && !thrown.getMessage().isEmpty()) {
                actualMessage += ": " + thrown.getMessage();
            }
            return new TestResult(
                tc,
                TestResult.Status.ERROR,
                durationMs,
                formattedInput,
                formattedExpected,
                actualMessage,
                thrown,
                errorLocation
            );
        }

        String formattedActual = ResultComparator.formatValue(actualObj);
        boolean passed = ResultComparator.compare(expectedObj, actualObj);

        return new TestResult(
            tc,
            passed ? TestResult.Status.PASS : TestResult.Status.FAIL,
            durationMs,
            formattedInput,
            formattedExpected,
            formattedActual,
            null,
            null
        );
    }

    private static String formatRawInput(JsonArray inputArr) {
        if (inputArr == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < inputArr.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(inputArr.get(i).toString());
        }
        sb.append("]");
        return sb.toString();
    }

    private static String findSolutionStackTraceLocation(Throwable t) {
        if (t == null) return null;
        for (StackTraceElement ste : t.getStackTrace()) {
            if ("Solution.java".equals(ste.getFileName()) || "Solution".equals(ste.getClassName())) {
                return "Solution.java:" + ste.getLineNumber() + " in " + ste.getMethodName() + "()";
            }
        }
        return null;
    }
}
