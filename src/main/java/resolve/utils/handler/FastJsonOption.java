package resolve.utils.handler;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

public class FastJsonOption implements JsonOption {
    @Override
    public String convertString(Object content) {
        return JSONObject.toJSONString(content);
    }

    @Override
    public Object convertObject(String content, Type type) {
        if(type instanceof Class && ((Class<?>) type).isAssignableFrom(String.class)){
            return content;
        }
        return JSONObject.parseObject(content, type);
    }

    @Override
    public <T> T convertObject(String content, Class<T> clazz) {
        return JSONObject.parseObject(content, clazz);
    }
}
