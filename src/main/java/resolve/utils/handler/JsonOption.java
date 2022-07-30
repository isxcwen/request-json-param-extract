package resolve.utils.handler;

import java.lang.reflect.Type;

public interface JsonOption {
    String convertString(Object content);
    Object convertObject(String content, Type type);
    <T> T convertObject(String content, Class<T> clazz);
}
