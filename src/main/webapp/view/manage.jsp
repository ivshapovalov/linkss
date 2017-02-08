<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<%@include file="header.jsp" %>

<body>
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
            <td
                    width="10%"><b><a href="/actions/checkExpired">Check and delete expired
                links</a></b></td>
        </tr>
    </table>
</section>
</body>
</html>
