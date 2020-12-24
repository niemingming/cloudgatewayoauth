package com.nmm.study.extest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义授权跳转页面
 */
@Controller
@SessionAttributes("authorizationRequest")
public class CustomApprovalController {
    @RequestMapping("/oauth/confirm_access")
    public void getAccessConfirm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/approval.html").forward(request,response);
    }
}
