<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Linkss</title>
</head>
<body>
<%@include file="header.jsp" %>
<section>

    <h3>Users</h3>
    <table border="1" width="50%">
        <tr>
            <td width="40%"><b>User name</b></td>
            <td width="40%"><b>Password</b></td>
            <td width="10%"><b>Links</b></td>
            <td width="10%"><b></b></td>
            <td width="10%"><b></b></td>
        </tr>
        <c:forEach items="${users}" var="column">
            <tr>
                <td width="40%">${column.getUserName()}</td>
                <td width="40%" >*</td>
                <td width="10%">
                    <a href="/actions/links?owner=${column.getUserName()}">${column.getLinkNumber()}
                </a></td>
                <td width="10%">
                    <a href="/actions/users?action=edit&key=${column.getUserName()}">Edit</a>
                </td>
                <td width="10%">
                    <a href="/actions/users?action=delete&key=${column.getUserName()}">Delete
                </a></td>

            </tr>
        </c:forEach>
    </table>
</section>
</body>
</html>
