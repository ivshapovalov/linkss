<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<meta name="viewport" content="width=device-width, initial-scale=1">

<%@include file="header.jsp" %>
<body>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
<style type="text/css">
    .bs-example{
        margin: 20px;
    }
</style>
<script>
    $(document).ready(function () {
        $('.dropdown-toggle').dropdown();
    });
</script>
<section>

    <h3>Links</h3>

    <div class="bs-example">
        <ul class="pagination">
            <li>
                <a href="/actions/links?page=${1}&owner=${owner}">
                    ${1}
                </a>
            </li>
            <c:choose>
                <c:when test="${currentPage lt 7}">
                    <li class="disabled"><a
                            href="/actions/links?page=${currentPage - 6}&owner=${owner}">
                        << </a></li>
                </c:when>
                <c:otherwise>
                    <li>
                        <a href="/actions/links?page=${currentPage - 6}&owner=${owner}">
                            << </a>
                    </li>
                </c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${currentPage lt 2}">
                    <li class="disabled"><a
                            href="/actions/links?page=${currentPage - 1}&owner=${owner}">
                        < </a></li>
                </c:when>
                <c:otherwise>
                    <li>

                        <a href="/actions/links?page=${currentPage - 1}&owner=${owner}">
                            < </a>
                    </li>
                </c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${currentPage-3 lt 1}">
                    <c:set var="min" value="1"></c:set>
                </c:when>
                <c:otherwise>
                    <c:set var="min" value="${currentPage-3}"></c:set>
                </c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${currentPage+6 gt numberOfPages}">
                    <c:choose>
                        <c:when test="${currentPage-6 lt 0}">
                            <c:set var="min" value="1"></c:set>
                        </c:when>
                        <c:otherwise>
                            <c:set var="min" value="${numberOfPages-6}"></c:set>
                        </c:otherwise>
                    </c:choose>
                    <c:set var="max" value="${numberOfPages}"></c:set>
                </c:when>
                <c:otherwise>
                    <c:set var="max" value="${min+6}"></c:set>
                </c:otherwise>
            </c:choose>
            <c:forEach begin="${min}" end="${max}" var="i">
                <c:choose>
                    <c:when test="${currentPage eq i}">
                        <li class="active">
                            <a href="/actions/links?page=${currentPage}&owner=${owner}">
                                    ${i}
                            </a>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li>
                            <a href="/actions/links?page=${i}&owner=${owner}">
                                    ${i}
                            </a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
            <c:choose>
                <c:when test="${currentPage gt numberOfPages-1}">
                    <li class="disabled"><a
                            href="/actions/links?page=${currentPage + 1}&owner=${owner}">
                        > </a></li>
                </c:when>
                <c:otherwise>
                    <li><a href="/actions/links?page=${currentPage + 1}&owner=${owner}">
                        > </a>
                    </li>
                </c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${currentPage gt numberOfPages-6}">
                    <li class="disabled"><a
                            href="/actions/links?page=${currentPage + 6}&owner=${owner}">
                        >> </a></li>
                </c:when>
                <c:otherwise>
                    <li><a href="/actions/links?page=${currentPage + 6}&owner=${owner}">
                        >> </a>
                    </li>
                </c:otherwise>
            </c:choose>
            <li><a href="/actions/links?page=${numberOfPages}&owner=${owner}">
                ${numberOfPages} </a>
            </li>

        </ul>
    </div>

    <table border="1" width="50%" class="table">
        <tr>
            <td width="10%"><b>User</b></td>
            <td width="10%"><b>Key</b></td>
            <td width="40%"><b>Text</b></td>
            <td width="10%"><b>Expire at</b></td>
            <td width="10%"><b>Visits</b></td>
            <td width="20%"><b>Short Link</b></td>
            <td width="20%"><b>Action</b></td>
        </tr>
        <c:forEach items="${list}" var="column">
            <tr>
                <td width="10%">${column.userName}</td>
                <td width="10%">${column.key}</td>
                <td width="40%">${column.link}</td>
                <td width="10%">${column.getSecondsAsPeriod()}</td>
                <td width="10%">${column.visits}</td>
                <td width="20%"><a href="${column.shortLink}">${column.shortLink}</a>
                </td>
                <td>
                    <div class="btn-group">
                        <button type="button" data-toggle="dropdown"
                                class="btn btn-success dropdown-toggle" >Action
                             <span class="caret"></span></button>
                        <ul class="dropdown-menu">
                            <li><a
                                    href="/actions/user/${column.userName}/links/delete/?key=${column.key}"><span
                                    class="glyphicon glyphicon-trash"></span>Delete</a></li>
                            <li><a
                                    href="/actions/user/${column.userName}/links/edit/?key=${column.key}"><span
                                    class="glyphicon glyphicon-pencil"></span>Edit</a></li>
                        </ul>
                    </div>
                </td>
            </tr>
        </c:forEach>
    </table>

</section>
</body>
</html>
