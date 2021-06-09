package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author atguigu-mqx
 */
@Service
public class TestServiceImpl implements TestService {

    //  谁能操作缓存！
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void testLock() {
        /*
        1.  先从缓存中获取key = num 所对应的数据！
        2.  如果获取到了数据不为空，则对其进行+1 操作！
        3.  如果获取到的数据是空，则直接return！
         */
        //  使用setnx:
        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", "OK");
        //  flag = true : 说明获取到锁了！
        if (flag){
            //  get num
            String num = redisTemplate.opsForValue().get("num");

            //  判断
            if (StringUtils.isEmpty(num)){

                //  直接返回
                return;
            }
            //  对num 进行数据类型转换
            int numValue = Integer.parseInt(num);

            //  对num 进行+1 ，放入缓存 set key value;
            redisTemplate.opsForValue().set("num",String.valueOf(numValue+1));

            //  删除锁
            redisTemplate.delete("lock");

        } else {
            //  没有获取到锁的人等待，
            try {
                Thread.sleep(100);
                //  自旋
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }




    }
}
