<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<%@include file="header.jsp" %>

<body>
<B>
    User '${user.getUserName()}'
</B>
<br><br>
<form action="/actions/user" method="post">
    <input type="hidden" name="oldUserName" value="${oldUserName}">
    <input type="hidden" name="oldPassword" value="${oldPassword}">
    <table border="1">
        <tr>
            <td><label path="userName">User</label></td>
            <td>
                <input type="text" name="userName" value=${user.userName}></td>
        </tr>
        <tr>
            <td><label path="password">Password</label></td>
            <td>
                <input type="password" name="password" value=${user.password}></td>
        </tr>
    </table>
    <br>
    <input type="submit" value="Update"/>

</form>
</body>
</html>