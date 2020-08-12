package com.nmm.study.authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 鉴权逻辑
 */
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
