<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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
<section>

    <h3>Statistics</h3>
    <table border="1" width="50%" class="table">
        <tr>
            <td width="20%"><b>Free links</b></td> <td>${freeLinksSize}</td>
        </tr>
        <tr>
            <td width="20%"><b>Links</b></td> <td>${linksSize}</td>
        </tr>
        <tr>
            <td width="20%"><b>Users</b></td> <td>${usersSize}</td>
        </tr>
        <tr>
            <td width="20%"><b>Domains</b></td> <td>${domainsSize}</td>
        </tr>

    </table>

    <h3>Manage</h3>
    <table border="1" width="50%" class="table">
        <tr>
            <td width="10%"><b><a href="/actions/users">Users</a></b></td>
        </tr>
        <tr>
            <td width="10%"><b><a href="/actions/domains">Domains</a></b></td>
        </tr>
        <tr>
            <td width="10%"><b><a href="/actions/freelinks">Free links</a></b></td>
        </tr>
      </table>

    <h3>Administrative Tools</h3>

    <table border="1" width="50%" class="table">
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
