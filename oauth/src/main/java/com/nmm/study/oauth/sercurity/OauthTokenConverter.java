package com.nmm.study.oauth.sercurity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自定义token claims内容
 */
@Slf4j
@Component
public class OauthTokenConverter extends DefaultUserAuthenticationConverter {

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {

        Map<String,Object> claims = new LinkedHashMap<>(); // 获取角色信息
        Object obj = authentication.getPrincipal(); // 这里的principal就是我们在AuthenticationManager返回的authentication定义的。我们可以在这里添加token内容
        claims.put("add","hello");
        claims.put("username",authentication.getName());
        claims.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));

        log.info("获取token信息：{}",claims );
        return claims;
    }
}
