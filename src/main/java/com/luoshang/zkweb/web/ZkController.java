package com.luoshang.zkweb.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.jr.ob.JSON;
import com.luoshang.zkweb.facade.ZkManager.PropertyPanel;
import com.luoshang.zkweb.model.Tree;
import com.luoshang.zkweb.model.TreeRoot;
import com.luoshang.zkweb.util.ZkCache;
import com.luoshang.zkweb.util.ZkCfgFactory;

@Controller
@RequestMapping("/zk")
public class ZkController implements DisposableBean {

	private static final Logger Logger = LoggerFactory.getLogger(ZkController.class);

	/**
	 * 查看zookeeper节点信息
	 * 
	 * @param path
	 * @param model
	 * @param cacheId
	 * @return
	 */
	@RequestMapping(value = "/queryZnodeInfo", produces = "text/html;charset=UTF-8")
	public String queryzNodeInfo(@RequestParam(required = false) String path, Model model,
			@RequestParam(required = true) String cacheId) {
		try {
			path = URLDecoder.decode(path, "utf-8");
			Logger.debug("queryzNodeInfo:{}",path);
			if (path != null) {
				model.addAttribute("zkpath", path);
				model.addAttribute("path", path);
				model.addAttribute("cacheId", cacheId);
				String data = ZkCache.get(cacheId).getData(path);
				if (data == null) {
					model.addAttribute("data", "");
					model.addAttribute("acls", Collections.emptyList());
					return "info";
				}
				model.addAttribute("data", ZkCache.get(cacheId).getData(path).trim());
				model.mergeAttributes(ZkCache.get(cacheId).getNodeMeta(path));
				model.addAttribute("acls", ZkCache.get(cacheId).getACLs(path));
			}
		} catch (Exception e) {
			Logger.debug("查看zookeeper节点信息异常:{}",e.getMessage());
			model.addAttribute("zkpath", path);
			model.addAttribute("path", path);
			model.addAttribute("cacheId", cacheId);
			model.addAttribute("data", "");
			model.addAttribute("acls", Collections.emptyList());
		}
		Logger.debug("model:{} ",model);
		return "info";
	}

	/**
	 * 查询zookeeper状态
	 * 
	 * @param model
	 * @param cacheId
	 * @return
	 */
	@RequestMapping(value = "/queryZKOk")
	@ResponseBody
	public String queryZKOk(Model model, @RequestParam(required = true) String cacheId) {
		String exmsg = "<font color='red'>断开连接或者异常</font>";
		try {
			if (ZkCache.get(cacheId).getData("/", false) != null) {
				Logger.debug("cacheId[{}] : {}", cacheId, "连接成功");
				return "<font color='blue'>连接成功</font>";
			} else {
				Logger.debug("cacheId[{}] : {}", cacheId, "断开连接或者异常");
				return exmsg;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.debug("cacheId[{}] : {}", cacheId, "断开连接或者异常");
		}
		return exmsg;
	}

	@RequestMapping(value = "/queryZKJMXInfo", produces = "application/json;charset=UTF-8")
	@ResponseBody
	public List<PropertyPanel> queryZKJMXInfo(@RequestParam(required = true) String simpleFlag,
			@RequestParam(required = true) String cacheId, HttpServletResponse response) {
		try {
			List<PropertyPanel> result = ZkCache.get(cacheId)
					.getJMXInfo(Integer.parseInt(simpleFlag) == 0 ? false : true);
			Logger.debug("queryZKJMXInfo simpleFlag={},cacheId={},result : {}", simpleFlag, cacheId,
					JSON.std.asString(result));
			response.addHeader("Access-Control-Allow-Origin", "*");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	/**
	 * 查询zookeeper节点
	 * 
	 * @param id
	 * @param path
	 * @param cacheId
	 * @return
	 */
	@RequestMapping(value = "/queryZnode")
	@ResponseBody
	public List<Tree> query(@RequestParam(required = false) String id,
			@RequestParam(required = false) String path, @RequestParam(required = true) String cacheId) {
		Logger.debug("id : {}", id);
		Logger.debug("path : {}", path);
		Logger.debug("cacheId : {}", cacheId);
		TreeRoot root = new TreeRoot();
		if (path == null) {
		} else if ("/".equals(path)) {
			root.remove(0);
			List<String> pathList = ZkCache.get(cacheId).getChildren(null);
			Logger.debug("list {}", pathList);
			for (String p : pathList) {
				Map<String, Object> atr = new HashMap<String, Object>();
				atr.put("path", "/" + p);
				Tree tree = new Tree(0, p, Tree.STATE_CLOSED, null, atr);
				root.add(tree);
			}
		} else {
			root.remove(0);
			try {
				path = URLDecoder.decode(path, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			List<String> pathList = ZkCache.get(cacheId).getChildren(path);
			for (String p : pathList) {
				Map<String, Object> atr = new HashMap<String, Object>();
				atr.put("path", path + "/" + p);
				Tree tree = new Tree(0, p, Tree.STATE_CLOSED, null, atr);
				root.add(tree);
			}
		}
		return root;
	}

	/**
	 * 保存数据
	 * 
	 * @param path
	 * @param data
	 * @param cacheId
	 * @return
	 */
	@RequestMapping(value = "/saveData", produces = "text/html;charset=UTF-8")
	@ResponseBody
	public String saveData(@RequestParam() String path, @RequestParam() String data,@RequestParam() String cacheId) {
		Logger.debug("cacheId---", cacheId);
		try {
			Logger.debug("data:{}", data);
			Logger.debug("path---"+path);
			return ZkCache.get(cacheId).setData(path, data) == true ? "保存成功" : "保存失败";
		} catch (Exception e) {
			Logger.debug("保存失败:{}", e.getMessage());
			return "保存失败!";
		}

	}

	/**
	 * 创建节点
	 * 
	 * @param path
	 * @param nodeName
	 * @param cacheId
	 * @return
	 */
	@RequestMapping(value = "/createNode", produces = "text/html;charset=UTF-8")
	public @ResponseBody String createNode(@RequestParam() String path, @RequestParam() String nodeName,
			@RequestParam(required = true) String cacheId) {

		try {
			Logger.debug("path:{}", path);
			Logger.debug("nodeName:{}", nodeName);
			return ZkCache.get(cacheId).createNode(path, nodeName, "") == true ? "保存成功" : "保存失败";
		} catch (Exception e) {
			Logger.debug("Error : {}", e.getMessage());
			e.printStackTrace();
			return "保存失败! Error : " + e.getMessage();
		}

	}

	/**
	 * 删除节点
	 * 
	 * @param path
	 * @param cacheId
	 * @return
	 */
	@RequestMapping(value = "/deleteNode", produces = "text/html;charset=UTF-8")
	public @ResponseBody String deleteNode(@RequestParam() String path, @RequestParam(required = true) String cacheId) {

		try {
			Logger.debug("path:{}", path);
			return ZkCache.get(cacheId).deleteNode(path) == true ? "删除成功" : "删除失败";
		} catch (Exception e) {
			Logger.debug("Error : {}", e.getMessage());
			e.printStackTrace();
			return "删除失败! Error : " + e.getMessage();
		}

	}

	/**
	 * 销毁zookeeper连接
	 */
	@Override
	public void destroy() throws Exception {
		Logger.debug("destroyZkCfgManager()...");
		ZkCfgFactory.createZkCfgManager().destroyPool();
	}

}
