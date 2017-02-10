<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<html>
<%@include file="header.jsp" %>
<body>
<br>

<form:form action="/actions/link" method="post" modelAttribute="fullLink">
    <input type="hidden" name="oldKey" value=${oldKey}>
    <input type="hidden" name="owner" value=${owner}>
    <form class="form-horizontal">
        <div class="row">
            <div class="form-group">
                <label class="control-label col-xs-2"><h4>Short link '${key}'</h4></label>
                <div class="col-xs-2">
                    <button type="submit" class="btn btn-primary"><h4>Update link</h4></button>
                </div>
            </div>
        </div>
        <br>
        <div class="row">
            <div class="form-group">
                <label for="key" class="control-label col-xs-2">Key</label>
                <div class="col-xs-2">
                    <form:input path="key" class="form-control" id="key" name="key"
                           placeholder="key" value="${key}"/>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="form-group">
                <label for="shortLink" class="control-label col-xs-2">Short link</label>
                <div class="col-xs-2">
                    <form:input path="shortLink" class="form-control" id="shortLink" name="shortLink"
                           placeholder="shortLink" value="${shortLink}" readonly="readonly"/>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="form-group">
                <label for="link" class="control-label col-xs-2">Link</label>
                <div class="col-xs-2">
                    <form:input path="link" class="form-control" id="link" name="link"
                           placeholder="link" value="${link}"/>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="form-group">
                <label for="secondsText" class="control-label col-xs-2">Expires </label>
                <div class="col-xs-2">
                    <input type="secondsText" class="form-control" id="secondsText" name="secondsText"
                           placeholder="DDD:HH:mm:ss" value="${fullLink.getSecondsAsPeriod()}">
                </div>
            </div>
        </div>
        <div class="row">
            <div class="form-group">
                <label for="userName" class="control-label col-xs-2">User</label>
                <div class="col-xs-2">
                    <form:input path="userName" class="form-control" id="userName" name="userName"
                           placeholder="userName" value="${userName}"/>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="form-group">
                <label class="control-label col-xs-2">QR</label>
                <div class="col-xs-2">
                    <img src="${fullLink.imageLink}"/>
                </div>
            </div>
        </div>

        <br>
    </form>
</form:form>
</body>
</html>