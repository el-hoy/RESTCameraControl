/**
 * 
 */
package org.restcameracontrol.app.init;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.restcameracontrol.config.SpringMvcContextConfig;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author rgarcia
 * 
 *         Explicaci�n de como funciona este tinglado:
 *         http://www.concretepage.com
 *         /spring-4/spring-4-rest-web-service-json-example-tomcat
 * 
 *         WebApplicationInitializer is used in place of web.xml
 * 
 *         All the web setting will be done in a class which will implement
 *         WebApplicationInitializer (CameraControlAppInitializer). On startup,
 *         server looks for WebApplicationInitializer. If server finds it in the
 *         application, then server starts the application using the settings
 *         defined in CameraControlAppInitializer implementing class.
 */
public class CameraControlAppInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		
		// Creamos el contexto de SPRING donde van a estar todos los
		// objetos, con anotaciones
		AnnotationConfigWebApplicationContext mvcContext = new AnnotationConfigWebApplicationContext();
		// La definición de los objetos de SPRING está en la clase
		// SpringMvcContextConfig (org.RESTCameraControl.config)
		mvcContext.register(SpringMvcContextConfig.class);
		mvcContext.setServletContext(servletContext);
		
		// Declaramos el DISPATCHER --> Envia peticiones al CONTROLER
		DispatcherServlet dispatcherServlet = new DispatcherServlet(mvcContext);
		dispatcherServlet.setDispatchOptionsRequest(true);
		ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", dispatcherServlet);
		
		// Mapeamos todas las posibles peticiones
		dispatcher.addMapping("/"); 
		dispatcher.setLoadOnStartup(1);
	}

}
