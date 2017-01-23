<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<html>
<head>
    <title>Linkss</title>
</head>
<body>
<B>
    Link ${fullLink.getKey()}
</B>
<br><br>

<form:form action="/actions/link" method="post" modelAttribute="fullLink">
    <input type="hidden" name="oldFullLink" value=${fullLink}>
    <input type="hidden" name="owner" value=${owner}>
    <table border="1">
        <tr>
            <td>
                Key
            </td>
            <td>
                <form:input path="key" id="key" value="${key}"/></td>
            </td>
        </tr>
        <tr>
            <td>Shortlink</td>
            <td>
                <form:input path="shortLink" id="shortLink" value="${shortLink}"/></td>
        </tr>
        <tr>
            <td>Link</td>
            <td>
                <form:input path="link" id="link" value="${link}"/></td>
        </tr>
        <tr>
            <td>Days</td>
            <td>
                <form:input path="days" id="days" value="${days}"/></td>
        </tr>
        <tr>
            <td>User</td>
            <td>
                <form:input path="userName" id="userName" value="${userName}"/></td>
        </tr>
        <tr>
            <td>Image</td>
            <td>
                <img src="${imageLink}"/></td>
        </tr>
    </table>
    <br>
    <button
            onclick="location.href='/actions/links?owner=${owner}'">
        Links
    </button>
    <input type="submit" value="Update link"/>
</form:form>
</body>
</html>