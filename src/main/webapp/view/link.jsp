<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<html>
<%@include file="header.jsp" %>
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

<body>
<br>
<H3>
    Short link '${key}'
</H3>
<form:form action="/actions/link" method="post" modelAttribute="fullLink">
    <input type="hidden" name="oldKey" value=${oldKey}>
    <input type="hidden" name="owner" value=${owner}>
    <table border="1" class="table">
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