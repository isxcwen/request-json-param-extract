package resolve.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import resolve.utils.JsonUtils;
import resolve.utils.handler.FastJsonOption;
import resolve.utils.handler.JsonOption;
import resolve.utils.handler.JacksonOption;

public class BaseConfig {

    @Bean
    @ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
    public JsonOption jacksonOption(){
        System.out.println("jacksonOption");
        JacksonOption jacksonOption = new JacksonOption();
        JsonUtils.setJsonOption(jacksonOption);
        return jacksonOption;
    }

    @Bean
    @ConditionalOnMissingBean(JsonOption.class)
    @ConditionalOnMissingClass("com.fasterxml.jackson.databind.ObjectMapper")
    @ConditionalOnClass(name = "com.alibaba.fastjson.JSONObject")
    public JsonOption fastjsonOption(){
        System.out.println("fastjsonOption");
        FastJsonOption fastJsonOption = new FastJsonOption();
        JsonUtils.setJsonOption(fastJsonOption);
        return fastJsonOption;
    }
}
