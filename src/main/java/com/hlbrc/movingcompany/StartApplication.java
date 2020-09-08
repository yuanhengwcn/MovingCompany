package com.hlbrc.movingcompany;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.DispatcherServlet;

@SpringBootApplication
@EnableScheduling
@ComponentScan("com.hlbrc.movingcompany.*")
@MapperScan("com.hlbrc.movingcompany.mapper")//之前我们使用xml文件写sql语句，而我们现在使用注解，所以需要添加mapper注解扫描
public class StartApplication {
	public static void main(String[] args) {
		SpringApplication.run(StartApplication.class, args);
	}
    /**
     * 设置匹配*.action后缀请求
     * @param dispatcherServlet
     * @return
     */
    @Bean
    public ServletRegistrationBean<DispatcherServlet> servletRegistrationBean(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean<DispatcherServlet> servletServletRegistrationBean = new ServletRegistrationBean<>(dispatcherServlet);
        servletServletRegistrationBean.addUrlMappings("*.do");
        servletServletRegistrationBean.addUrlMappings("*.css");
        servletServletRegistrationBean.addUrlMappings("*.js");
        servletServletRegistrationBean.addUrlMappings("*.png");
        servletServletRegistrationBean.addUrlMappings("*.gif");
        servletServletRegistrationBean.addUrlMappings("*.ico");
        servletServletRegistrationBean.addUrlMappings("*.jpeg");
        servletServletRegistrationBean.addUrlMappings("*.jpg");
        return servletServletRegistrationBean;
    }
}