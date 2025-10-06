package com.study.spring.sample.tx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.study.spring.sample.tx.dao.UserDao;
import com.study.spring.sample.tx.entity.Log;
import com.study.spring.sample.tx.entity.User;

@Service
public class UserService2 {

	@Autowired
	private UserDao userDao;

	@Autowired
	private LogService logService;

	@Transactional(propagation = Propagation.REQUIRES_NEW) // 根本没产生新事务
	public void insertUser(User u) {

		this.userDao.insert(u);
		Log log = new Log(System.currentTimeMillis() + "", System.currentTimeMillis() + "-" + u.getUserName());
		this.logService.insertLog(log);
	}

	@Transactional
	public void addUser(User u) {
		this.insertUser(u);
	}

}
