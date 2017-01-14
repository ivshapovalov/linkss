<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Main</title>
</head>
<body>
<%@include file="header.jsp" %>
<button onclick="location.href='/actions/statistics'">Statistics</button>
<c:choose>
    <c:when test="${user!=null && user.isAdmin()}" >
        <button onclick="location.href='/actions/manage'">Manage</button>
    </c:when>
</c:choose>
<button onclick="location.href='/actions/registration'">Registration</button>
<c:choose>
    <c:when test="${user==null || user.isEmpty()}" >
        <button onclick="location.href='/actions/signin'">Sign in</button>
    </c:when>
    <c:otherwise>
        <button onclick="location.href='/actions/logout'">Logout</button>
        <b>Login as ${user.getUserName()};</b>
    </c:otherwise>
</c:choose>

<section>
    <form method="post">
        <input type="hidden" name="user" value=${user}>
        <br>

        <table border="1" width="30%">
            <tr>
                <td colspan="2" width="100%" align="center">
                    <button type="submit" name="create">Create short link</button>
                    <button
                            onclick="location.href=''" type="button">
                        Clear
                    </button>
                </td>
            </tr>
            <tr>
                <td width="10%">Link:</td>
                <td width="90%"><input size="90%" type="text" id="${link}"
                                       name="link"
                                       value="${link}"></td>
            </tr>
            <tr>
                <td width="10%">
                    Short:
                </td>
                <td width="90%"><a href="${shortLink}">${shortLink}</a>
                </td>
            </tr>
            <tr>
                <td colspan="2" width="100%" align="center">
                    QR Code
                </td>
            </tr>
            <tr>
                <td colspan="2" width="100%" align="center">
                    <img src="${filename}" alt="">
                </td>
            </tr>
        </table>
    </form>
</section>
</body>
</html>