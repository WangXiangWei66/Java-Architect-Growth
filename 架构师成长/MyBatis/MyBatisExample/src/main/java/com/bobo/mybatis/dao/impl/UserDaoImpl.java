package com.bobo.mybatis.dao.impl;

import com.bobo.mybatis.dao.IUserDao;
import com.bobo.mybatis.domain.User;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDaoImpl extends SqlSessionDaoSupport implements IUserDao {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public List<User> selectAll() {
        return getSqlSession().selectList("com.bobo.mybatis.mapper.UserMapper.selectUserList");
    }
}
