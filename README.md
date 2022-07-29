# request-json-param-extract
用于springboot的@RequestBody json对象属性的参数解析

**支持SpringMVC 与 WebFLux**

## 介绍
在springboot项目中对于post的json请求往往需要定义一个Model去接收或者JSON工具去转换



    @PostMapping("/1")
    public String test(@RequestBody User user){
        return user.getName();
    }

    @PostMapping("/1")
    public String test(@RequestBody String body){
        JSONObject json = JSONObject.parse(body);
        return json.getString("name");
    }

现在只需要引入该jar 使用注解便可像@RequestParam一样获取参数

    @RequestMapping("/3")
    public String test2(@RequestBodyExtract("name") String name,
        @RequestBodyExtract("age") int age,
        @RequestBodyExtract("address") Address address){
        return "success";
    }


## 本地maven安装
mvn install:install-file -Dfile=method-paramter-resolve-0.0.1-SNAPSHOT.jar -DgroupId=resolve -DartifactId=param-resolve -Dversion='1.0' -Dpackaging=jar

