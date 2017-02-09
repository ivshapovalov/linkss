<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<%@include file="header.jsp" %>

<body>
<script type="text/javascript" src="/resources/js/makeAdmin.js" defer></script>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
      integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
      crossorigin="anonymous">
<link rel="stylesheet"
      href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css"
      integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp"
      crossorigin="anonymous">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
        integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
        crossorigin="anonymous"></script>
<style type="text/css">
    .bs-example {
        margin: 20px;
    }
</style>
<br>
<H3>
    User '${user.getUserName()}'
</H3>

<form action="/actions/user" method="post">
    <input type="hidden" name="oldUserName" value="${oldUserName}">
    <table border="1" class="table">
        <tr>
            <td><label path="userName">User</label></td>
            <td>
                <input type="text" name="userName" value=${user.userName}></td>
        </tr>
        <tr>
            <td><label path="email">E-mail</label></td>
            <td>
                <input type="text" name="email" value=${user.email}></td>
        </tr>
        <tr>
            <td><label path="password">Password</label></td>
            <td>
                <input type="password" name="password" value=${user.password}></td>
        </tr>
        <tr>

            <td><label path="isAdmin">Admin</label></td>
            <td>
                <input type="checkbox" onClick="makeAdmin(this)" id="admin" name="admin"
                       value="${user.admin}" <c:if
                        test="${user.admin}"> checked=" checked"</c:if>>
            </td>

        </tr>
    </table>
    <br>
    <input type="submit" value="Update"/>

</form>
</body>
</html>

