package com.bobo.mybatis.dao;

import com.bobo.mybatis.domain.User;

import java.util.List;

public interface IUserDao {

    List<User> selectAll();
}
