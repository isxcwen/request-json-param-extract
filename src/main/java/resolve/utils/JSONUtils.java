package resolve.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.lang.reflect.Type;

public class JSONUtils {
    private static ObjectMapper objectMapper;

    /**
     * 获取json操作对象
     * @return
     */
    private static ObjectMapper getObjectMapper() {
        if(JSONUtils.objectMapper == null){
            JSONUtils.objectMapper = init();
        }
        return JSONUtils.objectMapper;
    }

    private static ObjectMapper init(){
        ObjectMapper objectMapper = new ObjectMapper();
        //true - 遇到没有的属性就报错 false - 没有的属性不会管，不会报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //如果是空对象的时候,不抛异常
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }

    public static String convertString(Object content){
        try {
            return getObjectMapper().writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object convertObject(String content, Type type){
        JavaType javaType = getObjectMapper().constructType(type);
        try {
            return getObjectMapper().readValue(content, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T convertObject(String content, Class<T> clazz){
        try {
            return getObjectMapper().readValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
