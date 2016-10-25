package common.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import rfx.core.stream.configs.SqlDbConfigs;
import rfx.core.util.LogUtil;

public abstract class BaseDAO {
	private DataSource oracleDataSource;
	private final SqlDbConfigs sqlDbConfigs;
	protected String logPrefix = BaseDAO.class.getSimpleName();
	
	protected DataSource getOracleDataSource() {
		if(oracleDataSource == null){
			oracleDataSource = getSqlDbConfigs().getDataSource();
		}
		return oracleDataSource;
	}
	
	protected SqlDbConfigs getSqlDbConfigs() {		
		return sqlDbConfigs;
	}
	
	protected BaseDAO(SqlDbConfigs sqlDbConfigs) {		
		this.sqlDbConfigs = sqlDbConfigs;
		this.logPrefix = getClass().getSimpleName() + "("+sqlDbConfigs.getHost()+")";
	}
	
	public static class SqlExecutor {
		Connection con;
		CallableStatement callableStatement = null;	
		int maxBatchSize = 1, sqlCount = 0;
		String sql;
		
		public int getSqlCount() {
			return sqlCount;
		}
		
		protected void init(BaseDAO commonDbAccess, SqlParamsSetter sqlParamsSetter) throws SQLException{
			this.con = commonDbAccess.getOracleDataSource().getConnection();			
			this.sql = sqlParamsSetter.getSql();
			this.callableStatement = con.prepareCall(sql);				
			sqlParamsSetter.setParams(this);	
		}
		
		public SqlExecutor(BaseDAO commonDbAccess, SqlParamsSetter sqlParamsSetter) throws SQLException {
			super();
			init(commonDbAccess, sqlParamsSetter);		
		}		
		
		public SqlExecutor(int maxBatchSize, BaseDAO commonDbAccess, SqlParamsSetter sqlParamsSetter) throws SQLException {
			super();
			this.maxBatchSize = maxBatchSize;
			init(commonDbAccess, sqlParamsSetter);
		}
		
		public CallableStatement getCallableStatement() {
			return callableStatement;
		}		
		
		public int executeUpdate() throws SQLException{			
			return this.callableStatement.executeUpdate();
		}
		public int[] executeBatch() throws SQLException{			
			return this.callableStatement.executeBatch();
		}		
		public int addBatch() throws SQLException{
			this.callableStatement.addBatch();
			++sqlCount;
			if(sqlCount % maxBatchSize == 0){
				String className = getClass().getSimpleName();
				try {
					this.callableStatement.executeBatch();
					LogUtil.i(className, String.format(" executeBatch OK at time %s sqlCount %d", new Date().toString(), sqlCount ), true);
				} catch (Throwable e) {
					LogUtil.i(className, String.format(" executeBatch FAIL at time %s sqlCount %d", new Date().toString(), sqlCount ), true);
					e.printStackTrace();
					//skip this batch, reset callableStatement
					this.callableStatement = con.prepareCall(sql);
					return sqlCount;
				}
			}
			return sqlCount;
		}
		
		public void closeSqlResource(){			
			if(callableStatement != null){
				try {
					callableStatement.close();
				} catch (SQLException e1) {	}
			}	
			if(con != null){
				try {
					con.close();
				} catch (SQLException e1) {	}
			}				
		}
	}
	
	public static abstract class SqlParamsSetter {
		final protected String sql;
		private AtomicInteger pCount = new AtomicInteger(0);
		public synchronized int pCount() {
			return pCount.addAndGet(1);
		}		
		public synchronized void resetpCount() {
			pCount.set(0);
		}
		public synchronized int getpCount() {
			return pCount.get();
		}
		public SqlParamsSetter(String sql) {
			super();
			this.sql = sql;
		}
		public String getSql() {
			return sql;
		}
		public abstract void setParams(SqlExecutor executer) throws SQLException;
	}
	
	protected int executeUpdate(SqlParamsSetter sqlParamsSetter){
		if( ! this.sqlDbConfigs.isEnabled() ){
			return 0;
		}
		SqlExecutor sqlExecutor = null;
		int n = 0;
		try {
			sqlExecutor = new SqlExecutor(this, sqlParamsSetter);
			n = sqlExecutor.executeUpdate();
		} catch (Throwable e) {
			String msg = e.getMessage();
			if(msg.contains("ORA-12899")){
				if(sqlExecutor != null){
					sqlExecutor.closeSqlResource();
				}
				return 1;
			}
			e.printStackTrace();
			dbExceptionHandler(e, sqlParamsSetter );
		} finally {
			if(sqlExecutor != null){
				sqlExecutor.closeSqlResource();
			}
		}
		return n;
	}	
	
	final static String ERROR_CONNECTION_BUSY = "ORA-00257";
	final static String ERROR_CONNECTION_TIME_OUT = "timed out";
	volatile int timeToSleep = 0;//for ORA-00257
	void  dbExceptionHandler(Throwable exception, SqlParamsSetter sqlParamsSetter){		
		String msg = exception.getMessage();
		LogUtil.e(logPrefix+" DbLogDataManager", msg + " " + exception.toString());		
				
		if(msg.contains("ORA-06550")){
			return;
		}
		if(msg.contains(ERROR_CONNECTION_TIME_OUT)){
			resetOracleDataSource();//force reset datasource
		} else if(msg.contains(ERROR_CONNECTION_BUSY)){
			timeToSleep = 4000;
		}		
	}
	
	public synchronized void resetOracleDataSource() {	
		oracleDataSource = null;		
		oracleDataSource = getSqlDbConfigs().getDataSource();
	}
	
	protected void executeBatchUpdate(int maxBatchSize, SqlParamsSetter sqlParamsSetter){
		if( ! this.sqlDbConfigs.isEnabled() ){
			return;
		}
		String className = getClass().getSimpleName();
		LogUtil.i(className, String.format(" executeBatchUpdate at time %s ", new Date().toString() ), true);
		SqlExecutor sqlExecutor = null;
		try {
			sqlExecutor = new SqlExecutor(maxBatchSize, this, sqlParamsSetter);
			sqlExecutor.executeBatch();
			LogUtil.i(className, String.format(" executeBatch at time %s sqlCount %d", new Date().toString(), sqlExecutor.getSqlCount() ), true);
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.error(e);
		} finally {
			if(sqlExecutor != null){
				sqlExecutor.closeSqlResource();
			}
		}
	}
}
