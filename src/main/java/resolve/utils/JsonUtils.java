package resolve.utils;

import resolve.utils.handler.JsonOption;

import java.lang.reflect.Type;

public class JsonUtils {
    public static boolean inited = false;
    public static JsonOption jsonOption;

    public static void setJsonOption(JsonOption jsonOption) {
        JsonUtils.jsonOption = jsonOption;
        inited = true;
    }

    public static String convertString(Object content) {
        mustInit();
        return jsonOption.convertString(content);
    }

    public static Object convertObject(String content, Type type) {
        mustInit();
        return jsonOption.convertObject(content, type);
    }

    public static <T> T convertObject(String content, Class<T> clazz) {
        mustInit();
        return jsonOption.convertObject(content, clazz);
    }

    public static void mustInit(){
        if(!inited){
            throw new RuntimeException("没有配置json序列化工具");
        }
    }
}
