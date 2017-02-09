<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<%@include file="header.jsp" %>

<body>
<script type="text/javascript" src="/resources/js/makeAdmin.js" defer></script>

<br>
<H3>
    User '${user.getUserName()}'
</H3>

<form action="/actions/user" method="post">
    <input type="hidden" name="oldUserName" value="${oldUserName}">
    <table border="1">
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
                <%--<script type="text/javascript">--%>
                <%--$(function (){--%>
                <%--$("input:checkbox").prop('checked',true);--%>

                <%--if($('#admin').val()== "true"){--%>
                <%--$("input:checkbox").prop('checked',true);--%>
                <%--}else{--%>
                <%--$("input:checkbox").prop('checked', true);--%>
                <%--}--%>
                <%--});--%>
                <%--</script>--%>
            </td>

        </tr>
    </table>
    <br>
    <input type="submit" value="Update"/>

</form>
</body>
</html>

