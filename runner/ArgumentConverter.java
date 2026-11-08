package runner;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.*;
import java.util.*;

/**
 * Converts JSON elements to target Java types using Reflection and Gson.
 */
public class ArgumentConverter {

    private static final Gson gson = new Gson();

    public static Object convert(JsonElement element, Type targetType) {
        if (element == null || element.isJsonNull()) {
            if (targetType instanceof Class<?> && ((Class<?>) targetType).isPrimitive()) {
                throw new IllegalArgumentException("Cannot convert null to primitive type " + ((Class<?>) targetType).getName());
            }
            return null;
        }

        Class<?> rawClass = getRawClass(targetType);

        // Special handling for char and Character
        if (rawClass == char.class || rawClass == Character.class) {
            return convertToChar(element);
        }

        // Special handling for char[]
        if (rawClass == char[].class) {
            return convertToCharArray(element);
        }

        // Special handling for char[][]
        if (rawClass == char[][].class) {
            return convertToChar2DArray(element);
        }

        // Special handling for ListNode
        if (isListNodeClass(rawClass)) {
            return convertToListNode(element, rawClass);
        }

        // Special handling for TreeNode
        if (isTreeNodeClass(rawClass)) {
            return convertToTreeNode(element, rawClass);
        }

        // Array types
        if (rawClass.isArray()) {
            return convertToArray(element, rawClass);
        }

        // Fall back to Gson for standard types, primitives, wrappers, collections, maps
        try {
            return gson.fromJson(element, targetType);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Failed to convert JSON " + element + " to type " + targetType.getTypeName() + ": " + e.getMessage(), e
            );
        }
    }

    private static Character convertToChar(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.getAsJsonPrimitive();
            if (prim.isString()) {
                String s = prim.getAsString();
                if (!s.isEmpty()) return s.charAt(0);
            } else if (prim.isNumber()) {
                return (char) prim.getAsInt();
            }
        }
        throw new IllegalArgumentException("Cannot convert " + element + " to char.");
    }

    private static char[] convertToCharArray(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString().toCharArray();
        }
        if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            char[] result = new char[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                result[i] = convertToChar(arr.get(i));
            }
            return result;
        }
        throw new IllegalArgumentException("Cannot convert " + element + " to char[].");
    }

    private static char[][] convertToChar2DArray(JsonElement element) {
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException("Expected JSON array for char[][], got: " + element);
        }
        JsonArray outer = element.getAsJsonArray();
        char[][] result = new char[outer.size()][];
        for (int i = 0; i < outer.size(); i++) {
            result[i] = convertToCharArray(outer.get(i));
        }
        return result;
    }

    private static Object convertToArray(JsonElement element, Class<?> arrayClass) {
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException("Expected JSON array for type " + arrayClass.getSimpleName() + ", got: " + element);
        }

        JsonArray jsonArray = element.getAsJsonArray();
        Class<?> componentType = arrayClass.getComponentType();
        Object javaArray = Array.newInstance(componentType, jsonArray.size());

        for (int i = 0; i < jsonArray.size(); i++) {
            Object convertedElement = convert(jsonArray.get(i), componentType);
            Array.set(javaArray, i, convertedElement);
        }

        return javaArray;
    }

    private static boolean isListNodeClass(Class<?> clazz) {
        return clazz != null && "ListNode".equals(clazz.getSimpleName());
    }

    private static boolean isTreeNodeClass(Class<?> clazz) {
        return clazz != null && "TreeNode".equals(clazz.getSimpleName());
    }

    private static Object convertToListNode(JsonElement element, Class<?> listNodeClass) {
        if (element.isJsonNull()) return null;
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException("Expected JSON array to build ListNode, got: " + element);
        }

        JsonArray arr = element.getAsJsonArray();
        if (listNodeClass == ListNode.class) {
            int[] vals = new int[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                vals[i] = arr.get(i).getAsInt();
            }
            return ListNode.fromArray(vals);
        }

        // Reflection build for user-defined ListNode
        try {
            Constructor<?> valNextCtor = null;
            Constructor<?> valCtor = null;
            Constructor<?> noArgCtor = null;

            for (Constructor<?> c : listNodeClass.getConstructors()) {
                if (c.getParameterCount() == 2) valNextCtor = c;
                else if (c.getParameterCount() == 1) valCtor = c;
                else if (c.getParameterCount() == 0) noArgCtor = c;
            }

            Field valField = listNodeClass.getField("val");
            Field nextField = listNodeClass.getField("next");

            Object dummy = noArgCtor != null ? noArgCtor.newInstance() : listNodeClass.getDeclaredConstructor().newInstance();
            Object current = dummy;

            for (int i = 0; i < arr.size(); i++) {
                int val = arr.get(i).getAsInt();
                Object node;
                if (valCtor != null) {
                    node = valCtor.newInstance(val);
                } else {
                    node = listNodeClass.getDeclaredConstructor().newInstance();
                    valField.setInt(node, val);
                }
                nextField.set(current, node);
                current = node;
            }

            return nextField.get(dummy);
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct " + listNodeClass.getName() + " from JSON: " + e.getMessage(), e);
        }
    }

    private static Object convertToTreeNode(JsonElement element, Class<?> treeNodeClass) {
        if (element.isJsonNull()) return null;
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException("Expected JSON array to build TreeNode, got: " + element);
        }

        JsonArray arr = element.getAsJsonArray();
        List<Integer> vals = new ArrayList<>();
        for (JsonElement item : arr) {
            if (item.isJsonNull()) {
                vals.add(null);
            } else {
                vals.add(item.getAsInt());
            }
        }

        if (treeNodeClass == TreeNode.class) {
            return TreeNode.fromLevelOrder(vals);
        }

        // Reflection build for user-defined TreeNode
        if (vals.isEmpty() || vals.get(0) == null) return null;

        try {
            Field valField = treeNodeClass.getField("val");
            Field leftField = treeNodeClass.getField("left");
            Field rightField = treeNodeClass.getField("right");

            Constructor<?> valCtor = null;
            try {
                valCtor = treeNodeClass.getConstructor(int.class);
            } catch (NoSuchMethodException ignored) {}

            Object root = instantiateTreeNode(treeNodeClass, valCtor, valField, vals.get(0));
            Queue<Object> queue = new LinkedList<>();
            queue.offer(root);

            int i = 1;
            while (!queue.isEmpty() && i < vals.size()) {
                Object current = queue.poll();

                if (i < vals.size()) {
                    Integer leftVal = vals.get(i++);
                    if (leftVal != null) {
                        Object leftNode = instantiateTreeNode(treeNodeClass, valCtor, valField, leftVal);
                        leftField.set(current, leftNode);
                        queue.offer(leftNode);
                    }
                }

                if (i < vals.size()) {
                    Integer rightVal = vals.get(i++);
                    if (rightVal != null) {
                        Object rightNode = instantiateTreeNode(treeNodeClass, valCtor, valField, rightVal);
                        rightField.set(current, rightNode);
                        queue.offer(rightNode);
                    }
                }
            }

            return root;
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct " + treeNodeClass.getName() + " from JSON: " + e.getMessage(), e);
        }
    }

    private static Object instantiateTreeNode(Class<?> treeNodeClass, Constructor<?> valCtor, Field valField, int val) throws Exception {
        if (valCtor != null) {
            return valCtor.newInstance(val);
        }
        Object node = treeNodeClass.getDeclaredConstructor().newInstance();
        valField.setInt(node, val);
        return node;
    }

    public static Class<?> getRawClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> rawComponentClass = getRawClass(componentType);
            return Array.newInstance(rawComponentClass, 0).getClass();
        }
        return Object.class;
    }
}
