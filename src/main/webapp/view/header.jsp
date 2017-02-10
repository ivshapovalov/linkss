<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<head>
    <br>
    <ul class="nav nav-pills">

        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet"
              href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
              integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
              crossorigin="anonymous">
        <!-- Optional theme -->
        <link rel="stylesheet"
              href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css"
              integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp"
              crossorigin="anonymous">
        <!-- Latest compiled and minified JavaScript -->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
                integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
                crossorigin="anonymous"></script>
        <style type="text/css">
            .bs-example {
                margin: 20px;
            }
            .li.my_active {
                color: #ffffff;
                background-color: #5CB85C;
                background-image: linear-gradient(to bottom, #087508, #419641);
                border-color: #3E8F3E #3E8F3E #3E8F3E;
            }
            .li.my_active:hover {
                color: #ffffff;
                background-color: #419641;
                background-image: linear-gradient(to bottom, #419641, #419641);
                border-color: #3E8F3E #3E8F3E #3E8F3E;
            }
        </style>

        <title>Linkss</title>


        <link href="/resources/images/favicon.ico" rel="shortcut icon" type="image/x-icon">

        <li class="active"><a href="/">Home</a></li>
        <li class="active"><a href="/actions/links">Links</a></li>
        <c:choose>
            <c:when test="${autorizedUser!=null && autorizedUser.isAdmin()}">
                <li class="active"><a href="/actions/manage">Manage</a>
                </li>
            </c:when>
        </c:choose>
        <li class="active"><a href="/actions/signup">Sign up</a></li>
        <c:choose>
            <c:when test="${autorizedUser==null || autorizedUser.isEmpty()}">
                <li class="active"><a href="/actions/signin"
                >Sign in</a>
                </li>
            </c:when>
            <c:otherwise>
                <li class="active"><a href="/actions/logout">Logout<span
                        class="badge">${autorizedUser.getUserName()}</span></a>
                </li>
            </c:otherwise>
        </c:choose>
    </ul>

</head>



