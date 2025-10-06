package com.bobo.mybatis.mapper;

import com.bobo.mybatis.domain.Dept;
import com.bobo.mybatis.domain.User;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * Dao 的接口声明
 */
public interface UserMapper {

    public List<User> selectUserList();

    public Integer insertUser(User user);

    public List<User> queryUserList(RowBounds rowBounds);
}