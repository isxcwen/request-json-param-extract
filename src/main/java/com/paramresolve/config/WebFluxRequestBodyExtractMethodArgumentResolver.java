package com.paramresolve.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.StringDecoder;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.annotation.AbstractMessageReaderArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

public class WebFluxRequestBodyExtractMethodArgumentResolver extends AbstractMessageReaderArgumentResolver {
    private static final String REQUEST_BODY_READED = "request-body-readed";
    private static final String REQUEST_BODY_CACHE = "request-body-cache";

    private MethodParameter stringMethodParameter;

    private ObjectMapper objectMapper;

    public WebFluxRequestBodyExtractMethodArgumentResolver() {
        super(Arrays.asList(new DecoderHttpMessageReader<>(StringDecoder.allMimeTypes())));
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestBodyExtract.class);
    }

    @Override
    public Mono<Object> resolveArgument(
            MethodParameter param, BindingContext bindingContext, ServerWebExchange exchange) {
        if(!isReaded(exchange)){
            cacheBody(exchange, readBody(bindingContext, exchange));
        }
        Mono<Map<String, Object>> cache = getCache(exchange);
        return cache.mapNotNull(data -> extractParam(param, data));
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

    private boolean isReaded(ServerWebExchange exchange){
        return exchange.getAttributeOrDefault(REQUEST_BODY_READED, false);
    }

    private Mono<Map<String, Object>> getCache(ServerWebExchange exchange){
        return exchange.getAttribute(REQUEST_BODY_CACHE);
    }

    /**
     * 读取请求体中的数据
     * @param bindingContext
     * @param exchange
     * @return
     */
    private Mono<Map<String, Object>> readBody(BindingContext bindingContext, ServerWebExchange exchange){
        return readBody(getStringMethodParameter(), true, bindingContext, exchange).cast(String.class).map(body -> {
            try {
                return getObjectMapper().readValue(body, Map.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * body缓存起来 多次使用
     * @param exchange
     * @param data
     */
    private void cacheBody(ServerWebExchange exchange, Mono<Map<String, Object>> data){
        exchange.getAttributes().put(REQUEST_BODY_READED, true);
        exchange.getAttributes().put(REQUEST_BODY_CACHE, data);
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
