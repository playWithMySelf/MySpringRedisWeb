package com.founder.bussiness.login.service.impl;

import com.founder.bussiness.login.dao.LoginDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author: inwei
 * @create: ${Date} ${time}
 * @description:
 * @param: ${params}
 * @return: ${returns}
 */

//配置spring和junit整合，这样junit在启动时就会加载spring容器
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:applicationContext.xml","classpath:spring-mvc.xml",
    "classpath:spring-redis.xml"})
public class LoginServiceTest {

    @Autowired
    private LoginDao loginDao;
    @Test
    public void queryUserById() throws Exception {
        Map param = new HashMap();
        param.put("userid" , "admin");

        Map userInfo = this.loginDao.QueryUserById(param);

        System.out.println("结果为："+userInfo.containsKey("userid"));
    }

}