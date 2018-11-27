package com.luoshang.zkweb.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZkCfgManagerImpl implements InitializingBean, ZkCfgManager {

	private static Logger Logger = LoggerFactory.getLogger(ZkCfgManagerImpl.class);
	@Autowired
	private DataSource dataSource;
	private static Connection conn = null;
	static QueryRunner run = new QueryRunner(H2Util.getDataSource());

	public ZkCfgManagerImpl() {

	};

	/**
	 * 获取zookeeper连接
	 * 
	 * @return
	 * @throws SQLException
	 */
	private Connection getConnection() throws SQLException {
		if (null == conn) {
			conn = dataSource.getConnection();
		}
		return conn;
	}

	/**
	 * 关闭zookeeper连接
	 */
	private void closeConn() {
		if (null != conn) {
			try {
				conn.close();
			} catch (SQLException e) {
				Logger.error("zookeeper关闭失败！");
			}
		}
	}

	/**
	 * 销毁数据库连接池
	 */
	public void destroyPool() {
		H2Util.destroyDataSource();
		closeConn();
	}

	/**
	 * 初始化zookeeper配置
	 * 
	 * @return
	 */
	private boolean init() {
		if (isTableOk()) {
			return true;
		}
		Logger.error("create table ({})...", ZkCfgManager.initSql);
		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(ZkCfgManager.initSql);
			int ret = ps.executeUpdate();
			if (ret >= 0) {
				Logger.error("create table OK !ret={}", ret);
				List<Map<String, Object>> result = query("");
				if (result == null) {
					Logger.error("table select check Error!");
				} else {
					Logger.error("table select check OK!");
				}
				return true;
			}
			Logger.error("create table error !ret={}", ret);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("init zkCfg error : {}", e.getMessage());
			Logger.error(e.getMessage(), e);
		} finally {
			if (null != ps) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.error(e.getMessage(), e);
				}
			}
		}
		return false;
	}

	/**
	 * 查询zookeeper配置
	 */
	public List<Map<String, Object>> query(int page, int rows, String whereSql) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Logger.info("whereSq={}", whereSql);
			if (whereSql == null || whereSql.isEmpty()) {
				ps = getConnection().prepareStatement("SELECT * FROM ZK limit ?,?");
			} else {
				ps = getConnection().prepareStatement("SELECT * FROM ZK where " + whereSql + " limit ?,? ");
			}
			ps.setInt(1, (page - 1) * rows);
			ps.setInt(2, rows);
			rs = ps.executeQuery();
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			// ResultSetMetaData meta = rs.getMetaData();
			Map<String, Object> map = null;
			// int cols = meta.getColumnCount();
			while (rs.next()) {
				map = new HashMap<String, Object>();
				for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
					map.put(rs.getMetaData().getColumnName(i + 1), rs.getObject(i + 1));
				}
				list.add(map);
			}
			return list;
		} catch (SQLException e) {
			Logger.error("查询zookeeper配置异常！");
		} finally {
			if (null != rs) {
				try {
					rs.close();
				} catch (SQLException e) {
					Logger.error("关闭连接失败!");
				}
			}
			if (null != ps) {
				try {
					ps.close();
				} catch (SQLException e) {
					Logger.error("关闭查询实例失败!");
				}
			}
		}
		return new ArrayList<Map<String, Object>>();
	}

	/**
	 * 添加zookeeper配置
	 */
	public boolean add(String desc, String connectStr, String sessionTimeOut) {
		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement("INSERT INTO ZK VALUES(?,?,?,?)");
			ps.setString(1, UUID.randomUUID().toString().replaceAll("-", ""));
			ps.setString(2, desc);
			ps.setString(3, connectStr);
			ps.setString(4, sessionTimeOut);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			Logger.error("add zkCfg error : {}", e.getMessage());
		} finally {
			if (null != ps) {
				try {
					ps.close();
				} catch (SQLException e) {
					Logger.error("关闭插入实例失败!");
				}
			}
		}
		return false;
	}

	/**
	 * 查询zookeeper配置
	 */
	public List<Map<String, Object>> query() {
		List<Map<String, Object>> result = query("where not(desc like 'ignore_%')");
		if (result == null)
			return new ArrayList<Map<String, Object>>();
		return result;
	}

	/**
	 * 查询所有的配置
	 * 
	 * @return
	 */
	public List<Map<String, Object>> queryAll() {
		List<Map<String, Object>> result = query("");
		if (result == null)
			return new ArrayList<Map<String, Object>>();
		return result;
	}

	/**
	 * 无分页查询
	 * 
	 * @param whereSql
	 * @return
	 */
	private List<Map<String, Object>> query(String whereSql) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConnection().prepareStatement("SELECT * FROM ZK " + whereSql);
			rs = ps.executeQuery();

			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

			ResultSetMetaData meta = rs.getMetaData();
			Map<String, Object> map = null;
			int cols = meta.getColumnCount();
			while (rs.next()) {
				map = new HashMap<String, Object>();
				for (int i = 0; i < cols; i++) {
					map.put(meta.getColumnName(i + 1), rs.getObject(i + 1));
				}
				list.add(map);
			}

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.error(e.getMessage(), e);
		} finally {
			if (null != rs) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.error(e.getMessage(), e);
				}
			}
			if (null != ps) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.error(e.getMessage(), e);
				}
			}

		}
		return null;
	}

	/**
	 * 更新zookeeper配置
	 */
	public boolean update(String id, String desc, String connectStr, String sessionTimeOut) {
		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement("UPDATE ZK SET DESC=?,CONNECTSTR=?,SESSIONTIMEOUT=? WHERE ID=?;");
			ps.setString(1, desc);
			ps.setString(2, connectStr);
			ps.setString(3, sessionTimeOut);
			ps.setString(4, id);
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("update id={} zkCfg error : {}", new Object[] { id, e.getMessage() });
			Logger.error(e.getMessage(), e);
		} finally {
			if (null != ps) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.error(e.getMessage(), e);
				}
			}
		}

		return false;
	}

	/**
	 * 删除zookeeper配置
	 */
	public boolean delete(String id) {
		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement("DELETE ZK WHERE ID=?");
			ps.setString(1, id);
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("delete id={} zkCfg error : {}", new Object[] { id, e.getMessage() });
			Logger.error(e.getMessage(), e);
		} finally {
			if (null != ps) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.error(e.getMessage(), e);
				}
			}
		}
		return false;
	}

	public Map<String, Object> findById(String id) {

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConnection().prepareStatement("SELECT * FROM ZK WHERE ID = ?");
			ps.setString(1, id);
			rs = ps.executeQuery();
			Map<String, Object> map = new HashMap<String, Object>();
			ResultSetMetaData meta = rs.getMetaData();
			int cols = meta.getColumnCount();
			if (rs.next()) {
				for (int i = 0; i < cols; i++) {
					map.put(meta.getColumnName(i + 1).toLowerCase(), rs.getObject(i + 1));
				}
			}
			return map;
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.error(e.getMessage(), e);
		} finally {
			if (null != rs) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.error(e.getMessage(), e);
				}
			}
			if (null != ps) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.error(e.getMessage(), e);
				}
			}
		}
		return null;
	}

	public boolean add(String id, String desc, String connectStr, String sessionTimeOut) {
		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement("INSERT INTO ZK VALUES(?,?,?,?);");
			ps.setString(1, id);
			ps.setString(2, desc);
			ps.setString(3, connectStr);
			ps.setString(4, sessionTimeOut);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.error("add zkCfg error : {}", e.getMessage());
			Logger.error(e.getMessage(), e);
		} finally {
			if (null != ps) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.error(e.getMessage(), e);
				}
			}
		}
		return false;
	}

	public int count() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConnection().prepareStatement("SELECT count(id) FROM ZK");
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.error("count zkCfg error : {}", e.getMessage());
			Logger.error(e.getMessage(), e);
		} finally {
			if (null != rs) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.error(e.getMessage(), e);
				}
			}
			if (null != ps) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.error(e.getMessage(), e);
				}
			}
		}
		return 0;
	}

	private boolean isTableOk() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConnection().prepareStatement("SELECT ID,DESC,CONNECTSTR,SESSIONTIMEOUT FROM ZK where 1=0");
			rs = ps.executeQuery();
			// if(rs.next()){
			// return rs.getInt(1);
			// }
			return true;
		} catch (SQLException e) {
			// e.printStackTrace();
			Logger.error("isTableOk Failed,{}", e.getMessage());
			try (PreparedStatement psps = getConnection().prepareStatement("drop table ZK")) {
				psps.execute();
			} catch (SQLException e1) {
				// e1.printStackTrace();
			}
			return false;
		} finally {
			if (null != rs) {
				try {
					rs.close();
				} catch (SQLException e) {
					// e.printStackTrace();
				}
			}
			if (null != ps) {
				try {
					ps.close();
				} catch (SQLException e) {
					// e.printStackTrace();
				}
			}
		}
	}

	// @PostConstruct
	@Override
	public void afterPropertiesSet() throws Exception {
		init();
		ZkCache.init(ZkCfgFactory.createZkCfgManager());
		Logger.info(" afterPropertiesSet init {} zk instance", ZkCache.size());
	}
}
