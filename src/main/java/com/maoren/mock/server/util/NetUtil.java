package com.maoren.mock.server.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetUtil {
	
	/***
	 *  true:already in using  false:not using 
	 * @param port
	 */
	public static boolean isLoclePortUsing(int port){
		boolean flag = true;
		try {
			flag = isPortUsing("127.0.0.1", port);
		} catch (Exception e) {
			
		}
		return flag;
	}
	
	//找到本机没有被使用待端口
	public static int geLocletNotUsingPort(){
		int port= 8080;
		while(isLoclePortUsing(port)){
			port++;
		}
		return port;
	}
	
	//找到本地有效地址数组
	public static int[] geLocletNotUsingPort(int num){
		int[] ports=new int[num];
		int port= 8080;
		while(num>0){
			while(isLoclePortUsing(port)){
				port++;
			}
			ports[num-1]=port;
			port++;
			num--;
			
		}
		return ports;
	}
	/***
	 *  true:already in using  false:not using 
	 * @param host
	 * @param port
	 * @throws UnknownHostException 
	 */
	public static boolean isPortUsing(String host,int port) throws UnknownHostException{
		boolean flag = false;
		InetAddress theAddress = InetAddress.getByName(host);
		try {
			Socket socket = new Socket(theAddress,port);
			socket.close();
			flag = true;
		} catch (IOException e) {
			
		}
		return flag;
	}
}

