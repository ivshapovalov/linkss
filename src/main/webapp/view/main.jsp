<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<html>
<%@include file="header.jsp" %>
<body>
<section>
    <form:form action="" method="post">
        <input type="hidden" name="user" value=${user}>
        <form class="form-horizontal">
            <br>
            <div class="row">
                <div class="form-group">
                    <label class="control-label col-xs-1"></label>
                    <div class="col-xs-4">
                        <button type="submit" class="btn btn-success">Create short
                            link
                        </button>
                        <button class="btn btn-success"
                                onclick="location.href=''" type="button" class="btn btn-primary">
                            Clear
                        </button>
                    </div>
                </div>
            </div>
            <br>
            <div class="row">
                <div class="form-group">
                    <label for="link" class="control-label col-xs-1">Text</label>
                    <div class="col-xs-4">
                            <textarea path="link" class="form-control" id="link"
                                      name="link"
                                      placeholder="text" value="${link}">${link} </textarea>
                    </div>

                </div>
            </div>
            <br>
            <div class="row">
                <div class="form-group">
                    <label for="shortLink" class="control-label col-xs-1">Short link</label>
                    <div class="col-xs-4">
                        <a href="${shortLink}" id="shortLink">${shortLink} </a>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group">
                    <label for="shortLink" class="control-label col-xs-1"></label>
                    <div class="col-xs-4">
                        <img src="${image}"/>
                    </div>
                </div>
            </div>

        </form>
    </form:form>
</section>
</body>
</html>
