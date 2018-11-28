package com.luoshang.zkweb.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luoshang.zkweb.facade.ZkCfgManager;
import com.luoshang.zkweb.facade.ZkManager;
import com.luoshang.zkweb.service.ZkManagerImpl;

/**
 * zookeeper缓存
 * 
 * @author LS
 * @date 2018年11月28日上午11:30:40
 */
public class ZkCache {
	private static final Logger Logger = LoggerFactory.getLogger(ZkCache.class);
	private static Map<String, ZkManager> _cache = new ConcurrentHashMap<String, ZkManager>();

	public static ZkManager put(String key, ZkManager zk) {
		return _cache.put(key, zk);
	}

	public static ZkManager get(String key) {
		return _cache.get(key);
	}

	public static ZkManager remove(String key) {
		return _cache.remove(key);
	}

	public static int size() {
		return _cache.size();
	}

	public static Map<String, ZkManager> get_cache() {
		return _cache;
	}

	public static void set_cache(Map<String, ZkManager> _cache) {
		ZkCache._cache = _cache;
	}

	/**
	 * 初始化zookeeper配置管理
	 * 
	 * @param cfgManager
	 */
	public static void init(ZkCfgManager cfgManager) {
		List<Map<String, Object>> list = cfgManager.query();
		Logger.debug("zk info size={}", list.size());
		for (Map<String, Object> m : list) {
			Logger.debug("zk info: id={},connectstr={},timeout={}", m.get("ID"), m.get("CONNECTSTR"),
					m.get("SESSIONTIMEOUT"));
			ZkCache.put(m.get("ID").toString(), ZkManagerImpl.createZk().connect(m.get("CONNECTSTR").toString(),
					Integer.parseInt(m.get("SESSIONTIMEOUT").toString())));
		}
	}

}
