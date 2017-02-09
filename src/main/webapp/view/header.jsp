<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<head>
    <br>
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
    <title>Linkss</title>
    <link href="/resources/images/favicon.ico" rel="shortcut icon" type="image/x-icon" >

    <button onclick="location.href='/'" class="btn btn-success">Home</button>
    <button onclick="location.href='/actions/links'" class="btn btn-success">Links</button>
    <c:choose>
        <c:when test="${autorizedUser!=null && autorizedUser.isAdmin()}" >
            <button onclick="location.href='/actions/manage'" class="btn btn-success">Manage</button>
        </c:when>
    </c:choose>
    <button onclick="location.href='/actions/signup'" class="btn btn-success">Sign up</button>
    <c:choose>
        <c:when test="${autorizedUser==null || autorizedUser.isEmpty()}" >
            <button onclick="location.href='/actions/signin'" class="btn btn-success">Sign in</button>
        </c:when>
        <c:otherwise>
            <button onclick="location.href='/actions/logout'" class="btn btn-success">Logout</button>
            <b>Login as ${autorizedUser.getUserName()}</b>
        </c:otherwise>
    </c:choose>
</head>



