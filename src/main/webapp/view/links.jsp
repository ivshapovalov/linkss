<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Links</title>
</head>
<body>
<%@include file="header.jsp" %>
<section>
    <h3>Links</h3>
    <table border="1" width="50%">
        <tr>
            <td width="40%"><b>Link</b></td>
            <td width="10%"><b>Days</b></td>
            <td width="10%"><b>Count</b></td>
            <td width="20%"><b>Short Link</b></td>
            <td width="10%"><b>Delete</b></td>
            <td width="10%"><b>Update</b></td>
        </tr>
        <c:forEach items="${fullStat}" var="column">
            <tr>
                <td width="40%"><a href="${column.getLink()}">${column.getLink()}</a></td>
                <td width="10%"><input type="text" name="days" value=${column.getDays()}></td>
                <td width="10%">${column.getCount()}</td>
                <td width="20%"><a href="${column.getShortLink()}">${column.getShortLink()}</a></td>
                <td width="10%"><a href="/actions/deleteuserlink?key=${column.getKey()}&owner=${column.getUserName()}">Delete
                <td width="10%"><a href="/actions/updateuserlink?key=${column.getKey()}&owner=${column.getUserName()}&days=${days}">Update
                </a></td>
            </tr>
        </c:forEach>
    </table>
</section>
</body>
</html>
