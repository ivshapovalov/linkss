<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<head>
    <title>Linkss</title>
    <link href="/favicon.ico" rel="shortcut icon" type="image/x-icon" >

    <button onclick="location.href='/'">Home</button>
    <button onclick="location.href='/actions/links'">Links</button>
    <c:choose>
        <c:when test="${autorizedUser!=null && autorizedUser.isAdmin()}" >
            <button onclick="location.href='/actions/manage'">Manage</button>
        </c:when>
    </c:choose>
    <button onclick="location.href='/actions/signup'">Sign up</button>
    <c:choose>
        <c:when test="${autorizedUser==null || autorizedUser.isEmpty()}" >
            <button onclick="location.href='/actions/signin'">Sign in</button>
        </c:when>
        <c:otherwise>
            <button onclick="location.href='/actions/logout'">Logout</button>
            <b>Login as ${autorizedUser.getUserName()}</b>
        </c:otherwise>
    </c:choose>
</head>



