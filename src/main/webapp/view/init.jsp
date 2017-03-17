<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="s" uri="http://www.springframework.org/tags" %>

<html>
<head>
    <title>Linkss</title>
    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
</head>
<body>
<h3> Init Redis db?</h3>
<form action="./init" method="POST">
    <div class="g-recaptcha" data-sitekey="6LfYMRkUAAAAAIAYkmMRsZtqzv2D_Icz5PfvKNk7"></div>
    <br/>
    <input type="submit" value="Init">
</form>
</body>
</html>


