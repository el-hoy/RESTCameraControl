
/**
 * K- En lugar de tener un archivo XML definiendo los BEANS los tenemos en código JAVA en este fichero 
 */
package org.restcameracontrol.config;

import java.util.Hashtable;

import org.restcameracontrol.beans.CameraInfo;
import org.restcameracontrol.loggers.GPLogger;
import org.restcameracontrol.services.CameraService;
import org.restcameracontrol.services.FileService;
import org.restcameracontrol.services.TaskService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author rgarcia
 * 
 *         Spring configuration will not use XML approach. @Configuration
 *         supports a class to define the spring configuration for component and
 *         beans used in the application.
 *         
 * 
 */
@Configuration
@EnableWebMvc
@PropertySource(value = { "classpath:/restcameracontrol.properties" })
@ComponentScan(basePackages = { "org.restcameracontrol.controllers" })
public class SpringMvcContextConfig {

	@Bean(name = "autowiredAnnotationBeanPostProcessor")
	public org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor() {
		return new org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor();
	}
	
	@Bean(name="gpLogger")
	public GPLogger gpLogger()
	{
		return new GPLogger();
	}
	
	@Bean(name="cameras")
	@DependsOn(value="gpLogger")
	public Hashtable<Integer, CameraInfo> cameras()
	{
		return new Hashtable<Integer, CameraInfo>();
	}
	
	@Bean(name="cameraService")
	@DependsOn(value="cameras")
	public CameraService cameraService()
	{
		return new CameraService(gpLogger());
	}
	
	@Bean(name="fileService")
	@DependsOn(value="gpLogger")
	public FileService fileService()
	{
		return new FileService();
	}
	
	@Bean(name="taskService")
	@DependsOn(value="cameraService")
	public TaskService taskService()
	{
		return new TaskService();
	}
	
}
