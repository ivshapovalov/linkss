<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Statistics</title>
</head>
<body>
<section>
    <h2><a href="/">Home</a></h2>

    <h3>Domain Statistics</h3>

    <table border="1" width="50%">
        <c:forEach items="${shortStat}" var="column">
            <tr>
                <c:forEach items="${column}" var="row">
                    <td width="50%">
                            ${row}
                    </td>
                </c:forEach>
            </tr>
        </c:forEach>
    </table>

    <h3>Links Statistics</h3>

    <table border="1" width="50%">
        <c:forEach items="${fullStat}" var="column">
            <tr>
                <c:forEach items="${column}" var="row">
                    <td width="50%">
                            ${row}
                    </td>
                </c:forEach>
            </tr>
        </c:forEach>
    </table>
</section>
</body>
</html>
