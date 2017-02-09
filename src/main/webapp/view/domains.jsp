<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<%@include file="header.jsp" %>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
      integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
      crossorigin="anonymous">
<link rel="stylesheet"
      href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css"
      integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp"
      crossorigin="anonymous">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
        integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
        crossorigin="anonymous"></script>
<style type="text/css">
    .bs-example {
        margin: 20px;
    }
</style>
<body>
<section>
    <h3>Domains</h3>
    <div class="bs-example">
        <ul class="pagination">
            <li>
                <a href="/actions/domains?page=${1}">
                    ${1}
                </a>
            </li>
                <c:choose>
                    <c:when test="${currentPage lt 7}">
                        <li class="disabled"> <a href="/actions/domains?page=${currentPage - 6}">
                            <<  </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="/actions/domains?page=${currentPage - 6}">
                            <<  </a>
                        </li>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${currentPage lt 2}">
                        <li class="disabled"> <a href="/actions/domains?page=${currentPage - 1}">
                            < </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="/actions/domains?page=${currentPage - 1}">
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
                            <a href="/actions/domains?page=${currentPage}">
                                ${i}
                            </a>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li>
                            <a href="/actions/domains?page=${i}">
                                    ${i}
                            </a>

                        </li>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
                <c:choose>
                    <c:when test="${currentPage gt numberOfPages-1}">
                        <li class="disabled"> <a href="/actions/domains?page=${currentPage + 1}">
                            > </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="/actions/domains?page=${currentPage + 1}">
                            > </a>
                        </li>
                    </c:otherwise>
                </c:choose>
                 <c:choose>
                    <c:when test="${currentPage gt numberOfPages-6}">
                        <li class="disabled"> <a href="/actions/domains?page=${currentPage + 6}">
                            >>  </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="/actions/domains?page=${currentPage + 6}">
                            >>  </a>
                        </li>
                    </c:otherwise>
                </c:choose>

                <li> <a href="/actions/domains?page=${numberOfPages}">
                    ${numberOfPages}
                </li>

        </ul>
    </div>

    <br>

    <table border="1" width="50%" class="table">
        <tr>
            <td width="60%"><b>Domain</b></td>
            <td width="20%"><b>Visits on existing links</b></td>
            <td width="20%"><b>Visits on all links</b></td>
        </tr>
        <c:forEach items="${list}" var="column">
            <tr>
                <td width="60%">${column.name}</td>
                <td width="10%">${column.visitsActual}</td>
                <td width="10%">${column.visitsHistory}</td>
            </tr>
        </c:forEach>
    </table>

</section>
</body>
</html>
