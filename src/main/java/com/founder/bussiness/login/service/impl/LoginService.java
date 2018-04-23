package com.founder.bussiness.login.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.easymap.management.user.User;
import com.founder.bussiness.login.dao.LoginDao;
import com.founder.bussiness.login.service.ILoginService;
import com.founder.utils.FileUtil;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@SuppressWarnings("unchecked")
@Service
@Scope("prototype")
public class LoginService implements ILoginService{
	//log4j日志
	Logger logger = Logger.getLogger(LoginService.class.getName());

	/*数据库连接类*/
	@Resource
	private LoginDao loginDao;


   /**
    *@author: jinwei【jin_wei@founder.com.cn】
    *@description: 用户登陆
    *@create: 2018/1/11 16:30
    */
    public Map UserLoginDb(Map param) {
        Map remap = new HashMap();
        //验证用户
        Map info = null;
        try {
            info = this.loginDao.UserLoginDb(param);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(info==null || "".equals(info.get("USERNAME"))){
            remap.put("code", "EC04");
            remap.put("msg", "登录失败，用户名/密码错误！");
        }else{
            remap.put("code", "EC00");
            remap.put("msg", "登陆成功！");
            User cuser = new User();
            cuser.setId(String.valueOf(info.get("USERID")));
            cuser.setName(String.valueOf(info.get("USERNAME")));
            remap.put("user", cuser);
            //获取权限
            JSONObject authJson = getUserAuth();
            authJson.put("user",cuser);
            remap.put("auth", authJson);
        }
        return remap;
    }

     /**
      *@author: jinwei【jin_wei@founder.com.cn】
      *@description:获取用户权限(暂时写死)
      *@create: 2018/1/12 10:32
      */
    public JSONObject getUserAuth(){
        String jsonPath = LoginService.class.getResource("/").getPath()+"com/founder/config/menu.json";
        String authStr = FileUtil.readFile(jsonPath);
        return JSONObject.fromObject(authStr);
    }

     /**
      *@author: jinwei【jin_wei@founder.com.cn】
      *@description: 添加用户，并将key为userid,value为结果的内容存入redis
      *@create: 2018/1/11 16:30
      */

     @Transactional(propagation=Propagation.REQUIRED,rollbackFor={Exception.class, ArithmeticException.class}, timeout=1)
     @CachePut(cacheNames="User", key="#param['userid'].toString()")
     public Map AddUser(Map param) {
        Map remap = new HashMap();
        try {
            int flag1 = this.loginDao.AddUser(param);
            remap.put("code", flag1 >= 1?"EC00":"EC02");
            remap.put("msg",  flag1 >= 1?"操作成功":"操作失败");
        } catch (Exception e) {
            logger.debug("添加用户失败："+e.getMessage());
            System.out.println("添加用户失败，准备回滚");
            remap.put("code", "EC01");
            remap.put("msg", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return remap;
        }
        return remap;
    }

     /**
      *@author: jinwei【jin_wei@founder.com.cn】
      *@description: 更新用户
      *@create: 2018/1/11 16:30
      */
     //如果更新后返回了当前记录，则使用@CachePut(cacheNames="User", key="#param['userid'].toString()")
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor={Exception.class, ArithmeticException.class}, timeout=1)
    @CacheEvict(cacheNames="User", key="#param['userid'].toString()", allEntries = true)
    public Map UppUser(Map param) {
        Map remap = new HashMap();
        try {
            int flag = this.loginDao.UppUser(param);
            remap.put("code", flag >= 1?"EC00":"EC02");
            remap.put("msg",  flag >= 1?"操作成功":"没有找到记录，未能更新");
        } catch (Exception e) {
            logger.debug("更新用户失败："+e.getMessage());
            remap.put("code", "EC01");
            remap.put("msg", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return remap;
        }
        return remap;
    }

    /**
     *@author: jinwei【jin_wei@founder.com.cn】
     *@description: 删除用户，并将key为userid的内容从redis中删除
     *@create: 2018/1/11 16:30
     */
   @Transactional(propagation=Propagation.REQUIRED,rollbackFor={Exception.class, ArithmeticException.class}, timeout=1)
   @CacheEvict(cacheNames="User", key="#param['userid'].toString()", allEntries = true)
   public Map DelUser(Map param) {
        Map remap = new HashMap();
        try {
            int flag = this.loginDao.DelUser(param);
            remap.put("code", flag >= 1?"EC00":"EC02");
            remap.put("msg",  flag >= 1?"操作成功":"没有找到记录，未能删除");
        } catch (Exception e) {
            logger.debug("删除用户失败："+e.getMessage());
            remap.put("code", "EC01");
            remap.put("msg", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return remap;
        }
        return remap;
    }

     /**
      *@author: jinwei【jin_wei@founder.com.cn】
      *@description:查询用户
      *@create: 2018/1/12 17:20
      */
    public Map QueryUser(Map param) {
        //处理参数
        int start = Integer.parseInt((String)param.get("start"));
        int limit = Integer.parseInt((String)param.get("limit"));
        param.put("start" , start);
        param.put("limit" , limit);
        Map remap = new HashMap();
        List rowList = new ArrayList();
        String total = null;
        try {
            total = this.loginDao.QueryUserCount(param);
            rowList= this.loginDao.QueryUser(param);
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("查询用户失败："+e.getMessage());
            remap.put("code", "EC01");
            remap.put("msg", e.getMessage());
            return remap;
        }
        remap.put("code", "EC00");
        remap.put("msg", "查询成功");
        remap.put("rowlist", rowList);
        remap.put("total", total);
        return remap;
    }

    /**
     *@author: jinwei【jin_wei@founder.com.cn】
     *@description:根据id查询用户,并将key为userid,value为查询结果插入redis
     *@create: 2018/1/12 17:20
     */
    @Cacheable(cacheNames="User", key="#param['userid'].toString()")
    public Map QueryUserById(Map param) {
        //处理参数
        Map remap = new HashMap();
        Map userInfo = new HashMap();
        try {
            userInfo= this.loginDao.QueryUserById(param);
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("根据id查询用户失败："+e.getMessage());
            remap.put("code", "EC01");
            remap.put("msg", e.getMessage());
            return remap;
        }
        remap.put("code", "EC00");
        remap.put("msg", "查询成功");
        remap.put("userInfo", userInfo);
        return remap;
    }
}
