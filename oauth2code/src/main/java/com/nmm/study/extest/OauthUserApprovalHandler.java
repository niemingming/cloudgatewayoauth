package com.nmm.study.extest;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.approval.DefaultUserApprovalHandler;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义同意处理逻辑，不在每次都跳转授权确认页面。
 */
@Component
public class OauthUserApprovalHandler extends DefaultUserApprovalHandler implements UserApprovalHandler {
    @Override
    public boolean isApproved(AuthorizationRequest authorizationRequest, Authentication userAuthentication) {
        System.out.println("clientId:;" + authorizationRequest.getClientId());
        if (authorizationRequest.isApproved() || authorizationRequest.getClientId().equals("one")) {
            //one我们认为不需要确认。
            return true;
        }
        return false;
    }
}
