<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<form name="uploadForm" method="POST" 
        enctype="MULTIPART/FORM-DATA" 
        action="upload"> 
 		User Name:<input type="text" name="username" size="30"/> 
        Upload File1:<input type="file" name="file1" size="30"/> 
        <input type="submit" name="submit" value="上传"> 
        <input type="reset" name="reset" value="重置"> 
      </form> 
</body>
</html>