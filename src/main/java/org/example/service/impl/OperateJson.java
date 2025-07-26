package org.example.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OperateJson {
    private static final Logger log = LoggerFactory.getLogger(OperateJson.class);
    // 缓存每个类的方法列表（提升性能）
    private static final Map<Class<?>, List<Method>> methodCache = new ConcurrentHashMap<>();

    public static Map<String, Object> exportToJson(Object obj) {
        if (obj == null) return Collections.emptyMap();

        Map<String, Object> fieldMap = new HashMap<>();
        Class<?> clazz = obj.getClass();

        // 获取缓存的方法列表
        List<Method> getters = methodCache.computeIfAbsent(clazz, c -> {
            List<Method> list = new ArrayList<>();
            for (Method m : c.getMethods()) {
                if ((m.getName().startsWith("get") || m.getName().startsWith("is"))
                        && m.getParameterCount() == 0 && !m.getName().equals("getClass")) {
                    list.add(m);
                }
            }
            return list;
        });

        for (Method method : getters) {
            try {
                Object value = method.invoke(obj);
                if (value == null) continue;

                String fieldName = getFieldName(method.getName());
                fieldMap.put(fieldName, value);
            } catch (Exception e) {
                log.error("反射转JSON失败: {} -> {}", method.getName(), e.getMessage(), e);
            }
        }

        return fieldMap;
    }

    private static String getFieldName(String methodName) {
        String raw = methodName.startsWith("get") ? methodName.substring(3)
                : methodName.startsWith("is") ? methodName.substring(2)
                : methodName;
        return Character.toLowerCase(raw.charAt(0)) + raw.substring(1);
    }
}
