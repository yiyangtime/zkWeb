$(function() {
	initDataGrid();
	$('#millisecs').numberspinner({
		onSpinUp : function() {
			ZkStateRefresh(null)
		},
		onSpinDown : function() {
			ZkStateRefresh(null)
		}
	});
	$('#locale').combobox({
		width : "70px",
		height : "18px",
		panelHeight : "70px",
		editable : false,
		onSelect : function(record) {
			var isdelwelcome = $('#isfirstopen').attr('value')
			if (isdelwelcome == "0") {
				$('#isfirstopen').attr('value', "1");
			} else {
				setLanguage(record)
			}
		}
	});
	$('#locale').combobox('select', getLanguageFromCookie(null));
});

/**
 * 设置过滤
 * 
 * @param node
 * @returns
 */
function setFilter(node) {
	var _cfg = $('#zkweb_zkcfg').datagrid('getSelected');
	var _index = $('#zkweb_zkcfg').datagrid('getRowIndex', _cfg);
	$('#selectIndex').val(_index)
	$('#zkweb_zkcfg').datagrid('options').url = 'zkcfg/queryZkCfg?whereSql='+ encodeURI(encodeURI($('#filterValue').val())).trim()
	$('#zkweb_zkcfg').datagrid("reload");

}

/**
 * 初始化zookeeper数据表格
 * 
 * @returns
 */
function initDataGrid() {
	$('#zkweb_zkcfg').datagrid({
		sortName : "DESC",
		striped : true,
		columns : [[{
			field : "ID",
			title : "ID",
			sortable : true
		},{
			field : "DESC",
			title : "描述",
			sortable : true
		},{
			field : "CONNECTSTR",
			title : "连接IP及端口",
			sortable : true
		},{
			field : "SESSIONTIMEOUT",
			title : "会话超时时间[ms]",
			sortable : true
		}]],
		remoteSort : false,
		onLoadSuccess : function(data) {
			$('#zkweb_zkcfg').datagrid('selectRow',$('#selectIndex').val());
		},
		onClickRow : function(rowIndex, rowData) {
			initTree(rowIndex, rowData);
			// 设置选中
			var _index = $('#zkweb_zkcfg').datagrid('getRowIndex', rowData);
			$('#selectIndex').val(_index);
			$('#zkweb_zkcfg').datagrid('selectRow',$('#selectIndex').val());
			// 重置tab页面
			$('#zkTab').tabs('select', rowData.DESC);
			var isdelwelcome = $('#isDelWelcomeTab').attr('value');
			if (isdelwelcome == "0") {
				$('#zkTab').tabs('close', 0);
				$('#isDelWelcomeTab').attr('value', "1");
			}
			// 重置状态页面
			var url = "zk/queryZKJMXInfo?cacheId="+ rowData.ID+ "&simpleFlag="+ $('#zkstate_showtype_form input[name="showtype"]:checked ').val();
			$('#jmxpropertygrid').propertygrid({url : url});
			$('#zkstate_showtype_form input[name="id"]').val(rowData.ID);
			$('#jmxpanel').remove();
			refreshConnectState(rowData);
		},
		url:'zkcfg/queryZkCfg?whereSql='+ encodeURI(encodeURI($('#filterValue').val())).trim()
	});
}

/**
 * 刷新zookeeper连接状态
 * 
 * @param row
 * @returns
 */
function refreshConnectState(row) {
	$.post("zk/queryZKOk", {cacheId : row.ID}, function(data) {
		$('#connstaterefresh').html(data);
	});
	if ($('#lastRefreshConn').val()) {
		clearInterval($('#lastRefreshConn').val());
		$('#lastRefreshConn').val(null);
	}
	ref = setInterval(function() {
		$.post("zk/queryZKOk", {cacheId : row.ID}, function(data) {
			$('#connstaterefresh').html(data);
		});
	}, 5000);
	$('#lastRefreshConn').val(ref);
}

/**
 * zookeeper状态展示类型变化（简单，详细）
 * 
 * @param node
 * @returns
 */
function ZkStateShowTypeChange(node) {
	var url = "zk/queryZKJMXInfo?cacheId="+ $('#zkstate_showtype_form input[name="id"]').val()+ "&simpleFlag="
			+ $('#zkstate_showtype_form input[name="showtype"]:checked ').val();
	$('#jmxpropertygrid').propertygrid({
		url : url,
	});
}

/**
 * zookeeper状态刷新
 * 
 * @param node
 * @returns
 */
function ZkStateRefresh(node) {
	var url = "zk/queryZKJMXInfo?cacheId="+ $('#zkstate_showtype_form input[name="id"]').val()+ "&simpleFlag="
			+ $('#zkstate_showtype_form input[name="showtype"]:checked ').val();
	var secs = $('#zkstate_showtype_form input[name="millisecs"]').val() * 1000;
	var ref = null;
	if (secs > 0) {
		var refreshObject = $('#zkstate_showtype_form input[name="refreshObject"]').val();
		if (refreshObject) {
			clearInterval(refreshObject);
			$('#zkstate_showtype_form input[name="refreshObject"]').val(null);
		}
		ref = setInterval(function() {
			$('#jmxpropertygrid').propertygrid({
				url : url,
			});
		}, secs);
		$('#zkstate_showtype_form input[name="refreshObject"]').val(ref);
	} else {
		$('#jmxpropertygrid').propertygrid({
			url : url,
		});
		var refreshObject = $(
				'#zkstate_showtype_form input[name="refreshObject"]').val();
		if (refreshObject) {
			clearInterval(refreshObject);
			$('#zkstate_showtype_form input[name="refreshObject"]').val(null);
		}
	}

}

/**
 * 初始节点信息
 * 
 * @param rowIndex
 * @param row
 * @returns
 */
function initTree(rowIndex, row) {
	$('#zkTab').tabs({
		onSelect : function(title, index) {
			var pp = $(this).tabs('getTab', title);
			var pa = pp.panel('options');
			if (pp != null && pa.id != null) {
				$('#zkweb_zkcfg').datagrid('unselectAll');
				$('#zkweb_zkcfg').datagrid('selectRow', pa.id);
				var _cfg = $('#zkweb_zkcfg').datagrid('getSelected');
				initOneTree(pa.id, _cfg);
				$('#zkstate_showtype_form input[name="id"]').val(_cfg.ID);
				ZkStateRefresh(null);
				refreshConnectState(_cfg);
				var rootNode = $('#zkTree').tree('getRoot');
				if (rootNode == null) {
					localeMessager('alert', 'title', '提示','connstatedisconn', '连接未建立！');
				}
			}
		}
	});
	initOneTree(rowIndex, row);
}

/**
 * 初始化树形结构图
 * 
 * @param rowIndex
 * @param row
 * @returns
 */
function initOneTree(rowIndex, row) {
	cacheId = row.ID
	$('#zkTree').tree({
		checkbox : false,
		url : "zk/queryZnode?cacheId=" + cacheId,
		animate : true,
		lines : true,
		onLoadSuccess : function(node, data) {// node为加载完毕的父节点,data是加载好的子节点列表
			// 下面的代码是递归全部展开整颗树
			var t = $(this);
			if (data) {
				$(data).each(function(index, d) {
					if (this.state == 'closed') {
						t.tree('expandAll');
					}
				});
			}
			var rootNode = $(this).tree('getRoot');
			if (rootNode == null) {
				localeMessager('alert', 'title', '提示','connstatedisconn', '连接未建立！');
				return;
			}
			var curNode = $(this).tree('getSelected');
			if (!node) {
				node = rootNode;
			}
			$(this).tree('select', node.target);
			$(this).tree('expand', node.target);
			if (node != rootNode && node == curNode) {
				return;
			}
			var _path = "/";
			if (node) {
				if (node.attributes) {
					_path = node.attributes.path;
				}
			}
			var tab = $('#zkTab').tabs('getTab', row.DESC);
			if (tab != null) {
				tab.panel('refresh', "zk/queryZnodeInfo?path="+ encodeURI(encodeURI(_path))+ "&cacheId=" + cacheId);
			} else {
				$('#zkTab').tabs('add',{
					id : rowIndex,
					title : row.DESC,
					closable : true,
					href : "zk/queryZnodeInfo?path="+ encodeURI(encodeURI(_path))+ "&cacheId="+ cacheId
				});

			}
		},
		onContextMenu : function(e, node) {
			e.preventDefault();
			$(this).tree('select', node.target);
			$('#mm').menu('show', {
				left : e.pageX,
				top : e.pageY
			});
		},
		onClick : function(node) {
			var tab = $('#zkTab').tabs('getTab', row.DESC);
			var _path = "/"
			if (node && node.attributes)
			_path = node.attributes.path;
			if (tab != null) {
				tab.panel('refresh', "zk/queryZnodeInfo?path="+ encodeURI(encodeURI(_path))+ "&cacheId=" + cacheId);
			} else {
				$('#zkTab').tabs('add',{
					id : rowIndex,
					title : row.DESC,
					closable : true,
					href : "zk/queryZnodeInfo?path="+ encodeURI(encodeURI(_path))+ "&cacheId="+ cacheId
				});
			}
		},
		onBeforeExpand : function(node, param) {
			if (node.attributes != null) {
				$('#zkTree').tree('options').url = "zk/queryZnode?path="+ encodeURI(encodeURI(node.attributes.path))+ "&cacheId=" + cacheId;
			}
		}
	});
}

/**
 * 删除
 * 
 * @returns
 */
function remove() {
	var node = $('#zkTree').tree('getSelected');
	if (!node) {
		localeMessager('alert', 'title', "提示", 'nochoosenode', '没选择节点！');
		return;
	}
	var parentNode = $('#zkTree').tree('getParent', node.target);
	if (!parentNode) {
		parentNode = $('#zkTree').tree('getRoot');
	}
	if (node) {
		if ('/' == node.attributes.path || '/zookeeper' == node.attributes.path || '/zookeeper/quota' == node.attributes.path) {
			localeMessager('alert', 'title', '提示', 'canntdelnode', '不能删除此节点！');
			return;
		}
		var _cfg = $('#zkweb_zkcfg').datagrid('getSelected');
		if (_cfg) {
			localeMessager('confirm','title','提示','none','删除此节点及其所有的子节点: '+ node.attributes.path + ' ?',function(r) {
				if (r) {
					if (node.attributes) {
						_path = node.attributes.path;
						$.post("zk/deleteNode",{
							path : _path,
							cacheId : _cfg.ID
						},
						function(data) {
							localeMessager('alert','title','提示','none',data+ ',删除完成!');
							node = parentNode;
							$('#zkTree').tree('reload',node.target);
							$('#zkTree').tree('collapse',node.target);
							$('#zkTree').tree('expand',node.target);
							$('#zkTree').tree('select',node.target);
							var tab = $('#zkTab').tabs('getTab',_cfg.DESC);
							cacheId = _cfg.ID;
							tab.panel('refresh',"zk/queryZnodeInfo?path="+ encodeURI(encodeURI(node.attributes.path))+ "&cacheId="+ cacheId);
						});

					}
				}
			});
		}
	} else {
		localeMessager('alert', 'title', '提示', 'choosenode', '请选择一个需要删除的节点');
	}
}

/**
 * 全部收缩
 * 
 * @returns
 */
function collapseAll() {
	var node = $('#zkTree').tree('getSelected');
	$('#zkTree').tree('collapse', node.target);
	collapseAllRecur(node);
}

/**
 * 部分收缩
 * 
 * @param node
 * @returns
 */
function collapseAllRecur(node) {
	var childNodeList = $('#zkTree').tree('getChildren', node.target);
	if (!childNodeList) {
		return;
	}
	for (var i = 0; i < childNodeList.length; i++) {
		$('#zkTree').tree('collapse', childNodeList[i].target);
		collapseAllRecur(childNodeList[i]);
	}
}

/**
 * 收缩
 * 
 * @returns
 */
function collapse() {
	var node = $('#zkTree').tree('getSelected');
	$('#zkTree').tree('collapse', node.target);
}

/**
 * 全部展开
 * 
 * @returns
 */
function expandAll() {
	var node = $('#zkTree').tree('getSelected');
	$('#zkTree').tree('expand', node.target);
	expandAllRecur(node);
}

/**
 * 部分展开
 * 
 * @param node
 * @returns
 */
function expandAllRecur(node) {
	var data = $('#zkTree').tree('getData', node.target);
	var childNodeList = data.children;
	if (!childNodeList) {
		return;
	}
	for (var i = 0; i < childNodeList.length; i++) {
		$('#zkTree').tree('expand', childNodeList[i].target);
		expandAllRecur(childNodeList[i]);
	}
}

/**
 * 展开
 * 
 * @returns
 */
function expand() {
	var node = $('#zkTree').tree('getSelected');
	$('#zkTree').tree('expand', node.target);
}

/**
 * 添加zookeeper节点
 * 
 * @returns
 */
function addzkNode() {
	var _path = "/";
	var node = $('#zkTree').tree('getSelected');
	if (node) {
		if (node.attributes) {
			_path = node.attributes.path;
		}
	} else {
		localeMessager('alert', 'title', '提示', 'nochoosenode', '没选择节点！');
		return;
	}
	_nodeName = $('#zkNodeName').val();
	var _cfg = $('#zkweb_zkcfg').datagrid('getSelected');
	if (_cfg) {
		$.post("zk/createNode", {
			nodeName : _nodeName,
			path : _path,
			cacheId : _cfg.ID
		},
		function(data) {
			localeMessager('alert', 'title', '提示', 'none', data+ ',Add Done!');
			$('#zkweb_add_node').window('close');
			$('#zkTree').tree('reload', node.target);
			$('#zkTree').tree('collapse', node.target);
			$('#zkTree').tree('expand', node.target);
		});
	} else {
		localeMessager('alert', 'title', '提示', 'mustchoosecfg', '你必须选择一个配置');
	}
}

/**
 * 添加zookeeper配置
 * 
 * @returns
 */
function saveCfg() {
	$.messager.progress();
	$('#zkweb_add_cfg_form').form('submit',{
		url : 'zkcfg/addZkCfg',
		onSubmit : function() {
			var isValid = $(this).form('validate');
			if (!isValid) {
				$.messager.progress('close'); 
			}
			return isValid;
		},
		success : function(data) {
			localeMessager('alert', 'title', '提示', 'none', data+ ',保存完成!');
			$('#zkweb_zkcfg').datagrid("reload");
			$('#zkweb_add_cfg').window('close');
			$.messager.progress('close'); 
			$('#zkTab').tabs('close', 0);
		}
	});
}

/**
 * 更新zookeeper配置
 * 
 * @returns
 */
function updateCfg() {
	$.messager.progress();
	$('#zkweb_up_cfg_form').form('submit',{
		url : 'zkcfg/updateZkCfg',
		onSubmit : function() {
			var isValid = $(this).form('validate');
			if (!isValid) {
				$.messager.progress('close');
			}
			return isValid; 
		},
		success : function(data) {
			localeMessager('alert', 'title', '提示', 'none', data+ ',更新完成!');
			$('#zkweb_zkcfg').datagrid("reload");
			$('#zkweb_up_cfg').window('close');
			$.messager.progress('close'); 
			$('#zkTab').tabs('close', 0);
		}
	});
}

/**
 * 打开更新窗口
 * 
 * @returns
 */
function openUpdateWin() {
	var _cfg = $('#zkweb_zkcfg').datagrid('getSelected');
	if (_cfg) {
		$('#zkweb_up_cfg').window('open');
		$('#zkweb_up_cfg_form').form("load","zkcfg/queryZkCfgById?id=" + _cfg.ID);
	} else {
		localeMessager('alert', 'title', '提示', 'chooserow', '请选择一条zookeeper配置');
	}

}

function openDelWin() {
	var _cfg = $('#zkweb_zkcfg').datagrid('getSelected');
	if (_cfg) {
		localeMessager('confirm', 'title', '提示', 'confirmdelcfg', '确认删除这个配置吗?',function(r) {
			if (r) {
				$.get('zkcfg/delZkCfg', {
					id : _cfg.ID
				}, 
				function(data) {
					localeMessager('alert', 'title', '提示', 'none', data+ ',删除完成!');
				});
				$('#zkweb_zkcfg').datagrid("reload");
				$('#zkTab').tabs('close', 0);
			}
		});
	} else {
		localeMessager('alert', 'title', '提示', 'chooserow', '请选择一条zookeeper配置');
	}
}