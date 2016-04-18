package com.maoren.mock.server;

import org.eclipse.jetty.server.AsyncNCSARequestLog;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.maoren.mock.server.handler.MockHandler;
import com.maoren.mock.server.util.NetUtil;

/**
 * Hello world!
 */
public class MockServer 
{
    public static void main( String[] args ) throws Exception
    {
        System.out.println( "welcome Mock server !" );
        //使用帮助
        if (args!=null) {
			for (String arg : args) {
				if (arg.equalsIgnoreCase("-h")) {
					printHelp();
				}
			}
		}
        
        Server server = new Server();
        
        //自定义端口
        int mockport=NetUtil.geLocletNotUsingPort();
        String proPort=System.getProperty("mockport");
        if (proPort!=null && !proPort.equals("")) {
        	mockport=Integer.parseInt(proPort);
		}
        //自定义请求文件路径
        String mockpath =System.getProperty("mockpath");
       
        //配置连接器
        ServerConnector http = new ServerConnector(server);
		http.setPort(mockport);
		//http.setHost("localhost");
		server.addConnector(http);
        
        //设置ssl连接器
        HttpConfiguration https_config = new HttpConfiguration();
        //https_config.setSecureScheme("http");
        //https_config.setSecurePort(8083);
        https_config.setOutputBufferSize(32768);
        https_config.addCustomizer(new SecureRequestCustomizer());
         
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath("keystore");
        sslContextFactory.setKeyStorePassword("OBF:1zep1u2a1sp11y7z1sop1u301zel");
        sslContextFactory.setKeyManagerPassword("OBF:1zep1u2a1sp11y7z1sop1u301zel");
         
        ServerConnector httpsConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory,"https/1.1"),
                new HttpConnectionFactory(https_config));
        //httpsConnector.setPort(8443);
        httpsConnector.setPort(mockport+1);
        httpsConnector.setIdleTimeout(500000);
        server.addConnector(httpsConnector);
        
        //log
  		NCSARequestLog requestLog = new AsyncNCSARequestLog();
  		requestLog.setFilename("./jetty-request-yyyy_mm_dd.log");
  		requestLog.setExtended(false);
  		RequestLogHandler requestLogHandler = new RequestLogHandler();
  		requestLogHandler.setRequestLog(requestLog);
  		
        //MockHandler，根据配置直接返回数据
        MockHandler mockHandler=new MockHandler();
        //如果有配置使用配置的，否则使用classpath下面mockserver目录
        if (mockpath!=null && !mockpath.equals("")) {
        	mockHandler.setResourceBase(mockpath);
		}
        //mockHandler.setResourceBase("/tmp/mock/");
        //mockHandler.setBaseResourceByClassPath("/mockserver");
        
        //springmvc的DispatcherServlet 
  		XmlWebApplicationContext applicationContext = new XmlWebApplicationContext();
  		applicationContext.setConfigLocations(new String[]{"classpath:mockserver/application.xml"});
  		
  		ServletContextHandler springMVCServletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
  		
  		DispatcherServlet servlet=new DispatcherServlet(applicationContext);

  		//springMVCServletHandler.setContextPath("/server1/1.0");
  		springMVCServletHandler.setContextPath("/");
  		springMVCServletHandler.addEventListener(new ContextLoaderListener(applicationContext));
  		springMVCServletHandler.addServlet(new ServletHolder(servlet), "/*");
        
        //ResourceHandler 返回静态文件
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setResourceBase("/tmp/");//支持访问文件列表
        
        HandlerCollection hc =new HandlerCollection();  
        hc.setHandlers(new Handler[]{requestLogHandler,mockHandler,springMVCServletHandler,resourceHandler});  
        server.setHandler(hc);  
        server.start();  
        server.join();  
    }
    
    public static void printHelp(){
    	System.out.println("\njava -Dmockport=8083 -Dmockapth=/tmp mockserver.jar -h");
    	System.out.println("   -Dmockport mockserver port");
    	System.out.println("   -Dmockapth mockserver path,this path will must like $path/service1/1.0/GET:aa/bb/cc.json\n");
    }
}
