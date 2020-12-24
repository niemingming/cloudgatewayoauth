package com.nmm.study.extest;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限校验逻辑
 * 第三方的校验，各种集成校验都可以放到这里来实现。
 * 如果我们仅仅是要修改获取用户的属性问题，其他校验还是通过原生的校验方式，这里可以不处理。直接实现UserDetailsService
 */
@Component
public class CustomLoginProvider implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        /**
         * 1、idm校验
         */
//        String cpath = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getParameter("");
        //验证码校验
        //获取额外参数等等。这里我们就不校验就总是通过
        System.out.println("校验通过::" + username);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ADMIN"));

        return new UsernamePasswordAuthenticationToken(username,password,authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
