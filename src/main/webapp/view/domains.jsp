<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<body>
<section>
    <div class="container" style="alignment: center">
        <%@include file="header.jsp" %>
        <h2> DOMAINS </h2>
        <div class="bs-example">
            <ul class="pagination">
                <li>
                    <a href="./domains?page=${1}">
                        ${1}
                    </a>
                </li>
                <c:choose>
                    <c:when test="${currentPage lt 7}">
                        <li class="disabled"><a href="/manage/domains?page=${currentPage - 6}">
                            << </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="./domains?page=${currentPage - 6}">
                            << </a>
                        </li>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${currentPage lt 2}">
                        <li class="disabled"><a href="./domains?page=${currentPage - 1}">
                            < </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="./domains?page=${currentPage - 1}">
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
                                <a href="./domains?page=${currentPage}">
                                        ${i}
                                </a>
                            </li>
                        </c:when>
                        <c:otherwise>
                            <li>
                                <a href="./domains?page=${i}">
                                        ${i}
                                </a>

                            </li>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
                <c:choose>
                    <c:when test="${currentPage gt numberOfPages-1}">
                        <li class="disabled"><a href="./domains?page=${currentPage + 1}">
                            > </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="./domains?page=${currentPage + 1}">
                            > </a>
                        </li>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${currentPage gt numberOfPages-6}">
                        <li class="disabled"><a href="./domains?page=${currentPage + 6}">
                            >> </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="./domains?page=${currentPage + 6}">
                            >> </a>
                        </li>
                    </c:otherwise>
                </c:choose>

                <li><a href="./domains?page=${numberOfPages}">
                    ${numberOfPages} </a>
                </li>

            </ul>
        </div>

        <div>
            <h3>VISITS ACTUAL - '${visitsActualSize}'. VISITS HISTORY -
                '${visitsHistorySize}'</h3></div>

        <table border="1" width="50%" class="table">
            <tr>
                <td width="60%"><b>Domain</b></td>
                <td width="20%"><b>Visits on existing links</b></td>
                <td width="20%"><b>Visits on all links</b></td>
            </tr>
            <c:forEach items="${list}" var="column">
                <tr>
                    <td width="60%">${column.name}</td>
                    <td width="10%"><a href="./domain/${column.name}/visits?type=actual">
                            ${column.visitsActual}</a></td>
                    <td width="10%"><a href="./domain/${column.name}/visits?type=history">
                        ${column.visitsHistory}</a></td>
                </tr>
            </c:forEach>
        </table>
    </div>
</section>
</body>
</html>
