package resolve.config.mvc;

import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;
import resolve.annotations.RequestBodyExtract;
import resolve.constant.Constants;
import resolve.utils.ArgumentResolveUtils;
import resolve.utils.JSONUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

public class MVCRequestBodyExtractMethodArgumentResolver extends AbstractMessageConverterMethodArgumentResolver {
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
        return ArgumentResolveUtils.extractParam(parameter, cache);
    }

    protected <T> Object readWithMessageConverters(NativeWebRequest webRequest, MethodParameter parameter,
                                                   Type paramType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {

        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(servletRequest);
        return readWithMessageConverters(inputMessage, parameter, paramType);
    }

    private boolean isReaded(NativeWebRequest webRequest){
        Object attribute = webRequest.getAttribute(Constants.REQUEST_BODY_READED, RequestAttributes.SCOPE_REQUEST);
        return attribute == null ? false : (Boolean) attribute;
    }

    private Map<String, Object> getCache(NativeWebRequest webRequest){
        return (Map<String, Object>)webRequest.getAttribute(Constants.REQUEST_BODY_CACHE, RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 读取请求体中的数据
     * @param webRequest
     * @param parameter
     * @return
     */
    private Map<String, Object> readBody(NativeWebRequest webRequest, MethodParameter parameter) throws HttpMediaTypeNotSupportedException, IOException {
        String body = (String) readWithMessageConverters(webRequest, parameter, String.class);
        return JSONUtils.convertObject(body, Map.class);
    }

    /**
     * body缓存起来 多次使用
     * @param webRequest
     * @param data
     */
    private void cacheBody(NativeWebRequest webRequest, Map<String, Object> data){
        webRequest.setAttribute(Constants.REQUEST_BODY_READED, true, RequestAttributes.SCOPE_REQUEST);
        webRequest.setAttribute(Constants.REQUEST_BODY_CACHE, data, RequestAttributes.SCOPE_REQUEST);
    }
}
