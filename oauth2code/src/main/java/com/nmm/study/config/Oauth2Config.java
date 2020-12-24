package com.nmm.study.config;

import com.nmm.study.extest.OauthUserApprovalHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

/**
 * oauth2的核心配置类,oauth2的权限校验是基于security来的，
 */
@Configuration
@EnableAuthorizationServer // 作为认证服务器使用
public class Oauth2Config extends AuthorizationServerConfigurerAdapter {

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private OauthUserApprovalHandler userApprovalHandler;
    @Bean
    public PasswordEncoder passwordEncoder(){
        return passwordEncoder;
    }

    //与在yml中配置效果一样
//    @Override
//    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
//        security.tokenKeyAccess("permitAll()")
//                .checkTokenAccess("isAuthenticated()");
//    }

    /**
     * @AuthorizationEndpoint 是授权码模式的处理类/oauth/authorize
     * 授权码模式会校验scope是否匹配
     * 请求参数需要
     * client_id
     * response_type=code
     * scope=onescope
     * @param clients
     * @throws Exception
     */
    //也可以在yml中配置.yml中配置多个还不是很清楚
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients.jdbc(null)//这种是指从数据库中获取client。需要按照框架设定的表名和字段来做查询操作。相对比较死板
//            ;
        //不论是jdbc模式还是inMemory模式，我们都可以动态添加客户端。inMemory模式需要手动获取InMemoryClientDetailsService
        //在一种我们自己提供ClientDetailsService。在这里动态修改Client
//        clients.withClientDetails(MyClientDetailsService);
        //这里我们用内存形式
//        AuthorizationGrantType,指定了类型
        clients.inMemory()
                .withClient("one")
                .secret(passwordEncoder.encode("123456"))
                .authorizedGrantTypes("authorization_code") //refresh_token  password等几种模式
                .redirectUris("http://localhost:9000/userinfo") //授权码模式必须设置跳转路径
                .refreshTokenValiditySeconds(3600) //有效时长
                .accessTokenValiditySeconds(3600)
                .scopes("onescope")//scope不是必填项，只要请求带有就可以了。这里可以不设置。请求必须携带scope参数
                .and()
                .withClient("two") //多个客户端
                .secret(passwordEncoder.encode("123456"))
                .authorizedGrantTypes("authorization_code")
                .redirectUris("http://localhost:9000/userinfo") //授权码模式必须设置跳转路径
                .refreshTokenValiditySeconds(3600) //有效时长
                .accessTokenValiditySeconds(3600);
    }

    /**
     * 授权确认页面
     * @link WhitelabelApprovalEndpoint
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.userApprovalHandler(userApprovalHandler)//自定义approvalHandler避免每次都要调换默认的授权确认页面
//            .pathMapping("/oauth/confirm_access","/approval.html")//修改授权确认页面,未生效
        //自定义授权页面，我们可以通过controller实现。
                ;
    }
}
