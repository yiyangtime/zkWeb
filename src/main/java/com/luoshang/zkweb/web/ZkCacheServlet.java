package com.luoshang.zkweb.web;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luoshang.zkweb.util.ZkCache;
import com.luoshang.zkweb.util.ZkCfgFactory;
import com.luoshang.zkweb.util.ZkManagerImpl;

@WebServlet(name = "cacheServlet", urlPatterns = "/cache/*")
public class ZkCacheServlet extends HttpServlet {

	private static final Logger log = LoggerFactory.getLogger(ZkCacheServlet.class);

	private static final long serialVersionUID = 1L;

	/**
	 * 默认构造函数
	 */
	public ZkCacheServlet() {
		super();
	}

	/**
	 * doGet请求
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * doPost请求
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		for (Map<String, Object> m : ZkCfgFactory.createZkCfgManager().query()) {
			log.info("ID : {},CONNECTSTR : {},SESSIONTIMEOUT : {}",
					new Object[] { m.get("ID"), m.get("CONNECTSTR"), m.get("SESSIONTIMEOUT") });
			ZkCache.put(m.get("ID").toString(), ZkManagerImpl.createZk().connect(m.get("CONNECTSTR").toString(),
					Integer.parseInt(m.get("SESSIONTIMEOUT").toString())));
		}

		for (String key : ZkCache.get_cache().keySet()) {
			log.info("key : {} , zk : {}", key, ZkCache.get(key));
		}
	}

	@Override
	public void init() throws ServletException {
		super.init();
	}

}
