<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="s" uri="http://www.springframework.org/tags" %>

<html>
<%@include file="header.jsp" %>
<body>
<script>
    $(document).ready(function () {
        $('.dropdown-toggle').dropdown();
    });
</script>
<section>

    <h3>Archive links</h3>

    <div class="bs-example">
        <ul class="pagination">
            <li>
                <a href="/actions/archive?page=${1}&owner=${owner}">
                    ${1}
                </a>
            </li>
            <c:choose>
                <c:when test="${currentPage lt 7}">
                    <li class="disabled"><a
                            href="/actions/archive?page=${currentPage - 6}&owner=${owner}">
                        << </a></li>
                </c:when>
                <c:otherwise>
                    <li>
                        <a href="/actions/archive?page=${currentPage - 6}&owner=${owner}">
                            << </a>
                    </li>
                </c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${currentPage lt 2}">
                    <li class="disabled"><a
                            href="/actions/archive?page=${currentPage - 1}&owner=${owner}">
                        < </a></li>
                </c:when>
                <c:otherwise>
                    <li>

                        <a href="/actions/archive?page=${currentPage - 1}&owner=${owner}">
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
                            <a href="/actions/archive?page=${currentPage}&owner=${owner}">
                                    ${i}
                            </a>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li>
                            <a href="/actions/archive?page=${i}&owner=${owner}">
                                    ${i}
                            </a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
            <c:choose>
                <c:when test="${currentPage gt numberOfPages-1}">
                    <li class="disabled"><a
                            href="/actions/archive?page=${currentPage + 1}&owner=${owner}">
                        > </a></li>
                </c:when>
                <c:otherwise>
                    <li><a href="/actions/archive?page=${currentPage + 1}&owner=${owner}">
                        > </a>
                    </li>
                </c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${currentPage gt numberOfPages-6}">
                    <li class="disabled"><a
                            href="/actions/archive?page=${currentPage + 6}&owner=${owner}">
                        >> </a></li>
                </c:when>
                <c:otherwise>
                    <li><a href="/actions/archive?page=${currentPage + 6}&owner=${owner}">
                        >> </a>
                    </li>
                </c:otherwise>
            </c:choose>
            <li><a href="/actions/archive?page=${numberOfPages}&owner=${owner}">
                ${numberOfPages} </a>
            </li>

        </ul>
    </div>

    <table border="1" width="50%" class="table">
        <tr>
            <td width="10%"><b>User</b></td>
            <td width="10%"><b>Key</b></td>
            <td width="40%"><b>Text</b></td>
            <td width="40%"><b>Deleted</b></td>
            <td width="10%"><b>Visits</b></td>
            <td width="20%"><b>Short Link</b></td>
            <td width="20%"><b>Action</b></td>
        </tr>
        <c:forEach items="${list}" var="column">
            <tr>
                <td width="10%">${column.userName}</td>
                <td width="10%">${column.key}</td>
                <td width="40%">${column.link}</td>
                <s:eval
                        expression="T(ru.ivan.linkss.util.Util).convertLocalDateTimeToString(column.deleted)"
                        var="deleted"/>
                <td width="40%">${deleted}</td>
                <td width="10%">${column.visits}</td>
                <td width="20%"><a href="${column.shortLink}">${column.shortLink}</a>
                </td>
                <td>
                    <div class="btn-group">
                        <button type="button" data-toggle="dropdown"
                                class="btn btn-primary dropdown-toggle" >Action
                             <span class="caret"></span></button>
                        <ul class="dropdown-menu">
                            <li><a
                                    href="/actions/user/${column.userName}/archive/restore/?key=${column.key}"><span
                                    class="glyphicon glyphicon-open"></span>Restore</a></li>
                            <li><a
                                    href="/actions/user/${column.userName}/archive/delete/?key=${column.key}"><span
                                    class="glyphicon glyphicon-trash"></span>Delete</a></li>
                        </ul>
                    </div>
                </td>
            </tr>
        </c:forEach>
    </table>

</section>
</body>
</html>
