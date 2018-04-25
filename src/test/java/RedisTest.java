import com.founder.bussiness.login.service.impl.LoginService;
import com.founder.utils.RedisCatchUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

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
public class RedisTest {

    @Autowired
    private RedisCatchUtil redisCatchUtil;

    @Resource
    private LoginService loginService;
    private static String key;
    private static String field;
    private static String value;

    // 初始化 数据
    static {
        key = "tb_student";
        field = "stu_name";
        value = "一系列的关于student的信息！";
    }

    @Test
    public void testSelectUser() {
        Map map = new HashMap();
        map.put("userid","admin");
        Map resMap = loginService.QueryUserById(map);
        System.out.println("查询的用户信息为："+resMap);
    }

    @Test
    public void insertRedisHash() {
        Map map = new HashMap();
        map.put("xm","jinwei");
        map.put("age","25");
        redisCatchUtil.setCacheIntegerMap("testusers", map);
        System.out.println("----插入到redis---");
    }

    @Test
    public void readRedisHash() {
        System.out.println("----读取redis hash---");
        Map map = new HashMap();
        map = redisCatchUtil.getCacheIntegerMap("testusers");
        for(Object key : map.keySet()){
            System.out.println("key:"+key+"---"+"value:"+map.get(key));
        }
    }

}
