package com.bobo.mybatis.test;

import com.bobo.mybatis.domain.User;
import com.bobo.mybatis.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

/**
 * MyBatis的基本使用
 *
 */
public class Test01 {

    /**
     * MyBatis API 的使用
     * @throws Exception
     */
    @Test
    public void test1() throws  Exception{
        // 1.获取配置文件
        InputStream in = Resources.getResourceAsStream("mybatis-config.xml");
        // 2.加载解析配置文件并获取SqlSessionFactory对象
        //  1.加载全局配置文件  加载所有的映射文件  创建SqlSessionFactory对象
        //  加载的各种配置文件中的信息 肯定是需要存储在java 对象中--> 在哪个对象中？ Configuration
        //  加载的各种信息封装的对象是怎么和 SqlSessionFactory 关联起来的？
        // 设计模式： 工厂模式--》对外生产 SqlSession对象
        //          建造者模式 --》 针对复杂对象的创建
        // SqlSessionFactory 对象比较复杂 --》 1.创建 SqlSessionFactory对象 2.各种配置文件的加载解析 --》 这个是复杂的
        // SqlSessionFactoryBuilder() 1.创建 SqlSessionFactory对象 2.各种配置文件的加载解析
        // 1.完成 全局配置文件的加载解析 --Configuration 对象中 --》 映射文件加载解析 --》MappedStatement --》 绑定到 Configuration
        // 2. 创建SqlSessionFactory 关联到 Configuration 中
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
        // 3.根据SqlSessionFactory对象获取SqlSession对象
        //  获取SqlSession对象做了哪些初始化的操作？ 创建了 Executor对象和 DefaultSqlSession对象
        SqlSession sqlSession = factory.openSession();
        // 4.通过SqlSession中提供的 API方法来操作数据库
        //  SqlSession 是如何完成一个完整的 查询操作的
        PageHelper.startPage(1,2);
        // 如果开启了二级缓存
        // 先走 二级缓存 然后 一级缓存 如果都没有 开始数据库操作 数据库操作是通过JDBC完成的
        List<User> list = sqlSession.selectList("com.bobo.mybatis.mapper.UserMapper.selectUserList");
        for (User user : list) {
            System.out.println(user);
        }
        // 5.关闭会话
        sqlSession.close();
    }

    /**
     * MyBatis getMapper 方法的使用
     * 一级缓存是 SqlSession 级别的
     * 二级缓存是 SqlSessionFactory 进程级别 是把数据存储在当前系统的内存中
     * 三级缓存  集群。夸服务  --》 把缓存数据存储在第三方的服务中。比如Redis
     * 本质上二级缓存和三级缓存是一个。实现方式是一样的。只是数据的存储的位置有差异
     */
    @Test
    public void test2() throws Exception{
        // 1.获取配置文件
        InputStream in = Resources.getResourceAsStream("mybatis-config.xml");
        // 2.加载解析配置文件并获取SqlSessionFactory对象 全局配置文件的解析  映射文件的解析
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
        // 3.根据SqlSessionFactory对象获取SqlSession对象
        SqlSession sqlSession = factory.openSession();
        // 4.通过SqlSession中提供的 API方法来操作数据库
        List<User> list = sqlSession.selectList("com.bobo.mybatis.mapper.UserMapper.selectUserList");
        for (User user : list) {
            System.out.println(user);
        }
        System.out.println("----------------------");
        // 从一级缓存中获取了数据
        list = sqlSession.selectList("com.bobo.mybatis.mapper.UserMapper.selectUserList");
        for (User user : list) {
            System.out.println(user);
        }
        // 5.关闭会话 缓存数据是怎么存储的？ key statementId + sql 语句
        sqlSession.close(); // session 会话关闭的时候 一级缓存的数据会被清空
        System.out.println("----------session close------------");
        sqlSession = factory.openSession();
        // 4.通过SqlSession中提供的 API方法来操作数据库
        list = sqlSession.selectList("com.bobo.mybatis.mapper.UserMapper.selectUserList");
        for (User user : list) {
            System.out.println(user);
        }
        System.out.println("----------------------");
        list = sqlSession.selectList("com.bobo.mybatis.mapper.UserMapper.selectUserList");
        for (User user : list) {
            System.out.println(user);
        }
        // 5.关闭会话
        sqlSession.close();

    }

}
