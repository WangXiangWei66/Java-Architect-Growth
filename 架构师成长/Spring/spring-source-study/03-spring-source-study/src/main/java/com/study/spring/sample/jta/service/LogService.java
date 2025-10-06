package com.study.spring.sample.jta.service;

import com.study.spring.sample.jta.dao.LogDao;
import com.study.spring.sample.jta.entity.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogService {

	@Autowired
	private LogDao logDao;

	@Transactional
	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	// @Transactional(propagation = Propagation.NESTED)
	public void insertLog(Log log) {
		this.logDao.insert(log);
	}

}
