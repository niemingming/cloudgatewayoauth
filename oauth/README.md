### SpringCloudOauth2作为认证中心，提供jwttoken

### 1、理解什么是oauth2
这个可以参考网上资料，不在赘述我们这里采用的是用户名/密码的形式做认证。


### 2、搭建基础信息

1、引入依赖
```xml
 <properties>
        <spring.cloud.version>Hoxton.SR6</spring.cloud.version>
        <Spring.boot.version>2.3.2.RELEASE</Spring.boot.version>
        <spring-cloud-alibaba.version>2.2.1.RELEASE</spring-cloud-alibaba.version>
    </properties>


    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring.cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${Spring.boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

        </dependencies>
    </dependencyManagement>

<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-oauth2</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-jwt</artifactId>
            <version>1.1.1.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>com.auth0</groupId>
            <artifactId>java-jwt</artifactId>
            <version>3.10.3</version>
        </dependency>
        <dependency>
            <groupId>com.nimbusds</groupId>
            <artifactId>nimbus-jose-jwt</artifactId>
            <version>8.16</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.12</version>
        </dependency>
    </dependencies>
```    

spring-cloud-starter-oauth2 已经依赖的security包，我们不需要重复引入

2、定义两个配置文件

oauth2SecurityConfig：该类继承AuthorizationServerConfigurerAdapter，主要作用是配置oauth2认证相关的信息
```java
    //该方法配置oauth2下的安全认证信息，
    @Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
	}
    //该方法配置我们oauth2生成的client信息
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
	}
    //该方法最关键，是配置TokenEndpoint ，主要配置token生成规则信息。
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
	}
```
 SecurityConfig：该类集成了WebSecurityConfigurerAdapter，主要是配置我们token获取接口是否需要鉴权。
 全面看一下这两个类：
 
 Oauth2SecurityConfig:
 ```java
package com.nmm.study.config;

import com.nmm.study.oauth.sercurity.OauthTokenConverter;
import com.nmm.study.service.OauthUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.annotation.Resource;

/**
 * oauth2认证配置类
 */
@Slf4j
@Configuration
@EnableAuthorizationServer//该注解表示这是oauth2server
public class Oauth2SecurityConfig  extends AuthorizationServerConfigurerAdapter {

// 该类用于获取client配置

    @Bean
    @ConfigurationProperties(
            prefix = "security.oauth2.client"
    )
    public BaseClientDetails baseClientDetails(){
        return new BaseClientDetails();
    }
    @Autowired
    private BaseClientDetails baseClientDetails;

    @Bean
    public KeyProperties keyProperties(){
        return new KeyProperties();
    }
    @Resource
    private AuthenticationManager authenticationManager;
    @Autowired
    private OauthUserDetailsService userDetailsService;
    @Autowired
    private OauthTokenConverter oauthTokenConverter;

    /**
     * 配置获取token的客户端信息
     * 在默认的配置中OAuth2AuthorizationServerConfiguration已经读取配置信息，我们不需要额外配置爱。
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient(baseClientDetails.getClientId())
                .secret(passwordEncoder().encode(baseClientDetails.getClientSecret()))
                .authorizedGrantTypes(baseClientDetails.getAuthorizedGrantTypes().toArray(new String[]{}))
                .scopes(baseClientDetails.getScope().toArray(new String[]{}))
                .refreshTokenValiditySeconds(baseClientDetails.getRefreshTokenValiditySeconds())
                .accessTokenValiditySeconds(baseClientDetails.getAccessTokenValiditySeconds());
    }

    /**
     * 配置endpoints信息，注意这里的endpoint可以理解为controller或者servlet一样用于处理token相关请求的配置
     *
     * TokenEndpoint  TokenKeyEndpoint  OAuth2AuthorizationServerConfiguration AuthorizationServerSecurityConfiguration
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(new JwtTokenStore(jwtAccessTokenConverter())) // tokenstore可以有效的提升token效率
                .authenticationManager(authenticationManager) // 自定义鉴权的话，不在需要userDetailsvice了,如果用默认的就需要
//                .exceptionTranslator() //该方法用于定义验证不通过的异常信息定义
//                .userDetailsService(userDetailsService)
                .accessTokenConverter(jwtAccessTokenConverter()); //定义token生成规则则
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients()
                .tokenKeyAccess("permitAll()") // 获取token不鉴权
                .checkTokenAccess("isAuthenticated()"); //校验token需要鉴权
    }

    /**
     * jwt转换token
     * @return
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(){
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        //默认采用HMACSHA256算法
        jwtAccessTokenConverter.setSigningKey(keyProperties().getKey());

        DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(oauthTokenConverter);
        jwtAccessTokenConverter.setAccessTokenConverter(accessTokenConverter);
        return jwtAccessTokenConverter;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

```
SecurityConfig
```java
package com.nmm.study.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 常规security配置，用于补充鉴权
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//    /**
//     * 暂时考虑将该manager作为认证管理器
//     * @return
//     * @throws Exception
//     */
//    @Bean
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }

    /**
     * 配置安全策略
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()//所有token获取都不鉴权
                .anyRequest().authenticated();
    }

}

```

### 3、我们如何自定义认证逻辑

我们可以通过实现AuthenticationManager接口，并纳入spring管理即可，该实例会自动装配覆盖默认的认证规则。
需要注意的是，如果我们配置了该类，那么默认的UserDetailsService配置将失效，所有的校验完全取决于我们这里的处理。结果。

### 4、如何自定义token生成的claims内容。

在配置类中，jwttokenAccessTokenConverter中做如下配置：其中，UserTokenConverter就是我们自己设置的返回内容
```java
DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(oauthTokenConverter);
        jwtAccessTokenConverter.setAccessTokenConverter(accessTokenConverter);
        return jwtAccessTokenConverter;
```
### 5、如何自定义异常处理

endpoints.exceptionTranslator()设置自定义异常处理信息

### 6、如何替换jwt密钥规则
```java
 @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(){
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        //默认采用HMACSHA256算法
        jwtAccessTokenConverter.setSigningKey(keyProperties().getKey());
        //这里改为：
        //        KeyStoreKeyFactory keyFactory = new KeyStoreKeyFactory(keyProperties.getKeyStore().getLocation(),keyProperties.getKeyStore().getPassword().toCharArray());
        //        KeyPair keyPair = keyFactory.getKeyPair(keyProperties.getKeyStore().getAlias());
        //        accessTokenConverter.setKeyPair(keyPair);
        DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(oauthTokenConverter);
        jwtAccessTokenConverter.setAccessTokenConverter(accessTokenConverter);
        return jwtAccessTokenConverter;
    }
```

### 7、我使用的key与常见框架的key一致但是不能解密？

我们使用的一些工具类，通常会将key做base64加密使用，这时候我们可以通过如下设置：
```java
 MacSigner signer = new MacSigner(Base64.getDecoder().decode(keyProperties.getKey()));
        accessTokenConverter.setSigner(signer);
        accessTokenConverter.setVerifier(signer);
```
### 8、tokenstore作用

tokenstore是jwttoken的管理器，我们可以选择jdbc，redis、内存形式等，启用tokenstore会提高效率。


### 9、获取token方式，

post请求：/oauth/token
参数： client_id:
client_secret:
username:
password:
grant_type:password
如果有自定义逻辑自行添加