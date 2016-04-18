package com.maoren.mock.server.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;

import com.alibaba.fastjson.JSON;

public class MockHandler extends AbstractHandler{
	private static final Logger LOG = Log.getLogger(MockHandler.class);
	
	Resource _baseResource;
	
	String suffix=".json";
	String message="";
	
	//初始化默认根路径 classpath中的mockserver
	public MockHandler() {
		super();
		_baseResource=Resource.newResource(this.getClass().getResource("/mockserver"));
	}
	
	

	@Override
	protected void doStart() throws Exception {
		super.doStart();
		LOG.info("mockhandler resource root path:{}",_baseResource.getName());
	}



	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		LOG.info("=====请求地址:{} 和方法[{}]", target,request.getMethod());
		response.setContentType("text/html;charset=utf-8");  
		response.setContentType(request.getContentType());
		
		Resource findResource=findByTarget(target,baseRequest,request,response);
		if (findResource==null) {
			//未找到留给下一个handler处理
			/*response.setStatus(HttpServletResponse.SC_NOT_FOUND);  
	        baseRequest.setHandled(true);  
	        response.setCharacterEncoding("UTF-8");
	        response.getWriter().write("<h1>request not fond<br>"+message+"</h1>");
	        response.flushBuffer();*/
	        return ;
		}
		
        response.setStatus(HttpServletResponse.SC_OK);  
        response.setCharacterEncoding("UTF-8");  
        response.setContentType("application/json; charset=utf-8");
        response.setHeader("X-User-Id", "2016");
        baseRequest.setHandled(true); 
        byte[] buf=new byte[1024];
        InputStream is = findResource.getInputStream();
        int i;
        while((i=is.read(buf))!=-1){
        	response.getOutputStream().write(buf,0,i);
        }
        response.flushBuffer();
	}
	
	//找到返回Resource,否则转给下一个
	//找到需要的服务[/serve1/1.0/data/ds/upload]
	private Resource findByTarget(String target,Request baseRequest,
			HttpServletRequest request,HttpServletResponse response) throws IOException{
		String[] urlframes=target.split("/");
		if (urlframes==null || urlframes.length<3) {
	        return null;
		}
		LOG.info("=====拆解请求{}",JSON.toJSONString(urlframes));
		
		String url_path=urlframes[1]+"/"+urlframes[2]+"/";
		String url_method_path=urlframes[1]+"/"+urlframes[2]+"/"+request.getMethod()+":";
		
		String url_name="";
		String url_method_name="";
		for (int i = 3; i < urlframes.length; i++) {
			url_name+=(urlframes[i]+"_");
			url_method_name+=(urlframes[i]+"_");
		}
		url_name=url_name.substring(0, url_name.length()-1);
		url_method_name=url_method_name.substring(0, url_method_name.length()-1);
		
		String url=url_path+url_name+suffix;
		String url_method=url_method_path+url_method_name+suffix;
		
		LOG.info("=====need to find [{}] and [{}]",url,url_method);
		Resource tempResource=_baseResource.getResource(url_method) ;
		
		if (!tempResource.exists() &&
				!(tempResource=_baseResource.getResource(url)).exists()) {
			message=url+" or <br>"+url_method+" not find!";
			return findByTarget2Reg(url_path, url_name, baseRequest, request, response);
		}
		return tempResource;
	}
	
	/**
	 * 通过正则查找
	 * @param target_url_path '/service1/1.0/'
	 * @param target_url_name 'checklist_subject_1';
	 * */
	private Resource findByTarget2Reg(String target_url_path,String target_url_name,Request baseRequest,
			HttpServletRequest request,HttpServletResponse response) throws IOException{
		String[] files=_baseResource.getResource(target_url_path).list();
		//找到根资源目录
		if (files!=null && files.length>1) {
			for (String file : files) {
				LOG.info("match for {}{}",target_url_path,file);
				Pattern pattern = Pattern.compile(file.split("\\.")[0]);
				Matcher matcher = pattern.matcher(target_url_name);
				if(matcher.matches()){
					Resource tempResource=_baseResource.getResource(target_url_path+file) ;
					return tempResource;
				}
			}
		}
		LOG.warn("mockerhandler not find request {}{}", target_url_path,target_url_name);
		return null;
	}
	
	/* ------------------------------------------------------------ */
    /**
     * 甚至资源地址为指定目录
     * @param resourceBase The base resource as a string.
     */
    public void setResourceBase(String resourceBase)
    {
        try
        {
            setBaseResource(Resource.newResource(resourceBase));
        }
        catch (Exception e)
        {
            LOG.warn(e.toString());
            LOG.debug(e);
            throw new IllegalArgumentException(resourceBase);
        }
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param base The resourceBase to set.
     */
    public void setBaseResource(Resource base)
    {
        _baseResource=base;
    }
    
    /**
     * 通过classpath解析请求返回值,默认为/mockserver
     * */
    public void setBaseResourceByClassPath(String classpath){
    	_baseResource=Resource.newResource(this.getClass().getResource(classpath));
    }
}
