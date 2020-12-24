## 授权码模式的oauth2认证服务

### 1、注意oauth2是基于security的，因此必须引入security

### 2、我们配置了oauth2的权限策略后，不需要再security里面再次配置。

有两种方式配置，一种在yml里面配置，一种在代码里面配置。详细可以看代码

### 3、我们配置了授权码模式后访问http://ip:port/oauth/authorize?response_type=code&client_id=one&scope=two


参数|是否必填|说明
---|---|---
client_id|是|客户端id，我们在配置中注册的，可以动态添加,注册时可以不指定scope
response_code|是|响应类型，为code
scope|是|授权范围，虽然没啥用

### 4、上述地址访问后报403，未跳转到登录页面

这是由于，登录页面的跳转不是oauth2控制的，是security控制的，因此我们需要配置formLogin

### 5、自定义登录页，登录报403

这是由于，默认security是启用csrf防护的，我们要么获取_csrf参数，要么就禁用该防护

### 6、自定义登录校验

 http.authenticationProvider(customLoginProvider)
 实现AuthenticationProvider接口即可
 
 ### 7、自定义oauthclient信息，不通过内存和jdbc
 
 clients.withClientDetails(MyClientDetailsService);
 实现ClientDetailsService接口
 
 ### 8、登录后不希望出现授权确认页面
 
 endpoints.userApprovalHandler(userApprovalHandler)自定义授权跳转逻辑实现UserApprovalHandler接口

### 9、修改授权确认页面样式

需要自己写一个controller

```java
@Controller
@SessionAttributes("authorizationRequest") //该注解不可少
public class CustomApprovalController {
    @RequestMapping("/oauth/confirm_access")
    public void getAccessConfirm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/approval.html").forward(request,response);
    }
}
```