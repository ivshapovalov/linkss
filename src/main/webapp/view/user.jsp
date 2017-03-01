<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>

<body>
<br>

<div class="container" style="alignment: center">
    <%@include file="header.jsp" %>
    <h2> USER ${oldUserName}</h2>
    <form action="/actions/user" method="post">
        <input type="hidden" name="oldUserName" value="${oldUserName}">
        <form class="form-horizontal">
            <div class="row">
                <div class="form-group">
                    <label class="control-label col-xs-2"><h4>User '${user.getUserName()}'</h4>
                    </label>
                    <div class="col-xs-2">
                        <button type="submit" class="btn btn-primary"><h4>Update user</h4></button>
                        <button type="button" class="btn btn-danger"
                                onclick="location.href='/actions/users/${user.userName}/delete'">
                            <h4>
                                Delete</h4></button>
                    </div>
                </div>
            </div>
            <br>
            <div class="row">
                <div class="form-group">
                    <label for="userName" class="control-label col-xs-2">Name</label>
                    <div class="col-xs-2">
                        <input type="userName" class="form-control" id="userName" name="userName"
                               placeholder="userName" value="${user.userName}">
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group">
                    <label for="email" class="control-label col-xs-2">E-mail</label>
                    <div class="col-xs-2">
                        <input type="email" class="form-control" id="email" name="email"
                               placeholder="email" value="${user.email}">
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group">
                    <label for="password" class="control-label col-xs-2">Password</label>
                    <div class="col-xs-2">
                        <input type="password" class="form-control" id="password" name="password"
                               placeholder="Password" value="${user.password}">
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group">
                    <label class="control-label col-xs-2">Admin</label>
                    <div class="col-xs-2">
                        <div class="isAdmin">
                            <label> <input type="checkbox" onClick="makeAdmin(this)" id="admin"
                                           name="admin"
                                           value="${user.admin}" <c:if
                                    test="${user.admin}"> checked=" checked"</c:if>></label>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </form>
</div>
</body>
</html>

