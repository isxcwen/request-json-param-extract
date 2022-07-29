package resolve.utils;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ValueConstants;
import resolve.annotations.RequestBodyExtract;

import java.lang.reflect.Type;
import java.util.Map;

public class ArgumentResolveUtils {
    public static Object extractParam(MethodParameter methodParameter, Map<String, Object> data){
        RequestBodyExtract parameterAnnotation = methodParameter.getParameterAnnotation(RequestBodyExtract.class);
        Assert.state(parameterAnnotation != null, "No RequestBodyExtract annotation");

        //获取要提取的参数名
        String name = parameterAnnotation.value();
        if(!StringUtils.hasLength(name)){
            name = parameterAnnotation.name();
        }
        if(!StringUtils.hasLength(name)){
            name = methodParameter.getParameterName();
        }

        boolean required = parameterAnnotation.required();
        String defaultValue = parameterAnnotation.defaultValue();

        return getValue(data, name, ResolvableType.forMethodParameter(methodParameter).getType(), required, defaultValue);
    }

    private static Object getValue(Map<String, Object> data, String name, Type type, boolean required, String defaultValue) {
        Object value = data.get(name);
        if(value != null){
            return JSONUtils.convertObject(JSONUtils.convertString(value), type);
        }else {
            if(required){
                Assert.state(!ValueConstants.DEFAULT_NONE.equals(defaultValue), "param " + name +" is required");
            }
            if(!ValueConstants.DEFAULT_NONE.equals(defaultValue)){
                return JSONUtils.convertObject(defaultValue, type);
            }

        }
        return null;
    }
}
