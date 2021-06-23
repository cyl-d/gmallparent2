package com.atguigu.gmall.all.controller;

import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author cyl
 * @Date 2021/6/22 17:17
 * @Version 1.0
 */

@Api(tags = "用户认证接口")
@Controller
public class PassportController {

    @GetMapping("login.html")
    public String login(HttpServletRequest request) {
//        获取历史地址
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl", originUrl);
        return "login";
    }


}
