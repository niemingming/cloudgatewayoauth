### 基于oauth2的jwt token 结合spring security springcloudgateway做鉴权


### 0、说明

springcloudgateway是基于springwebflux实现，因此其更多的难点在于响应式编程，结合security之后，很多配置项和我们之前略有不同，尤其是其自带的jwt解析也有不同。



### 1、基础环境搭建
引入pom依赖：
```xml
 <properties>
        <java.version>1.8</java.version>
        <spring-cloud.version>Hoxton.SR6</spring-cloud.version>
        <zjdz-common.version>0.0.1-SNAPSHOT</zjdz-common.version>
        <zjdz-basic-model.version>0.0.1-SNAPSHOT</zjdz-basic-model.version>
        <Spring.boot.version>2.3.2.RELEASE</Spring.boot.version>
        <spring-cloud-alibaba.version>2.2.1.RELEASE</spring-cloud-alibaba.version>
        <fastjson.version>1.2.58</fastjson.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>io.projectreactor.netty</groupId>
                    <artifactId>reactor-netty</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>${fastjson.version}</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>io.projectreactor.netty</groupId>
            <artifactId>reactor-netty</artifactId>
            <version>0.9.9.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-oauth2-resource-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-oauth2-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-oauth2-jose</artifactId>
        </dependency>
    </dependencies>
```

### 2、配置授权验证
```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig extends WebFluxConfigurationSupport {

    @Autowired
    private GatewayAuthorizationManager gatewayAuthorizationManager;

    @Bean
    public KeyProperties keyProperties(){
        return new KeyProperties();
    }

    /**
     * 配置调用安全认证服务
     * @param httpSecurity
     * @return
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        String secretKey = keyProperties().getKey();
        httpSecurity.oauth2ResourceServer()
                .authenticationEntryPoint((exchange,e) -> {
                    e.printStackTrace();
                    //用于处理token异常的信息，处理异常的返回信息
                    return Mono.defer(() -> {
                        ServerHttpResponse response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.OK);//修改返回状态为ok
                        // 我们通过flux转换对象为Publisher，异步处理
                        return response.writeWith(Flux.just("发生异常").map(d -> response.bufferFactory().wrap(d.getBytes(Charset.forName("UTF-8")))));
                    });
                })
                .jwt()
                .jwtDecoder(NimbusReactiveJwtDecoder.withSecretKey(new SecretKeySpec(secretKey.getBytes(),"HMACSHA256")).build()) // 配置自己的jwt token解析类
//                .jwtAuthenticationConverter() //配置自己的角色获取规则
                ;
        httpSecurity.authorizeExchange()
                .pathMatchers("/api/login").permitAll()//获取token不鉴权
                .anyExchange().access(gatewayAuthorizationManager) //配置鉴权类
                .and().csrf().disable();

        return httpSecurity.build();

    }
}
```
关键点：

@EnableWebFluxSecurity:表示启用安全认证

httpSecurity.oauth2ResourceServer().jwt() 在默认情况下就已经完成了配置，但是默认支持的rsa认证等形式，不支持我们这里的HMACSHA

#### 如何配置HMACSHA算法认证

.jwtDecoder(NimbusReactiveJwtDecoder.withSecretKey(new SecretKeySpec(secretKey.getBytes(),"HMACSHA256")).build()) // 配置自己的jwt token解析类

#### 如何定义异常响应
```java
.authenticationEntryPoint((exchange,e) -> {
                    e.printStackTrace();
                    //用于处理token异常的信息，处理异常的返回信息
                    return Mono.defer(() -> {
                        ServerHttpResponse response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.OK);//修改返回状态为ok
                        // 我们通过flux转换对象为Publisher，异步处理
                        return response.writeWith(Flux.just("发生异常").map(d -> response.bufferFactory().wrap(d.getBytes(Charset.forName("UTF-8")))));
                    });
                })
```
这里还提供了一种有效的响应重写方式。

### 如何处理自定义鉴权

```java
httpSecurity.authorizeExchange()
                .pathMatchers("/api/login").permitAll()//获取token不鉴权
                .anyExchange().access(gatewayAuthorizationManager) //配置鉴权类
                .and().csrf().disable();

```
实现ReactiveAuthorizationManager接口

```java
@Slf4j
@Component
public class GatewayAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {
    /**
     * 校验逻辑
     * @param mono
     * @param authorizationContext
     * @return
     */
    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> mono, AuthorizationContext authorizationContext) {
        return mono.filter(au -> {
            Object obj = au.getPrincipal();
            log.info("鉴权逻辑，根据结果返回true或者false");
            return true;//鉴权逻辑
        }).map(a -> new AuthorizationDecision(true))
                .defaultIfEmpty(new AuthorizationDecision(false));
    }
}
```

### 如何重写响应头

参考ResponseRewriteFilter，一定注意其order，要在默认的之前

还有需要注意的是，重写后重设contentlenght头，让响应正确的结束

