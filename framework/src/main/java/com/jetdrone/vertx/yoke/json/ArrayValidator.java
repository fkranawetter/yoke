package com.jetdrone.vertx.yoke.json;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ArrayValidator {

    public static boolean isValid(Object instance, JsonSchemaResolver.Schema schema) {
        if (!isArray(instance)) {
            return false;
        }

        List<Object> array = (List<Object>) instance;

        // validate additionalItems
        Boolean additionalItems = schema.get("additionalItems");

        if (additionalItems != null && !additionalItems) {
            List<Object> items = schema.get("items");
            if (array.size() > items.size()) {
                return false;
            }
        }

        // validate maxItems
        Integer maxItems = schema.get("maxItems");

        if (maxItems != null) {
            if (array == null || array.size() > maxItems) {
                return false;
            }
        }

        // validate minItems
        Integer minItems = schema.get("minItems");

        if (minItems != null) {
            if (array == null || array.size() < minItems) {
                return false;
            }
        }

        // validate uniqueItems
        Boolean uniqueItems = schema.get("uniqueItems");

        if (uniqueItems != null && uniqueItems) {
            if (array == null) {
                return false;
            }

            Set<Object> set = new HashSet<>();

            for (Object o : array) {
                if (!set.add(o)) {
                    return false;
                }
            }

            set.clear();
        }

        if (array != null) {
            Object items = schema.get("items");
            JsonSchemaResolver.Schema itemsSchema = null;

            if (items instanceof JsonSchemaResolver.Schema) {
                itemsSchema = (JsonSchemaResolver.Schema) items;
            } else {
                if (items instanceof Map) {
                    // convert to schema
                    itemsSchema = new JsonSchemaResolver.Schema((Map<String, Object>) items);
                    schema.put("items", itemsSchema);
                }
            }

            setParentIfNotNull(itemsSchema, schema);

            for (Object item : array) {
                if (!JsonSchema.conformsSchema(item, itemsSchema)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isArray(Object value) {
        return value == null || value instanceof List;
    }

    private static void setParentIfNotNull(JsonSchemaResolver.Schema schema, JsonSchemaResolver.Schema parent) {
        if (schema != null) {
            schema.setParent(parent);
        }
    }
}