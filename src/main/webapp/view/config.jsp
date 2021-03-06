<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<body>
<body>
<div class="container" style="alignment: center">
    <%@include file="header.jsp" %>
    <h2> CONFIG </h2>
    <section>

        <h3>Statistics</h3>
        <table border="1" width="50%" class="table">
            <tr>
                <td width="20%"><b>Free links</b></td>
                <td>${freeLinksSize}</td>
            </tr>
            <tr>
                <td width="20%"><b>Links</b></td>
                <td>${linksSize}</td>
            </tr>
            <tr>
                <td width="20%"><b>Users</b></td>
                <td>${usersSize}</td>
            </tr>
            <tr>
                <td width="20%"><b>Domains</b></td>
                <td>${domainsSize}</td>
            </tr>

        </table>

        <h3>Config</h3>
        <table border="1" width="50%" class="table">
            <tr>
                <td width="10%"><b><a href="./users"
                                      class="glyphicon glyphicon-user">Users</a></b></td>
            </tr>
            <tr>
                <td width="10%"><b><a href="./domains"
                                      class="glyphicon glyphicon-globe">Domains</a></b></td>
            </tr>
            <tr>
                <td width="10%"><b><a href="./freelinks" class="glyphicon glyphicon-book">Free
                    links</a></b></td>
            </tr>
            <tr>
                <td width="10%"><b><a href="./toplinks"
                                      class="glyphicon glyphicon-thumbs-up">Top
                    used links</a></b></td>
            </tr>

        </table>

        <h3>Administrative Tools</h3>

        <table border="1" width="50%" class="table">
            <tr>
                <td width="10%"><b><a href="./populate">Populate</a></b></td>
            </tr>
            <tr>
                <td
                        width="10%"><b><a href="./checkExpired">Check and delete expired
                    links</a></b></td>
            </tr>
        </table>
    </section>
</div>
</body>
</html>
