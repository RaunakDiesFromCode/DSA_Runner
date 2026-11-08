package runner;

import com.google.gson.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses testcases.json into a structured TestSuite.
 */
public class JsonParser {

    public static TestSuite parse(Path jsonPath) throws IOException {
        if (!Files.exists(jsonPath)) {
            throw new IllegalArgumentException("Test cases file not found: " + jsonPath.toAbsolutePath());
        }

        String content = Files.readString(jsonPath).trim();
        if (content.isEmpty()) {
            throw new IllegalArgumentException("Test cases file is empty: " + jsonPath.getFileName());
        }

        JsonElement rootElement;
        try {
            rootElement = com.google.gson.JsonParser.parseString(content);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON in " + jsonPath.getFileName() + ": " + e.getMessage(), e);
        }

        String method = null;
        String problem = null;
        JsonArray testCasesArray = null;

        if (rootElement.isJsonObject()) {
            JsonObject rootObj = rootElement.getAsJsonObject();

            if (rootObj.has("method") && !rootObj.get("method").isJsonNull()) {
                method = rootObj.get("method").getAsString().trim();
            }

            if (rootObj.has("problem") && !rootObj.get("problem").isJsonNull()) {
                problem = rootObj.get("problem").getAsString().trim();
            } else if (rootObj.has("name") && !rootObj.get("name").isJsonNull() && !rootObj.has("testCases")) {
                problem = rootObj.get("name").getAsString().trim();
            }

            // Find array under testCases, cases, or tests
            if (rootObj.has("testCases") && rootObj.get("testCases").isJsonArray()) {
                testCasesArray = rootObj.getAsJsonArray("testCases");
            } else if (rootObj.has("cases") && rootObj.get("cases").isJsonArray()) {
                testCasesArray = rootObj.getAsJsonArray("cases");
            } else if (rootObj.has("tests") && rootObj.get("tests").isJsonArray()) {
                testCasesArray = rootObj.getAsJsonArray("tests");
            } else {
                throw new IllegalArgumentException(
                    "Missing 'testCases' array in " + jsonPath.getFileName() +
                    ". Expected format: {\"method\": \"...\", \"testCases\": [...]}"
                );
            }
        } else if (rootElement.isJsonArray()) {
            testCasesArray = rootElement.getAsJsonArray();
        } else {
            throw new IllegalArgumentException("Root of " + jsonPath.getFileName() + " must be a JSON object or array.");
        }

        if (testCasesArray.isEmpty()) {
            throw new IllegalArgumentException("The test cases array in " + jsonPath.getFileName() + " is empty.");
        }

        List<TestCase> testCases = new ArrayList<>();
        int testIndex = 1;

        for (JsonElement item : testCasesArray) {
            if (!item.isJsonObject()) {
                throw new IllegalArgumentException(
                    "Test case #" + testIndex + " must be a JSON object containing 'input' and 'expected'."
                );
            }

            JsonObject tcObj = item.getAsJsonObject();

            // Name (optional)
            String name = null;
            if (tcObj.has("name") && !tcObj.get("name").isJsonNull()) {
                name = tcObj.get("name").getAsString().trim();
            }

            // Input
            if (!tcObj.has("input")) {
                String desc = name != null ? " ('" + name + "')" : "";
                throw new IllegalArgumentException("Test case #" + testIndex + desc + " is missing 'input' field.");
            }
            JsonElement inputElement = tcObj.get("input");
            JsonArray rawInput;
            if (inputElement.isJsonArray()) {
                rawInput = inputElement.getAsJsonArray();
            } else {
                // Single input argument wrapped as 1-element array
                rawInput = new JsonArray();
                rawInput.add(inputElement);
            }

            // Expected
            boolean hasExpected = tcObj.has("expected") || tcObj.has("output");
            JsonElement rawExpected = null;
            if (tcObj.has("expected")) {
                rawExpected = tcObj.get("expected");
            } else if (tcObj.has("output")) {
                rawExpected = tcObj.get("output");
            }

            testCases.add(new TestCase(testIndex, name, rawInput, rawExpected, hasExpected));
            testIndex++;
        }

        return new TestSuite(method, problem, testCases);
    }
}
