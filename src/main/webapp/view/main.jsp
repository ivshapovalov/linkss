<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>

<%@include file="header.jsp" %>
<body>
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

<section>
    <form method="post">
        <input type="hidden" name="user" value=${user}>
        <br>
        <table border="1" width="30%" class="table">
            <tr>
                <td colspan="2" width="100%" align="center">
                    <button type="submit" name="create" class="btn btn-success">Create short link</button>
                    <button
                            onclick="location.href=''" type="button" class="btn btn-success">
                        Clear
                    </button>
                </td>
            </tr>
            <tr>
                <td width="10%"><label path="link">Text</label></td>
                <td width="90%"><input size="90%" type="text" id="${link}"
                                       name="link"
                                       value="${link}"></td>
            </tr>
            <tr>
                <td width="10%">
                    Short:
                </td>
                <td width="90%"><a href="${shortLink}">${shortLink}</a>
                </td>
            </tr>
            <tr>
                <td colspan="2" width="100%" align="center">
                    QR Code
                </td>
            </tr>
            <tr>
                <td colspan="2" width="100%" align="center">
                    <img src="${image}" alt="">
                </td>
            </tr>
        </table>
    </form>
</section>
</body>
</html>
