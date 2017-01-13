<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Linkss</title>
</head>
<body>
<form:form action="register" method="post" >
    <table>
        <tr>
            <td>User name</td>
            <td><form:input path="userName" id="username" value="${userName}"/></td>
        </tr>
        <tr>
            <td>Password</td>
            <td><form:input path="password" id="password" value="${password}"/></td>
        </tr>
        <tr>
            <td></td>
            <td><input type="submit" value="register" id="register"/></td>
        </tr>
    </table>
</form:form>
</body>
</html>