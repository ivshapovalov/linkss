<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<html>
<body>
<div class="container" style="alignment: center">
    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
    <%@include file="header.jsp" %>
    <h2> SIGN UP </h2>
    <form:form action="./register" method="post" modelAttribute="user">
        <table class="table">
            <tr>
                <td>User name</td>
                <td><form:input path="userName" id="userName" value="${userName}" required="true"
                                onkeyup="var regex=/[a-zA-Z,'0-9']/;
                    if(!regex.test(this.value)) this.value=''"/></td>
            </tr>
            <tr>
                <td>E-mail</td>
                <td><form:input path="email" id="email" value="${email}" pattern="*@*.*" required="true"/></td>
            </tr>
            <tr>
                <td>Password</td>
                <td><form:input type="password" path="password" id="password"
                                value="${password}" required="true" /></td>
            </tr>
            <tr>
                <td><input type="submit" value="Register" name="register" id="register"/></td>
            </tr>
        </table>
        <div class="g-recaptcha" data-sitekey="6LfYMRkUAAAAAIAYkmMRsZtqzv2D_Icz5PfvKNk7"></div>
    </form:form>
</div>
</body>
</html>