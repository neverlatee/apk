package com.netease.lottery;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Servlet implementation class ApkSigner
 */
@WebServlet("/ApkSigner")
public class ApkSigner extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public ApkSigner() {
        // TODO Auto-generated constructor stub
    }
    private String filePath;    // 文件存放目录  
    private String tempPath;    // 临时文件目录  
 
    // 初始化  
    public void init(ServletConfig config) throws ServletException  
    {  
        super.init(config);  
       /* // 从配置文件中获得初始化参数  
        filePath = config.getInitParameter("filepath");  
        tempPath = config.getInitParameter("temppath");  */
 
        ServletContext context = getServletContext();  
 
        filePath = context.getRealPath("/filePath");  
        tempPath = context.getRealPath("/tempPath");
        System.out.println("文件存放目录、临时文件目录准备完毕 ..."); 
        System.out.println(filePath);
    }  

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);  
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/octet-stream"); 
		OutputStream out = response.getOutputStream();
//        PrintWriter pw = response.getWriter();  
        try{  
            DiskFileItemFactory diskFactory = new DiskFileItemFactory();  
            // threshold 极限、临界值，即硬盘缓存 1M  
            diskFactory.setSizeThreshold(4 * 1024);  
            // repository 贮藏室，即临时文件目录  
            diskFactory.setRepository(new File(tempPath));  
          
            ServletFileUpload upload = new ServletFileUpload(diskFactory);  
            // 设置允许上传的最大文件大小 4M  
            upload.setSizeMax(96* 1024 * 1024);  
            // 解析HTTP请求消息头  
            List fileItems = upload.parseRequest(request);  
            Iterator iter = fileItems.iterator();  
            while(iter.hasNext())  
            {  
                FileItem item = (FileItem)iter.next();  
                if(item.isFormField())  
                {  
                    System.out.println("处理表单内容 ...");  
                    processFormField(item);  
                }else{  
                    System.out.println("处理上传的文件 ...");  
                    String filename=processUploadFile(item); 
                    //读取配置文件
                    Properties pro=new Properties();
//                  InputStream inputStream=this.getClass().getClassLoader().getResourceAsStream( "/test.properties" ); 
                    InputStream inputStream=new FileInputStream("D:\\test.properties");
                    pro.load(inputStream);
                    String keystore=pro.getProperty("key.store");
                    String storepass=pro.getProperty("key.store.password");
                    String keypass = pro.getProperty("key.alias.password");
                    String keyalias = pro.getProperty("key.alias");
                    System.out.println(keystore);
                    System.out.println(storepass);
                    System.out.println(keypass);
                    System.out.println(keyalias);
                    //执行签名命令
                    String newApkPath =filePath+"\\"+filename.substring(0,filename.lastIndexOf("."))+"_product.apk";
                    String oldApkPath =filePath+"\\"+filename;
                    String command="jarsigner -verbose -digestalg SHA1 -sigalg MD5withRSA -keystore "+keystore
                    				+" -storepass "+storepass+" -keypass "+keypass+" -signedjar "+"\""+newApkPath+"\""+" "+"\""+oldApkPath+"\""+" "+keyalias;
                    System.out.println(command);
                    Runtime run = Runtime.getRuntime();
                    Process process=run.exec(command);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));  
                    String lineStr;  
                    System.out.println("执行命令完成，打印输出信息");
                    while ((lineStr = bufferedReader.readLine()) != null)  
                        //获得命令执行后在控制台的输出信息  
                        System.out.println(lineStr);// 打印输出信息  
                    //检查命令是否执行失败。  
                    if (process.waitFor() != 0) {  
                        if (process.exitValue() == 1)//p.exitValue()==0表示正常结束，1：非正常结束  
                            System.err.println("命令执行失败!");  
                    }  
                    bufferedReader.close();
                    response.addHeader("Content-Disposition", "attachment; filename=" + filename.substring(0,filename.lastIndexOf("."))+"_product.apk");
                    FileInputStream fileInputStream=new FileInputStream(newApkPath);
                    BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream);
                    int size=0;
                    byte[] b=new byte[4096];
                    while ((size=bufferedInputStream.read(b))!=-1) {
                    	out.write(b, 0, size);
                    }
                    bufferedInputStream.close();
                }  
            }// end while()
 
            out.close();  
        }catch(Exception e){  
            System.out.println("使用 fileupload 包时发生异常 ...");  
            e.printStackTrace();  
        }// end try ... catch ...  
    }// end doPost()  
	 // 处理表单内容  
    private void processFormField(FileItem item)//, PrintWriter pw)  
        throws Exception  
    {  
        String name = item.getFieldName();  
        String value = item.getString();          
//        pw.println(name + " : " + value + "\r\n");  
    }  
    //处理上传的文件  
    private String processUploadFile(FileItem item)//, OutputStream pw)  
    		throws Exception {  
	    // 此时的文件名包含了完整的路径，得注意加工一下  
	    String filename = item.getName();         
	    System.out.println("完整的文件名：" + filename);  
	    int index = filename.lastIndexOf("\\");  
	    filename = filename.substring(index + 1, filename.length());  
	
	    long fileSize = item.getSize();  
	
	    if("".equals(filename) && fileSize == 0)  
	    {             
	        System.out.println("文件名为空 ...");  
	        return "";  
	    }  
	
	    File uploadFile = new File(filePath + "/" + filename);  
	    item.write(uploadFile);
//	    pw.println(filename + " 文件保存完毕 ...");  
//	    pw.println("文件大小为 ：" + fileSize + "\r\n");  
	    return filename;
	    
    }  

}
