<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<html>
<body>
<div class="container" style="alignment: center">
    <%@include file="header.jsp" %>
    <h2> IMAGE </h2>
    <div class="row">
        <div class="form-group">
            <label for="shortLink" class="control-label col-xs-1"></label>
            <div class="col-xs-6">
                <h2>
                    <a href="${shortLink}" id="shortLink"><img src="${image}"/></a></h2>
            </div>
        </div>
    </div>
</div>
</body>
</html>
