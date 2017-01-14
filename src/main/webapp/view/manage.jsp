<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Statistics</title>
</head>
<body>
<%@include file="header.jsp" %>
<section>

    <h3>Users</h3>
    <table border="1" width="50%">
        <tr>
            <td width="40%"><b>Link</b></td>
            <td width="10%"><b>Count</b></td>
            <td width="20%"><b>Short Link</b></td>
            <td width="30%"><b>Image</b></td>
        </tr>
        <c:forEach items="${fullStat}" var="column">
            <tr>
                <td width="40%"><a href="${column.getLink()}">${column.getLink()}</a></td>
                <td width="10%">${column.getCount()}</td>
                <td width="20%"><a href="${column.getShortLink()}">${column.getShortLink()}</a></td>
                <td width="30%"><img src="${column.getImageLink()}"></td>

            </tr>
        </c:forEach>
    </table>
</section>
</body>
</html>
