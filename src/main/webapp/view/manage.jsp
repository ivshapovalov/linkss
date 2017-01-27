<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Linkss</title>
</head>
<body>
<%@include file="header.jsp" %>
<section>
    <h3>Statistics</h3>
    <table border="1" width="50%">
        <tr>
            <td width="10%"><b><a href="/actions/users">Users</a></b></td>
        </tr>
        <tr>
            <td width="10%"><b><a href="/actions/domains">Domains</a></b></td>
        </tr>

    </table>

    <h3>Administrative Tools</h3>

    <table border="1" width="50%">
    <tr>
            <td width="10%"><b><a href="/actions/populate">Populate</a></b></td>
        </tr>
        <tr>
            <td width="10%"><b><a href="/actions/update">Check and delete expired</a></b></td>
        </tr>
    </table>
</section>
</body>
</html>
