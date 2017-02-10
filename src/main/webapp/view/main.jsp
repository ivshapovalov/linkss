<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<%@include file="header.jsp" %>
<body>
<section>
    <form method="post">
        <input type="hidden" name="user" value=${user}>
        <br>
        <table border="2" class="table">
            <tr>
                <td colspan="2">
                    <button type="submit" name="create" class="btn btn-primary">Create short
                        link
                    </button>
                    <button
                            onclick="location.href=''" type="button" class="btn btn-primary">
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
                <td width="10%"><label path="short">Short:</label></td>
                <td width="90%"><a href="${shortLink}">${shortLink}</a>
                </td>
            </tr>
            <tr>
                <td width="10%" align="left">
                    <label path="qrcode">QR</label>
                </td>
                <td width="90%" align="left">
                    <img src="${image}" alt="">
                </td>
            </tr>
        </table>
    </form>
</section>
</body>
</html>
