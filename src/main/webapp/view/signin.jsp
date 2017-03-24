<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<html>

<body>
<script src="https://www.google.com/recaptcha/api.js" async defer></script>
<div class="container" style="alignment: center">
    <%@include file="header.jsp" %>
    <h2> SIGN IN </h2>
    <form:form action="./login" method="post" modelAttribute="user">
        <table class="table">
            <tr>
                <td>User name</td>
                <td><form:input path="userName" id="userName" value="${userName}"/></td>
            </tr>
            <tr>
                <td>Password</td>
                <td><form:input type="password" path="password" id="password"
                                value="${password}"/></td>
            </tr>
            <tr>
                <td></td>
                <td><input type="submit" value="Login" id="login"/>
                <input type="button" value="Remind" id="remind"
                           onclick="location.href='./remind'"/></td>
            </tr>
        </table>
        <div class="g-recaptcha" data-sitekey="6LfYMRkUAAAAAIAYkmMRsZtqzv2D_Icz5PfvKNk7"></div>
    </form:form>

<%--<h3> Defaults</h3>--%>
    <%--<b>User - admin, password - admin</b><br>--%>
    <%--<b>User - user, password - user </b>--%>
</div>
</body>
</html>