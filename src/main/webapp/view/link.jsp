<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<html>
<head>
    <title>Linkss</title>
</head>
<body>
<%@include file="header.jsp" %>

<form:form action="/actions/link" method="post" modelAttribute="fullLink">
    <input type="hidden" name="oldKey" value=${oldKey}>
    <input type="hidden" name="owner" value=${owner}>
    <table border="1">
        <tr>
            <td><form:label path="key">Key</form:label></td>
            <td><form:input path="key" id="key" value="${key}"/></td>
        </tr>
        <tr>
            <td><form:label path="shortLink">Short link</form:label></td>
            <td><form:input path="shortLink" disabled="true"/></td>
        </tr>
        <tr>
            <td><form:label path="link">Link</form:label></td>
            <td><form:input path="link" id="link" value="${link}"/></td>
        </tr>
        <tr>
            <td><form:label path="days">Days</form:label></td>
            <td><input type="number" name="days" id="days" value="${fullLink.getDays()}"></td>

        <%--<td><form:input path="days" id="days" value="${days}"/></td>--%>
        </tr>
        <tr>
            <td><form:label path="userName">User</form:label></td>
            <td><form:input path="userName" id="userName" value="${userName}"/></td>
        </tr>
        <tr>
            <td><form:label path="imageLink">Image</form:label></td>
            <td><img src="${imageLink}"/></td>
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