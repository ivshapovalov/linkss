<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Statistics</title>
</head>
<body>
<section>
    <h2><a href="">Home</a></h2>
    <h2><a href="/stat">Statistics</a></h2>
    <form method="post">
        <table border="1" width="30%">
            <tr>
                <td colspan="2" width="100%" align="center">
                    <button type="submit">Create short link</button>
                    <button
                            onclick="location.href=''" type="button">
                        Clear
                    </button>
                </td>
            </tr>
            <tr>
                <td width="10%">Link:</td>
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
                    <img src="${filename}.png" alt="">
                </td>
            </tr>
        </table>


    </form>
</section>
</body>
</html>
