package com.atguigu.gmall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author cyl
 */
@SpringBootApplication
@ComponentScan({"com.atguigu.gmall"})
@MapperScan("com.atguigu.gmall.product.mapper")
@EnableDiscoveryClient
public class ServiceProductApplication {

   public static void main(String[] args) {
      SpringApplication.run(ServiceProductApplication.class, args);
   }
}
