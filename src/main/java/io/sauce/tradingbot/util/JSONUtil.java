package io.sauce.tradingbot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.*;

public class JSONUtil {

    @Getter
    private static ObjectMapper objectMapper;

    public static void setObjectMapper(ObjectMapper objectMapper){
        JSONUtil.objectMapper = objectMapper;
    }
    public static String toJsonString(Object obj) {
        if (Objects.isNull(obj)){
            return null;
        }
        if (obj instanceof String){
            return (String) obj;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readToEntity(String json, Class<T> cls) {
        if (Objects.isNull(json)){
            return null;
        }
        if (cls == String.class){
            return (T)json;
        }
        try {
            return objectMapper.readValue(json,cls);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readToEntity(String json, JavaType javaType) {
        try {
            return objectMapper.readValue(json,javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> ArrayList<T> readToList(String json, Class<T> cls)  {
        if (Objects.isNull(json)){
            return null;
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructParametricType(ArrayList.class,cls));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <K,V> HashMap<K,V> readToHashMap(String json, Class<K> kcls, Class<V> vcls)  {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructParametricType(HashMap.class,kcls,vcls));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
