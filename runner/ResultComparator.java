package runner;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Robust recursive comparator for test results and expected values.
 */
public class ResultComparator {

    public static final double DEFAULT_FLOAT_TOLERANCE = 1e-6;

    public static boolean compare(Object expected, Object actual) {
        return compare(expected, actual, DEFAULT_FLOAT_TOLERANCE);
    }

    public static boolean compare(Object expected, Object actual, double floatTolerance) {
        if (expected == null && actual == null) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }

        // Both numbers (e.g. Integer vs Long, Float vs Double)
        if (expected instanceof Number && actual instanceof Number) {
            Number nExp = (Number) expected;
            Number nAct = (Number) actual;
            if (isFloatingPoint(nExp) || isFloatingPoint(nAct)) {
                return Math.abs(nExp.doubleValue() - nAct.doubleValue()) <= floatTolerance;
            }
            return nExp.longValue() == nAct.longValue();
        }

        // Characters vs Strings of length 1
        if (expected instanceof Character && actual instanceof Character) {
            return expected.equals(actual);
        }
        if (expected instanceof Character && actual instanceof String) {
            return ((String) actual).length() == 1 && ((String) actual).charAt(0) == (Character) expected;
        }
        if (expected instanceof String && actual instanceof Character) {
            return ((String) expected).length() == 1 && ((String) expected).charAt(0) == (Character) actual;
        }

        // Booleans
        if (expected instanceof Boolean && actual instanceof Boolean) {
            return expected.equals(actual);
        }

        // Strings
        if (expected instanceof String && actual instanceof String) {
            return expected.equals(actual);
        }

        // ListNode comparison
        if (isListNode(expected) && isListNode(actual)) {
            return compareListNodes(expected, actual);
        }

        // TreeNode comparison
        if (isTreeNode(expected) && isTreeNode(actual)) {
            return compareTreeNodes(expected, actual);
        }

        // Sequence types: Array or List
        if (isSequence(expected) && isSequence(actual)) {
            return compareSequences(expected, actual, floatTolerance);
        }

        // Fallback to standard equals
        return Objects.equals(expected, actual);
    }

    private static boolean isFloatingPoint(Number n) {
        return n instanceof Double || n instanceof Float;
    }

    private static boolean isSequence(Object obj) {
        return obj != null && (obj.getClass().isArray() || obj instanceof List);
    }

    private static int getSequenceLength(Object seq) {
        if (seq.getClass().isArray()) {
            return Array.getLength(seq);
        }
        return ((List<?>) seq).size();
    }

    private static Object getSequenceElement(Object seq, int index) {
        if (seq.getClass().isArray()) {
            return Array.get(seq, index);
        }
        return ((List<?>) seq).get(index);
    }

    private static boolean compareSequences(Object seq1, Object seq2, double tolerance) {
        int len1 = getSequenceLength(seq1);
        int len2 = getSequenceLength(seq2);
        if (len1 != len2) {
            return false;
        }
        for (int i = 0; i < len1; i++) {
            Object el1 = getSequenceElement(seq1, i);
            Object el2 = getSequenceElement(seq2, i);
            if (!compare(el1, el2, tolerance)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isListNode(Object obj) {
        return obj != null && "ListNode".equals(obj.getClass().getSimpleName());
    }

    private static boolean isTreeNode(Object obj) {
        return obj != null && "TreeNode".equals(obj.getClass().getSimpleName());
    }

    private static boolean compareListNodes(Object n1, Object n2) {
        Object curr1 = n1;
        Object curr2 = n2;
        int maxSteps = 10000;
        int steps = 0;

        while (curr1 != null && curr2 != null && steps++ < maxSteps) {
            try {
                Field val1 = curr1.getClass().getField("val");
                Field val2 = curr2.getClass().getField("val");
                if (val1.getInt(curr1) != val2.getInt(curr2)) {
                    return false;
                }
                Field next1 = curr1.getClass().getField("next");
                Field next2 = curr2.getClass().getField("next");
                curr1 = next1.get(curr1);
                curr2 = next2.get(curr2);
            } catch (Exception e) {
                return false;
            }
        }
        return curr1 == null && curr2 == null;
    }

    private static boolean compareTreeNodes(Object t1, Object t2) {
        if (t1 == null && t2 == null) return true;
        if (t1 == null || t2 == null) return false;

        try {
            Field val1 = t1.getClass().getField("val");
            Field val2 = t2.getClass().getField("val");
            if (val1.getInt(t1) != val2.getInt(t2)) return false;

            Field left1 = t1.getClass().getField("left");
            Field left2 = t2.getClass().getField("left");
            Field right1 = t1.getClass().getField("right");
            Field right2 = t2.getClass().getField("right");

            return compareTreeNodes(left1.get(t1), left2.get(t2)) &&
                   compareTreeNodes(right1.get(t1), right2.get(t2));
        } catch (Exception e) {
            return false;
        }
    }

    public static String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        if (value instanceof Character) {
            return "'" + value + "'";
        }
        if (value.getClass().isArray()) {
            StringBuilder sb = new StringBuilder("[");
            int len = Array.getLength(value);
            for (int i = 0; i < len; i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatValue(Array.get(value, i)));
            }
            sb.append("]");
            return sb.toString();
        }
        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatValue(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        if (isListNode(value)) {
            return formatListNode(value);
        }
        if (isTreeNode(value)) {
            return formatTreeNode(value);
        }
        return String.valueOf(value);
    }

    private static String formatListNode(Object head) {
        List<String> items = new ArrayList<>();
        Object curr = head;
        int max = 20;
        int count = 0;
        while (curr != null && count++ < max) {
            try {
                items.add(String.valueOf(curr.getClass().getField("val").get(curr)));
                curr = curr.getClass().getField("next").get(curr);
            } catch (Exception e) {
                break;
            }
        }
        if (curr != null) items.add("...");
        return "[" + String.join(", ", items) + "]";
    }

    private static String formatTreeNode(Object root) {
        if (root == null) return "[]";
        List<String> items = new ArrayList<>();
        Queue<Object> q = new LinkedList<>();
        q.offer(root);

        while (!q.isEmpty()) {
            Object node = q.poll();
            if (node == null) {
                items.add("null");
            } else {
                try {
                    items.add(String.valueOf(node.getClass().getField("val").get(node)));
                    q.offer(node.getClass().getField("left").get(node));
                    q.offer(node.getClass().getField("right").get(node));
                } catch (Exception e) {
                    break;
                }
            }
        }

        // Trim trailing nulls
        int lastNonNull = items.size() - 1;
        while (lastNonNull >= 0 && "null".equals(items.get(lastNonNull))) {
            lastNonNull--;
        }

        return "[" + String.join(", ", items.subList(0, lastNonNull + 1)) + "]";
    }
}
