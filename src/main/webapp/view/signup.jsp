<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<html>
<body>
<div class="container" style="alignment: center">
    <%@include file="header.jsp" %>
    <h2> SIGN UP </h2>
    <form:form action="/actions/register" method="post" modelAttribute="user">
        <table class="table">
            <tr>
                <td>User name</td>
                <td><form:input path="userName" id="userName" value="${userName}"/></td>
                <td>User name mustn't contain underscore '_' symbol</td>
            </tr>
            <tr>
                <td>E-mail</td>
                <td><form:input path="email" id="email" value="${email}" pattern="*@*.*"/></td>
            </tr>
            <tr>
                <td>Password</td>
                <td><form:input type="password" path="password" id="password"
                                value="${password}"/></td>
            </tr>
            <tr>
                <td><input type="submit" value="Register" name="register" id="register"/></td>
                <td><input type="submit" value="Register and login" name="registerAndLogin"
                           id="registerAndLogin"/></td>

            </tr>
        </table>
    </form:form>
</div>
</body>
</html>