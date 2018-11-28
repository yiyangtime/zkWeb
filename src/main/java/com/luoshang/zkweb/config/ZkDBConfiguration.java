package com.luoshang.zkweb.config;

import java.beans.PropertyVetoException;
import java.util.Locale;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import com.mchange.v2.c3p0.ComboPooledDataSource;
/**
 * zookeeper数据库线程池配置
 * 
 * @author LS
 * @date 2018年11月28日下午2:20:12
 */
@Configuration
public class ZkDBConfiguration implements WebMvcConfigurer {
	@Autowired
	private Environment env;

	@Bean(name = "dataSource")
	@Qualifier(value = "dataSource")
	@Primary
	public DataSource getDataSource() {
		ComboPooledDataSource dataSource = DataSourceBuilder.create().type(ComboPooledDataSource.class).build();
		dataSource.setJdbcUrl(env.getProperty("spring.datasource.url"));
		dataSource.setUser(env.getProperty("spring.datasource.username"));
		dataSource.setPassword(env.getProperty("spring.datasource.password"));
		try {
			dataSource.setDriverClass(env.getProperty("spring.datasource.driver-class-name"));
		} catch (PropertyVetoException e) {
			return null;
		}
		dataSource.setInitialPoolSize(Integer.parseInt(env.getProperty("spring.datasource.initial-pool-size")));
		dataSource.setMinPoolSize(Integer.parseInt(env.getProperty("spring.datasource.min-pool-size")));
		dataSource.setMaxPoolSize(Integer.parseInt(env.getProperty("spring.datasource.max-pool-size")));
		dataSource.setAcquireIncrement(Integer.parseInt(env.getProperty("spring.datasource.acquire-increment")));
		dataSource.setIdleConnectionTestPeriod(Integer.parseInt(env.getProperty("spring.datasource.idle-connection-test-period")));
		dataSource.setMaxIdleTime(Integer.parseInt(env.getProperty("spring.datasource.max-idle-time")));
		dataSource.setMaxStatements(Integer.parseInt(env.getProperty("spring.datasource.max-statements")));
		dataSource.setAcquireRetryAttempts(Integer.parseInt(env.getProperty("spring.datasource.acquire-retry-attempts")));
		dataSource.setBreakAfterAcquireFailure(Boolean.parseBoolean(env.getProperty("spring.datasource.break-after-acquire-failure")));
		return dataSource;
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("home");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/resources/");
	}

	@Bean
	public SessionLocaleResolver localeResolver() {
		SessionLocaleResolver slr = new SessionLocaleResolver();
		// 默认语言
		slr.setDefaultLocale(Locale.CHINA);
		return slr;
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
		// 参数名
		lci.setParamName("lang");
		return lci;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}

	@Bean
	public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
		RequestMappingHandlerAdapter requestMappingHandlerAdapter = new RequestMappingHandlerAdapter();
		return requestMappingHandlerAdapter;
	}
}
