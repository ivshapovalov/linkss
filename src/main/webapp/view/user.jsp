<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>

<body>
<br>

<script type="text/javascript">
    function makeChecked(checkBox) {
        if (checkBox.checked == true) {
            checkBox.value = true;
        } else {
            checkBox.value = false;
        }
    }
</script>
<div class="container" style="alignment: center">
    <%@include file="header.jsp" %>
    <h2> USER ${oldUserName}</h2>
    <form action="./save" method="post">
        <input type="hidden" name="oldUserName" value="${oldUserName}">
        <form class="form-horizontal">
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
            <c:choose>
                <c:when test="${autorizedUser.admin}">
                    <div class="row">
                        <div class="form-group">
                            <label class="control-label col-xs-2">Admin</label>
                            <div class="col-xs-2">
                                <div class="admin">
                                    <label> <input type="checkbox" onClick="makeChecked(this)"
                                                   id="admin"
                                                   name="admin"
                                                   value="${user.admin}" <c:if
                                            test="${user.admin}"> checked="checked"</c:if>></label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group">
                            <label class="control-label col-xs-2">Verified</label>
                            <div class="col-xs-2">
                                <div class="verified">
                                    <label> <input type="checkbox" onClick="makeChecked(this)"
                                                   id="verified"
                                                   name="verified"
                                                   value="${user.verified}" <c:if
                                            test="${user.verified}">
                                                   checked="checked"</c:if>></label>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:when>
            </c:choose>
            <div class="row">
                <div class="form-group">
                    <%--<label class="control-label col-xs-2"><h4></h4>--%>
                    <%--</label>--%>
                    <div class="col-xs-1">
                        <button type="submit" class="btn btn-primary"><h4>Update</h4>
                        </button>
                    </div>
                    <div class="col-xs-1">
                        <button type="button" class="btn btn-danger"
                                onclick="location.href='delete'"><h4>Delete</h4></button>
                    </div>
                </div>
            </div>

        </form>
    </form>
</div>
</body>
</html>

