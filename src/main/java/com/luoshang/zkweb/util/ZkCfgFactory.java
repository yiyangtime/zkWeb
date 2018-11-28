package com.luoshang.zkweb.util;

import com.luoshang.zkweb.facade.ZkCfgManager;
import com.luoshang.zkweb.service.ZkCfgManagerImpl;

/**
 * zookeeper工厂类
 * 
 * @author LS
 * @date 2018年11月28日上午11:33:51
 */
public class ZkCfgFactory {
	private static ZkCfgManager _instance = new ZkCfgManagerImpl();
	public static ZkCfgManager createZkCfgManager() {
		return _instance;
	}

}
