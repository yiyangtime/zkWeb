package com.luoshang.zkweb.model;

import java.util.List;
import java.util.Map;
/**
 * 树
 * @author LS
 * @date 2018年11月28日上午11:22:38
 */
public class Tree {
	private int id;
	private String text;
	// state：节点状态，'open'或'closed'，默认为'open'。 设置为“关闭”时，节点具有子节点，并将从远程站点加载它们
	private String state;
	public static final String STATE_OPENNED = "open";
	public static final String STATE_CLOSED = "closed";
	private List<Tree> childern;
	// checked：指示是否选中了节点。
	private Boolean checked;
	// attributes：可以将自定义属性添加到节点
	private Map<String, Object> attributes;

	public Tree() {
		// TODO Auto-generated constructor stub
	}

	public Tree(int id, String text, String state, List<Tree> childern, Map<String, Object> attributes) {
		super();
		this.id = id;
		this.text = text;
		this.state = state;
		this.childern = childern;
		this.attributes = attributes;
	}

	public Tree(int id, String text, String state, List<Tree> childern) {
		super();
		this.id = id;
		this.text = text;
		this.state = state;
		this.childern = childern;
	}

	public Tree(int id, String text, String state, List<Tree> childern, boolean checked,
			Map<String, Object> attributes) {
		super();
		this.id = id;
		this.text = text;
		this.state = state;
		this.childern = childern;
		this.checked = checked;
		this.attributes = attributes;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<Tree> getChildern() {
		return childern;
	}

	public void setChildern(List<Tree> childern) {
		this.childern = childern;
	}

	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

}
