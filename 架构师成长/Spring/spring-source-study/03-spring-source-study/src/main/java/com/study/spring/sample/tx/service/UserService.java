package com.study.spring.sample.tx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.spring.sample.tx.dao.LogDao;
import com.study.spring.sample.tx.dao.UserDao;
import com.study.spring.sample.tx.entity.Log;
import com.study.spring.sample.tx.entity.User;

@Service
public class UserService {

	@Autowired
	private UserDao userDao;

	@Autowired
	private LogDao logDao;

	@Transactional
	public void insertUser(User u) {

		this.userDao.insert(u);
		Log log = new Log(System.currentTimeMillis() + "", System.currentTimeMillis() + "-" + u.getUserName());
		this.logDao.insert(log);
	}

}
