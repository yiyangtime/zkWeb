package com.luoshang.zkweb.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestMail
{
    // 默认密码
    private String pwd = "test123";
    // 默认目的邮件
    private String email = "XX@qq.com";
    // 默认状态
    private String state = "0";
    // 修改状态
    private String state1 = "1";
    // Md5秘钥
    private String MD5key = "5482a1bea452412e817d14e93383f359";
    // 标记(用于是否为本服务器发送,一般建议MD5加密,接收校验,此处为简化写了一个固定值)
    private String sign = "mail";
    // 发送邮件的账号(阿里的)
    private String MailFrom = "xx@xx.xx";
    // 发送邮件的账号的密码
    private String MailPwd = "xxx";
    // 邮件标题
    private String MailTitle = "测试邮件";

    // 发送注册邮件,此处页面是两个框(邮箱,密码,确认密码)
    @RequestMapping("/mail/send")
    public String mailMain(@RequestParam("pwd") String Mpwd, @RequestParam("email") String Memail)
    {
	String ret = "";
	// 是否已存在
	if (Memail.equals(email))
	{
	    System.out.println("已存在本账号:" + Memail);
	    ret = "<h2>账号已存在,<a href=\"http://192.168.1.170:8020/use/login.html\"> 请登录 </a> 或者<a href=\"http://192.168.1.170:8020/use/Forget.html\"> 忘记密码 </a></h2>";
	} else
	{
	    // 利用正则表达式(可改进)验证邮箱是否符合邮箱的格式
	    if (!Memail.matches("^\\w+@(\\w+\\.)+\\w+$"))
	    {
		System.out.println("邮箱格式不正确 !!!");
		ret = "邮箱格式不正确";
	    }
	    System.out.println("开始注册 !!!");
	    System.out.println("开始发送邮件 !!!"); // 发送注册邮件
	    // 发送邮件
	    StringBuffer sb = new StringBuffer("点击下面链接激活账号,48小时生效,否则重新注册账号,链接只能使用一次,请尽快激活!</br>");
	    sb.append("<a href=\"http://127.0.0.1:8081/mail/register?email=");
	    sb.append(Memail);
	    sb.append("&state=");
	    sb.append(state);
	    sb.append("&sign=");
	    sb.append(sign);
	    sb.append("&pwd=");
	    sb.append(Mpwd);
	    sb.append("\">点击激活");
	    sb.append("</a>");
//	    JdkUtils.sendMail(MailFrom, Memail, MailFrom, MailPwd, MailTitle, sb.toString());
	    System.out.println("邮件发送完毕!!!");
	    ret = "<h2>邮件已发送!!<h2>";
	}
	return ret;
    }

    // 点击注册
    @RequestMapping("/mail/register")
    public String register(@RequestParam("sign") String Msign, HttpServletRequest request,@RequestParam("pwd") String Mpwd, @RequestParam("email") String Memail, @RequestParam("state") String state)
    {
	String ret = "";
	// 是否本服务器邮件
	// 获取全部已验证邮件正确性
	// 未实现
	if (Msign.equals(sign))
	{ // 修改状态为注册 state = "1";
	    System.out.println("注册成功,状态修改成功!!!");
	    StringBuffer sb = new StringBuffer();
	    sb.append("<h2><a href=\"http://192.168.1.170:8020/use/login.html\">注册成功,点击登录</a></h2>");
	    ret = sb.toString();
	} else
	{
	    System.out.println("修改了邮件中链接的内容 !!!");
	    ret = "请勿修改邮件中链接的内容 !!!";
	}
	return ret;
    }

    // 登录,此处页面是两个框(邮箱,密码)
    @RequestMapping("/mail/login")
    public String login(@RequestParam("email") String Memail, @RequestParam("pwd") String Mpwd)
    {
	System.out.println("开始登陆!!!");
	// 此处默认从数据库取得状态为已注册,生产环境需从数据库取值
	String Mstate = "1";
	String ret = "";
	if (Memail.equals(email) && Mpwd.equals(pwd) && Mstate.equals(state1))
	{
	    System.out.println("登陆成功!!!");
	    ret = "登录成功";
	} else
	{
	    // 用户名是否存在
	    if (Memail.equals(email))
	    {
		System.out.println("密码错误!!!");
		ret = "密码错误";
	    } else
	    {
		System.out.println("用户名不存在!!!");
		ret = "用户名不存在,请前去<a href=\"http://192.168.1.170:8020/use/Registered.html\">注册</a>";
	    }
	}
	return ret;

    }

    // 忘记密码,此处页面是一个框(邮箱)
    @RequestMapping("/mail/forget")
    public String forget(@RequestParam("email") String Memail) throws Exception
    {
	String ret = "";
	System.out.println("忘记密码开始修改密码!!!");
	// 数据库查询是否有此邮箱且已注册
	String state = "1";
	if (Memail.equals(email) && state.equals(state1))
	{
	    System.out.println("开始修改密码,发送邮件!!!");
	    // 发送邮件
	    StringBuffer sb = new StringBuffer("点击下面链接修改密码,48小时生效,否则重新修改,链接只能使用一次,请尽快修改!</br>");
	    sb.append("<a href=\"http://192.168.1.170:8020/use/UpdatePwd.html?email=");
	    sb.append(Memail);
	    sb.append("&sign=");
	    sb.append(sign);
	    sb.append("\">点击激活");
	    sb.append("</a>");
//	    JdkUtils.sendMail(MailFrom, Memail, MailFrom, MailPwd, MailTitle, sb.toString());
	    System.out.println("邮件发送完毕!!!");
	    ret = "";
	} else
	{
	    System.out.println("未找到邮箱!!!");
	    ret = "未找到邮箱,前往<a href=\"http://192.168.1.170:8020/use/Registered.html\">注册</a>>";
	}
	return ret;
    }

    // 点击修改密码,此处页面是两个框(邮箱,密码,确认密码)
    @RequestMapping("/mail/update")
    public String update(@RequestParam("email") String Memail, @RequestParam("pwd") String pwd,@RequestParam("sign") String Msign)
    {
	System.out.println("开始修改密码!!!");
	String ret = "";
	if (Memail.equals(email) && Msign.equals(sign))
	{
	    // 修改数据库的密码
	    pwd = MailPwd;
	    ret = "<h3>密码修改完成,请<a href=\"http://192.168.1.170:8020/use/login.html\"></a></h3>";
	    System.out.println("修改密码完成!!!");
	} else
	{
	    // 校验码sign不对
	    System.out.println("校验码不对!!!");
	    ret = "<h3>校验码不正确,请勿修改链接内容,请返回邮箱重新进入</h3>";
	}
	return ret;
    }
}
