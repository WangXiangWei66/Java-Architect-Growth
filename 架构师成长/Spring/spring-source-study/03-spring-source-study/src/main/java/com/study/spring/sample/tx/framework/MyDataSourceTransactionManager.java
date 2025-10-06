package com.study.spring.sample.tx.framework;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionUsageException;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class MyDataSourceTransactionManager implements PlatformTransactionManager {

	private DataSource dataSource;

	@Override
	public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
		// 开启事务，怎么开？
		try {
			Connection conn = this.dataSource.getConnection();

			// 设置连接不自动提交
			conn.setAutoCommit(false);

			// 放入线程上下文中，这样线程后续参与该事务的业务操作都使用这同一连接
			// 问题，后面业务操作会怎样来获取连接
			ConnectionHolder holder = new ConnectionHolder(conn);
			TransactionSynchronizationManager.bindResource(this.dataSource, holder);

			return new SimpleTransactionStatus(true);
		} catch (SQLException e) {
			throw new CannotCreateTransactionException("获取连接异常", e);
		}
	}

	@Override
	public void commit(TransactionStatus status) throws TransactionException {
		if (status.isRollbackOnly()) {
			this.rollback(status);
		}

		Connection conn = ((ConnectionHolder) (TransactionSynchronizationManager.getResource(dataSource)))
				.getConnection();
		try {
			conn.commit();
		} catch (SQLException e) {
			throw new TransactionUsageException("提交事务异常", e);
		} finally {
			this.doCleanupAfterCompletion(conn);
		}
		System.out.println("***************** 执行了事务提交");
	}

	@Override
	public void rollback(TransactionStatus status) {
		Connection conn = ((ConnectionHolder) (TransactionSynchronizationManager.getResource(dataSource)))
				.getConnection();
		try {
			conn.rollback();
		} catch (SQLException e) {
			throw new TransactionUsageException("回滚事务异常", e);
		} finally {
			this.doCleanupAfterCompletion(conn);
		}

		System.out.println("***************** 执行了事务回滚");
	}

	private void doCleanupAfterCompletion(Connection conn) throws TransactionException {
		// 解绑连接
		TransactionSynchronizationManager.unbindResource(dataSource);
		// 还原
		try {
			conn.setAutoCommit(true);
			conn.close();
		} catch (SQLException e) {
			throw new TransactionUsageException("还原连接自动提交异常", e);
		}

	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
