# request-json-param-extract
用于springboot的@RequestBody json对象属性的参数解析

**支持SpringMVC 与 WebFlux**

## 设计原因
在springboot项目中对于post的json请求需要通过@RequestBody


1. **定义一个Model去接收**

```
@PostMapping("/1")
public String test(@RequestBody User user){
    return user.getName();
}
```
_缺点_：
- 需要定义实体 
- 不相关字段过多，会产生大量实体
- 部分业务只有一到两个字段，定义实体过于繁琐
<br/>
  <br/>
2. **JSON工具去转换，然后Getter， Setter**


```
@PostMapping("/2")
public String test2(@RequestBody String body){
    JSONObject json = JSONObject.parse(body);
    return json.getString("name");
}
```    
_缺点_：
- 需要转换成JSON对象
- 不同的类型不同的Getter API

## @RequestBodyExtract
现在只需要引入该jar 使用注解便可像@RequestParam一样获取参数

    @RequestMapping("/3")
    public String test3(@RequestBodyExtract("name") String n,
        @RequestBodyExtract int age,
        @RequestBodyExtract(required = false) Hobby hobby,
        @RequestBodyExtract(required = true, defaultValue = "true") boolean sex,
        @RequestBodyExtract("addresss") List<Address> address){
        return "success";
    }


## 本地maven安装
    mvn install:install-file -Dfile=method-paramter-resolve-0.0.1-SNAPSHOT.jar -DgroupId=resolve -DartifactId=param-resolve -Dversion=1.0 -Dpackaging=jar
##
    <dependency>
        <groupId>resolve</groupId>
        <artifactId>param-resolve</artifactId>
        <version>1.0</version>
    </dependency>