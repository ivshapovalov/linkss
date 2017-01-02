<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Main</title>
</head>
<body>
<section>
    <h2><a href="index.html">Home</a></h2>
    <form method="post">
        <dl>
            <dt>Link:</dt>
            <dd><input type="text" id="${link}" name="link">${shortLink}</dd>
        </dl>
        <button type="submit">Create</button>
        <button onclick="window.history.back()">Cancel</button>
    </form>
</section>
</body>
</html>
