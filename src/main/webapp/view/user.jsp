<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>SQLCmd</title>
</head>
<body>
<%@include file="header.jsp" %>
<B>
    User ${user.getUserName()}
</B>
<br><br>
<form action="/actions/user" method="post">
    <input type="hidden" name="oldUserName" value="${oldUserName}">
    <input type="hidden" name="oldPassword" value="${oldPassword}">
    <table border="1">
        <tr>
            <td>
                Name
            </td>
            <td>
                <input type="text" name="userName" value=${user.getUserName()}></td>
            </td>
        </tr>
        <tr>
            <td>Password</td>
            <td>
                <input type="password" name="password" value=${user.getPassword()}></td>
        </tr>
    </table>
    <br>
    <input type="submit" value="Update"/>

</form>
</body>
</html>