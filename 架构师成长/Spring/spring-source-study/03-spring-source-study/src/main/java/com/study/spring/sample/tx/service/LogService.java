package com.study.spring.sample.tx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.study.spring.sample.tx.dao.LogDao;
import com.study.spring.sample.tx.entity.Log;

@Service
public class LogService {

	@Autowired
	private LogDao logDao;

	// @Transactional
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	// @Transactional(propagation = Propagation.NESTED)
	public void insertLog(Log log) {

		this.logDao.insert(log);
	}

}
