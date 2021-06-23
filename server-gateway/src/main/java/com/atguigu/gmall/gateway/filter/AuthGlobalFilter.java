package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @Author cyl
 * @Date 2021/6/22 17:26
 * @Version 1.0
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {

    /**
     *  匹配路径的工具类
      */
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("authUrls.url")
    private String authUrls;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        获取request 对象
        ServerHttpRequest request = exchange.getRequest();
//        获取请求路径
        String path = request.getURI().getPath();
//        拦截设置
//        内部数据 不开放使用
        if (antPathMatcher.match("/**/inner/**", path)) {
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }
//        获取用户id
        String userId = getUserId(request);
//        用户id被盗用
        if ("-1".equals(userId)) {
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }
//        判断当前连接是否是必须要登录的连接
        if (antPathMatcher.match("/api/**/auth/**", path)) {
//            如果未登录 返回
            if (StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }
//        验证url 是否需要拦截
        String[] split = authUrls.split(",");
        for (String s : split) {
            if (path.contains(s) && StringUtils.isEmpty(userId)) {
//                将网页重定向登录页面
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.SEE_OTHER);
                response.getHeaders().set(HttpHeaders.LOCATION, "http://www.gmall.com/login.html?originUrl="+request.getURI());
//                重定向
                response.setComplete();
            }
        }

//        如果有用户id 或 临时用户id, 将其传递后端
        String userTempId = getUserTempId(request);
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)) {
            if (!StringUtils.isEmpty(userId)) {
                request.mutate().header("userId", userId).build();
                return chain.filter(exchange.mutate().request(request).build());
            }
            if (!StringUtils.isEmpty(userTempId)) {
                request.mutate().header("userTempId", userTempId).build();
                return chain.filter(exchange.mutate().request(request).build());
            }
        }

        return chain.filter(exchange);
    }

    /**
     * 接口鉴权失败返回的接口
     * @param response
     * @param permission
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum permission) {
        // 返回用户没有权限登录
//        Result<Object> result = Result.build(null, resultCodeEnum);
        Result<Object> result = Result.build(null, permission);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = response.bufferFactory().wrap(bits);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        // 输入到页面
        return response.writeWith(Mono.just(wrap));

    }

    /**
     * 获取用户id
     * @param request
     * @return
     */
    private String getUserId(ServerHttpRequest request) {
//        获取Token
        String token = request.getHeaders().getFirst("token");
        if (StringUtils.isEmpty(token)) {
//            如果token不存在 尝试通过cookie 获取token
            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
            HttpCookie token1 = cookies.getFirst("token");
            if (token1 != null) {
                token = URLDecoder.decode(token1.getValue());
            }
        }
        if (!StringUtils.isEmpty(token)) {
//            从redis 获取id
            String userStr = (String) redisTemplate.opsForValue().get("user:login:" + token);
            JSONObject jsonObject = JSONObject.parseObject(userStr);
            String ip = jsonObject.getString("ip");
//            判断ip 是否相同
            String gatwayIpAddress = IpUtil.getGatwayIpAddress(request);
            if (!gatwayIpAddress.equals(ip)) {
                return "-1";
            }
            return jsonObject.getString("userId");
        }
        return null;
    }

    /**
     * 获取当前用户临时用户Id
     * @param request
     * @return
     */
    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = request.getHeaders().getFirst("userTempId");
        if (StringUtils.isEmpty(userTempId)) {
//            使用cookie 尝试再次获取临时用户id
            HttpCookie userTempId1 = request.getCookies().getFirst("userTempId");
            if (userTempId1 != null) {
                userTempId = URLDecoder.decode(userTempId1.getValue());
            }
        }
        return userTempId;
    }
}
