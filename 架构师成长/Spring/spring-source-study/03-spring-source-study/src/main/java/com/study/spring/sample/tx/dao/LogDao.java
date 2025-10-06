package com.study.spring.sample.tx.dao;

import com.study.spring.sample.tx.entity.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LogDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	// @Insert("insert t_log(id,log) values(#{id},#{log})")
	public void insert(Log log) {
		jdbcTemplate.update("insert t_log(id,log) values(?,?)", log.getId(), log.getLog());
	}

}
