<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<html>
<body>
<div class="container" style="alignment: center">
    <%@include file="header.jsp" %>
    <form:form action="" method="post">
        <input type="hidden" name="user" value=${user}>
        <form class="form-horizontal" >
            <br>
            <div style="alignment: center">
                <div class="row" style="text-align: center">
                    <div class="form-group">
                        <div class="col-xs-12">
                            <button type="submit" class="btn btn-success">Create short
                                link
                            </button>
                            <button class="btn btn-success"
                                    onclick="location.href=''" type="button"
                                    class="btn btn-primary">
                                Clear
                            </button>
                        </div>
                    </div>
                </div>
                <br>
                <div class="row" style="text-align: center">
                    <div class="form-group">
                        <div class="col-xs-12" style="text-align: center">
                            <textarea style="text-align: center" path="link" class="form-control"
                                      id="link"
                                      name="link"
                                      placeholder="Text/Link" value="${link}">${link}
                            </textarea>
                        </div>

                    </div>
                </div>
                <br>
                <div class="row" style="text-align: center">
                    <div class="form-group">
                        <div class="col-xs-12">
                            <h2>
                                <a href="${shortLink}" id="shortLink"><img src="./${image}"/>
                                ${shortLink} </a></h2>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </form:form>
</div>
</body>
</html>
