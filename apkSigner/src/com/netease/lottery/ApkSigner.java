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
    private String filePath;    // �ļ����Ŀ¼  
    private String tempPath;    // ��ʱ�ļ�Ŀ¼  
 
    // ��ʼ��  
    public void init(ServletConfig config) throws ServletException  
    {  
        super.init(config);  
       /* // �������ļ��л�ó�ʼ������  
        filePath = config.getInitParameter("filepath");  
        tempPath = config.getInitParameter("temppath");  */
 
        ServletContext context = getServletContext();  
 
        filePath = context.getRealPath("/filePath");  
        tempPath = context.getRealPath("/tempPath");
        System.out.println("�ļ����Ŀ¼����ʱ�ļ�Ŀ¼׼����� ..."); 
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
            // threshold ���ޡ��ٽ�ֵ����Ӳ�̻��� 1M  
            diskFactory.setSizeThreshold(4 * 1024);  
            // repository �����ң�����ʱ�ļ�Ŀ¼  
            diskFactory.setRepository(new File(tempPath));  
          
            ServletFileUpload upload = new ServletFileUpload(diskFactory);  
            // ���������ϴ�������ļ���С 4M  
            upload.setSizeMax(96* 1024 * 1024);  
            // ����HTTP������Ϣͷ  
            List fileItems = upload.parseRequest(request);  
            Iterator iter = fileItems.iterator();  
            while(iter.hasNext())  
            {  
                FileItem item = (FileItem)iter.next();  
                if(item.isFormField())  
                {  
                    System.out.println("��������� ...");  
                    processFormField(item);  
                }else{  
                    System.out.println("�����ϴ����ļ� ...");  
                    String filename=processUploadFile(item); 
                    //��ȡ�����ļ�
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
                    //ִ��ǩ������
                    String newApkPath =filePath+"\\"+filename.substring(0,filename.lastIndexOf("."))+"_product.apk";
                    String oldApkPath =filePath+"\\"+filename;
                    String command="jarsigner -verbose -digestalg SHA1 -sigalg MD5withRSA -keystore "+keystore
                    				+" -storepass "+storepass+" -keypass "+keypass+" -signedjar "+"\""+newApkPath+"\""+" "+"\""+oldApkPath+"\""+" "+keyalias;
                    System.out.println(command);
                    Runtime run = Runtime.getRuntime();
                    Process process=run.exec(command);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));  
                    String lineStr;  
                    System.out.println("ִ��������ɣ���ӡ�����Ϣ");
                    while ((lineStr = bufferedReader.readLine()) != null)  
                        //�������ִ�к��ڿ���̨�������Ϣ  
                        System.out.println(lineStr);// ��ӡ�����Ϣ  
                    //��������Ƿ�ִ��ʧ�ܡ�  
                    if (process.waitFor() != 0) {  
                        if (process.exitValue() == 1)//p.exitValue()==0��ʾ����������1������������  
                            System.err.println("����ִ��ʧ��!");  
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
            System.out.println("ʹ�� fileupload ��ʱ�����쳣 ...");  
            e.printStackTrace();  
        }// end try ... catch ...  
    }// end doPost()  
	 // ���������  
    private void processFormField(FileItem item)//, PrintWriter pw)  
        throws Exception  
    {  
        String name = item.getFieldName();  
        String value = item.getString();          
//        pw.println(name + " : " + value + "\r\n");  
    }  
    //�����ϴ����ļ�  
    private String processUploadFile(FileItem item)//, OutputStream pw)  
    		throws Exception {  
	    // ��ʱ���ļ���������������·������ע��ӹ�һ��  
	    String filename = item.getName();         
	    System.out.println("�������ļ�����" + filename);  
	    int index = filename.lastIndexOf("\\");  
	    filename = filename.substring(index + 1, filename.length());  
	
	    long fileSize = item.getSize();  
	
	    if("".equals(filename) && fileSize == 0)  
	    {             
	        System.out.println("�ļ���Ϊ�� ...");  
	        return "";  
	    }  
	
	    File uploadFile = new File(filePath + "/" + filename);  
	    item.write(uploadFile);
//	    pw.println(filename + " �ļ�������� ...");  
//	    pw.println("�ļ���СΪ ��" + fileSize + "\r\n");  
	    return filename;
	    
    }  

}
