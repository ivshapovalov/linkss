<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>

<%@include file="header.jsp" %>
<body>


<section>
    <form method="post">
        <input type="hidden" name="user" value=${user}>
        <br>
        <table border="1" width="30%">
            <tr>
                <td colspan="2" width="100%" align="center">
                    <button type="submit" name="create">Create short link</button>
                    <button
                            onclick="location.href=''" type="button">
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
