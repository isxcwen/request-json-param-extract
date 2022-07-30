package resolve.config.webflux;

import org.springframework.core.MethodParameter;
import org.springframework.core.codec.StringDecoder;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.annotation.AbstractMessageReaderArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import resolve.annotations.RequestBodyExtract;
import resolve.constant.Constants;
import resolve.utils.ArgumentResolveUtils;
import resolve.utils.JsonUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class WebFluxRequestBodyExtractMethodArgumentResolver extends AbstractMessageReaderArgumentResolver {
    private MethodParameter stringMethodParameter;

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
        return cache.mapNotNull(data -> ArgumentResolveUtils.extractParam(param, data));
    }

    private boolean isReaded(ServerWebExchange exchange){
        return exchange.getAttributeOrDefault(Constants.REQUEST_BODY_READED, false);
    }

    private Mono<Map<String, Object>> getCache(ServerWebExchange exchange){
        return exchange.getAttribute(Constants.REQUEST_BODY_CACHE);
    }

    /**
     * 读取请求体中的数据
     */
    private Mono<Map<String, Object>> readBody(BindingContext bindingContext, ServerWebExchange exchange){
        return readBody(getStringMethodParameter(), true, bindingContext, exchange).cast(String.class).map(body -> JsonUtils.convertObject(body, Map.class));
    }

    /**
     * body缓存起来 多次使用
     */
    private void cacheBody(ServerWebExchange exchange, Mono<Map<String, Object>> data){
        exchange.getAttributes().put(Constants.REQUEST_BODY_READED, true);
        exchange.getAttributes().put(Constants.REQUEST_BODY_CACHE, data);
    }

    /**
     * 获取String类型请求体
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
