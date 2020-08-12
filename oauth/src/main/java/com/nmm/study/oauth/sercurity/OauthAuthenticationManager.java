package com.nmm.study.oauth.sercurity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证方法实现类
 * 只需要加入到spring容器管理即可，我们不需要在配置了。
 */
@Slf4j
@Component
public class OauthAuthenticationManager implements AuthenticationManager {
    /**
     * 业务认证方法，由我们自己实现，除了这种方式，
     * 还可以通过AuthenticationProvider接口实现，这种方式更灵活一些，允许我们定义自己支持的鉴权形式，哪些鉴权通过哪种形式进行鉴权。比如用户来源多样时，
     * 特殊标识时，我们就可以做这种处理
     * @param authentication
     * @return
     * @throws AuthenticationException
     */
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials() + "";

        log.info("登录信息：用户名：{},密码：{}" ,username,password);

        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        /**
         * 从数据库或者规则获取用户权限判断，并获取用户登录信息用于表示鉴权是否需通过，即authentication是否为true、
         *
         * 这里我们登录判断用户名？密码规则，然后将权限查询出来.
         */
        return new UsernamePasswordAuthenticationToken(username,password,authorities);
    }
}
