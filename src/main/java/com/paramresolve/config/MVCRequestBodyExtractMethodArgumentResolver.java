package com.paramresolve.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

public class MVCRequestBodyExtractMethodArgumentResolver extends AbstractMessageConverterMethodArgumentResolver {
    private static final String REQUEST_BODY_READED = "request-body-readed";
    private static final String REQUEST_BODY_CACHE = "request-body-cache";

    private MethodParameter stringMethodParameter;

    private ObjectMapper objectMapper;

    public MVCRequestBodyExtractMethodArgumentResolver() {
        super(Arrays.asList(new StringHttpMessageConverter(Charset.defaultCharset())));
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestBodyExtract.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        if(!isReaded(webRequest)){
            cacheBody(webRequest, readBody(webRequest, parameter));
        }
        Map<String, Object> cache = getCache(webRequest);
        return extractParam(parameter, cache);
    }

    protected <T> Object readWithMessageConverters(NativeWebRequest webRequest, MethodParameter parameter,
                                                   Type paramType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {

        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(servletRequest);
        return readWithMessageConverters(inputMessage, parameter, paramType);
    }

    private Object extractParam(MethodParameter methodParameter, Map<String, Object> data){
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

    private Object getValue(Map<String, Object> data, String name, Type type, boolean required, String defaultValue) {
        Object value = data.get(name);
        if(value != null){
            return mapperConvertObject(mapperConvertString(value), type);
        }else {
            if(required){
                Assert.state(!ValueConstants.DEFAULT_NONE.equals(defaultValue), "param " + name +" is required");
            }
            if(!ValueConstants.DEFAULT_NONE.equals(defaultValue)){
                return mapperConvertObject(defaultValue, type);
            }

        }
        return null;
    }

    private String mapperConvertString(Object content){
        try {
            return getObjectMapper().writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Object mapperConvertObject(String content, Type type){
        JavaType javaType = getObjectMapper().constructType(type);
        try {
            return getObjectMapper().readValue(content, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isReaded(NativeWebRequest webRequest){
        Object attribute = webRequest.getAttribute(REQUEST_BODY_READED, RequestAttributes.SCOPE_REQUEST);
        return attribute == null ? false : (Boolean) attribute;
    }

    private Map<String, Object> getCache(NativeWebRequest webRequest){
        return (Map<String, Object>)webRequest.getAttribute(REQUEST_BODY_CACHE, RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 读取请求体中的数据
     * @param webRequest
     * @param parameter
     * @return
     */
    private Map<String, Object> readBody(NativeWebRequest webRequest, MethodParameter parameter) throws HttpMediaTypeNotSupportedException, IOException {
        String body = (String) readWithMessageConverters(webRequest, parameter, String.class);
        try {
            return getObjectMapper().readValue(body, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * body缓存起来 多次使用
     * @param webRequest
     * @param data
     */
    private void cacheBody(NativeWebRequest webRequest, Map<String, Object> data){
        webRequest.setAttribute(REQUEST_BODY_READED, true, RequestAttributes.SCOPE_REQUEST);
        webRequest.setAttribute(REQUEST_BODY_CACHE, data, RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 获取json操作对象
     * @return
     */
    public ObjectMapper getObjectMapper() {
        if(this.objectMapper == null){
            ObjectMapper objectMapper = new ObjectMapper();
            //true - 遇到没有的属性就报错 false - 没有的属性不会管，不会报错
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //如果是空对象的时候,不抛异常
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            this.objectMapper = objectMapper;
        }
        return objectMapper;
    }

    /**
     * 获取String类型请求体
     * @return
     */
    private MethodParameter getStringMethodParameter(){
        if(this.stringMethodParameter == null){
            try {
                Method stringMethod = this.getClass().getDeclaredMethod("stringMethod", String.class);
                stringMethod.setAccessible(true);
                this.stringMethodParameter = new MethodParameter(stringMethod, 0);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return stringMethodParameter;
    }
    private void stringMethod(String body){}
}
