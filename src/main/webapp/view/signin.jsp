<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<html>

<%@include file="header.jsp" %>
<body>
<form:form action="/actions/login" method="post" modelAttribute="user">
    <table class="table">
        <tr>
            <td>User name</td>
            <td><form:input path="userName" id="userName" value="${userName}"/></td>
        </tr>
        <tr>
            <td>Password</td>
            <td><form:input type="password" path="password" id="password" value="${password}"/></td>
        </tr>
        <tr>
            <td></td>
            <td><input type="submit" value="Login" id="login"/></td>
        </tr>
    </table>
</form:form>
</body>
</html>