package com.nmm.study.config;

import com.nmm.study.extest.CustomLoginProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.DefaultLoginPageConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomLoginProvider customLoginProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //formlogin是指未授权的情况下跳转到登录页面
        http.authenticationProvider(customLoginProvider)
                .formLogin()
                .usernameParameter("user") // 指定username参数名
                .passwordParameter("password") //指定password参数名
                .loginProcessingUrl("/login")  //需要指定登录处理逻辑的url，要不然上面页面重置后会有影响,有顺序影响
                .loginPage("/login.html")//自定义登录页面？自定义页面需要考虑csrf拦截。
                .and().csrf().disable()//启用自定义登录，需要考虑csrf拦截，一种是我们跳转前获取所有csrf参数设置进去，一种就是金庸_csrf属性值。
//                .and()
//                .authorizeRequests()
//                .antMatchers("/login","/login/**").permitAll()
                ;
//        http.apply();//手动添加formLogin
    }
}
