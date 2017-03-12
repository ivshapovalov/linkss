<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>

<body>
<script>
    $(document).ready(function () {
        $('.dropdown-toggle').dropdown();
    });
</script>
<section>
    <div class="container" style="alignment: center">
        <%@include file="header.jsp" %>
        <h2> USERS </h2>
        <div class="bs-example">
            <ul class="pagination">
                <li>
                    <a href="./users?page=${1}">
                        ${1}
                    </a>
                </li>
                <c:choose>
                    <c:when test="${currentPage lt 7}">
                        <li class="disabled"><a href="./users?page=${currentPage - 6}">
                            << </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="./users?page=${currentPage - 6}">
                            << </a>
                        </li>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${currentPage lt 2}">
                        <li class="disabled"><a href="./users?page=${currentPage - 1}">
                            < </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="./users?page=${currentPage - 1}">
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
                                <a href="./users?page=${currentPage}">
                                        ${i}
                                </a>
                            </li>
                        </c:when>
                        <c:otherwise>
                            <li>
                                <a href="./users?page=${i}">
                                        ${i}
                                </a>

                            </li>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
                <c:choose>
                    <c:when test="${currentPage gt numberOfPages-1}">
                        <li class="disabled"><a href="./users?page=${currentPage + 1}">
                            > </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="./users?page=${currentPage + 1}">
                            > </a>
                        </li>
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${currentPage gt numberOfPages-6}">
                        <li class="disabled"><a href="./users?page=${currentPage + 6}">
                            >> </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="./users?page=${currentPage + 6}">
                            >> </a>
                        </li>
                    </c:otherwise>
                </c:choose>

                <li><a href="./users?page=${numberOfPages}">
                    ${numberOfPages} </a>
                </li>

            </ul>
        </div>

        <br>

        <table border="1" width="50%" class="table">
            <tr>
                <td width="40%"><b>User name</b></td>
                <td width="40%"><b>Password</b></td>
                <td width="10%"><b>Links</b></td>
                <td width="10%"><b>Archive</b></td>
                <td width="10%"><b>Actions</b></td>

            </tr>
            <c:forEach items="${list}" var="column">
                <tr>
                    <td width="40%"><a href="users/${column.userName}/edit">
                            ${column.userName}</a></td>
                    <td width="40%">*</td>
                    <td width="10%">
                        <a href="./links?owner=${column.userName}">${column.linkCount}
                        </a></td>
                    <td width="10%">
                        <a href="./archive?owner=${column.userName}">${column.archiveCount}
                        </a></td>
                    <td width="10%">
                        <div class="btn-group">
                            <button type="button" data-toggle="dropdown"
                                    class="btn btn-primary dropdown-toggle">Action
                                <span class="caret"></span></button>
                            <ul class="dropdown-menu">
                                <li><a
                                        href="./users/${column.userName}/edit"><span
                                        class="glyphicon glyphicon-pencil"></span>Edit</a></li>
                                <li><a
                                        href="./users/${column.userName}/clear"><span
                                        class="glyphicon glyphicon-compressed"></span>Clear</a></li>
                                <li><a
                                        href="./users/${column.userName}/delete"><span
                                        class="glyphicon glyphicon-trash"></span>Delete</a></li>
                            </ul>
                        </div>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>
</section>
</body>
</html>
