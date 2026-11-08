package runner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Represents a single test case loaded from testcases.json.
 */
public class TestCase {
    private final int index;
    private final String name;
    private final JsonArray rawInput;
    private final JsonElement rawExpected;
    private final boolean hasExpected;

    public TestCase(int index, String name, JsonArray rawInput, JsonElement rawExpected, boolean hasExpected) {
        this.index = index;
        this.name = name;
        this.rawInput = rawInput;
        this.rawExpected = rawExpected;
        this.hasExpected = hasExpected;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public JsonArray getRawInput() {
        return rawInput;
    }

    public JsonElement getRawExpected() {
        return rawExpected;
    }

    public boolean hasExpected() {
        return hasExpected;
    }
}
