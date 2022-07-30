package resolve.utils.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.lang.reflect.Type;

public class JacksonOption implements JsonOption {
    private ObjectMapper objectMapper;

    private ObjectMapper getObjectMapper() {
        if(this.objectMapper == null){
            this.objectMapper = init();
        }
        return this.objectMapper;
    }

    private static ObjectMapper init(){
        ObjectMapper objectMapper = new ObjectMapper();
        //true - 遇到没有的属性就报错 false - 没有的属性不会管，不会报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //如果是空对象的时候,不抛异常
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }

    @Override
    public String convertString(Object content){
        try {
            return getObjectMapper().writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object convertObject(String content, Type type){
        if(type instanceof Class && ((Class<?>) type).isAssignableFrom(String.class)){
            return content;
        }
        JavaType javaType = getObjectMapper().constructType(type);
        try {
            return getObjectMapper().readValue(content, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T convertObject(String content, Class<T> clazz){
        try {
            return getObjectMapper().readValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
