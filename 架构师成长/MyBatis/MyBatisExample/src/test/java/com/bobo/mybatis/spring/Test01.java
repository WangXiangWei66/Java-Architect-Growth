package com.bobo.mybatis.spring;

import com.bobo.mybatis.dao.IUserDao;
import com.bobo.mybatis.domain.User;
import com.bobo.mybatis.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@RunWith(value = SpringJUnit4ClassRunner.class)
public class Test01 {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IUserDao userDao;

    /**
     * SqlSessionFactory在哪创建的？
     * SqlSession对象在哪创建的？
     * Mapper代理类在哪创建的？
     */
    @Test
    public void testQuery1(){
        List<User> users = userMapper.selectUserList();
        for (User user : users) {
            System.out.println(user);
        }
    }

    @Test
    public void testQuery2(){
        List<User> users = userDao.selectAll();
        for (User user : users) {
            System.out.println(user);
        }
    }
}
