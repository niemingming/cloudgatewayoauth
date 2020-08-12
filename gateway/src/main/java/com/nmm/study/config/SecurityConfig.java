package com.nmm.study.config;

import com.nmm.study.authorization.GatewayAuthorizationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;

/**
 * 配置我们的security，需要注意的是springcloudgateway采用webflux框架，我们需要配置webflux项
 */
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
