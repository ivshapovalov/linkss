<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<html>
<%@include file="header.jsp" %>


<body>
<br>
<H3>
    Short link '${key}'
</H3>
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
            <td><form:label path="link">Text</form:label></td>
            <td><form:input path="link" id="link" value="${link}"/></td>
        </tr>
        <tr>
            <td><label path="seconds">Expire at</label></td>
            <td><input type="text" name="secondsText" id="secondsText"
                       <%--pattern="[0-9]{3}:[0-9]{2}:[0-9]{2}:[0-9]{2}"--%>
                       value="${fullLink.getSecondsAsPeriod()}"></td>
        </tr>
        <tr>
            <td><form:label path="userName">User</form:label></td>
            <td><form:input path="userName" id="userName" value="${userName}"/></td>
        </tr>
        <tr>
            <td><form:label path="imageLink">Image</form:label></td>
            <td><img src="${fullLink.imageLink}"/></td>
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