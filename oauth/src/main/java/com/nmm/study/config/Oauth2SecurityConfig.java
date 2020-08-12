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
