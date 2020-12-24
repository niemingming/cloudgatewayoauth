package com.nmm.study.extest;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Component;

/**
 * 自定义ClientDetails的管理服务。我们在这里可以灵活管理客户端,比如提供添加、删除方法，
 * 提供基于数据库的查询设置方法。
 */
@Component
public class MyClientDetailsService implements ClientDetailsService {
    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        return null;
    }
}
