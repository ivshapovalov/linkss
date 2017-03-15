<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="s" uri="http://www.springframework.org/tags" %>

<html>
<body>
<script>
    $(document).ready(function () {
        $('.dropdown-toggle').dropdown();
    });
</script>
<div class="container" style="alignment: center">
    <%@include file="header.jsp" %>
    <h2> VISITS <a href="./map">map</a> </h2>
    <section>
        <div class="bs-example">
            <ul class="pagination">
                <li>
                    <a href="./visits?page=${1}">
                        ${1}
                    </a>
                </li>
                <c:choose>
                    <c:when test="${currentPage lt 7}">
                        <li class="disabled"><a
                                href="./visits?page=${currentPage - 6}">
                            << </a></li>
                    </c:when>
                    <c:otherwise>
                        <li>
                            <a href="./visits?page=${currentPage - 6}">
                                << </a>
                        </li>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${currentPage lt 2}">
                        <li class="disabled"><a
                                href="./visits?page=${currentPage - 1}">
                            < </a></li>
                    </c:when>
                    <c:otherwise>
                        <li>

                            <a href="./visits?page=${currentPage - 1}">
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
                                <a href="./visits?page=${currentPage}">
                                        ${i}
                                </a>
                            </li>
                        </c:when>
                        <c:otherwise>
                            <li>
                                <a href="./visits?page=${i}">
                                        ${i}
                                </a>
                            </li>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
                <c:choose>
                    <c:when test="${currentPage gt numberOfPages-1}">
                        <li class="disabled"><a
                                href="./visits?page=${currentPage + 1}">
                            > </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="./visits?page=${currentPage + 1}">
                            > </a>
                        </li>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${currentPage gt numberOfPages-6}">
                        <li class="disabled"><a
                                href="./visits?page=${currentPage + 6}">
                            >> </a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="./visits?page=${currentPage + 6}">
                            >> </a>
                        </li>
                    </c:otherwise>
                </c:choose>
                <li><a href="./visits?page=${numberOfPages}">
                    ${numberOfPages} </a>
                </li>

            </ul>
        </div>

        <table border="1" width="50%" class="table">
            <tr>
                <td width="10%"><b>Time</b></td>
                <td width="10%"><b>ip</b></td>
                <td width="10%"><b>Country code</b></td>
                <td width="10%"><b>Country name</b></td>
                <td width="10%"><b>Region code</b></td>
                <td width="10%"><b>Region name</b></td>
                <td width="10%"><b>City</b></td>
                <td width="10%"><b>Postal code</b></td>
                <td width="10%"><b>Latitude</b></td>
                <td width="10%"><b>Longitude</b></td>
            </tr>
            <c:forEach items="${list}" var="column">
                <tr>
                    <td width="10%">
                        <s:eval
                                expression="T(ru.ivan.linkss.util.Util).convertLocalDateTimeToString(column.time)"
                                var="time"/>
                    ${time}

                    </td>

                    <td width="10%">${column.ipLocation.ip}</td>
                    <td width="10%">${column.ipLocation.countryCode}</td>
                    <td width="10%">${column.ipLocation.countryName}</td>
                    <td width="10%">${column.ipLocation.region}</td>
                    <td width="10%">${column.ipLocation.regionName}</td>
                    <td width="10%">${column.ipLocation.city}</td>
                    <td width="10%">${column.ipLocation.postalCode}</td>
                    <td width="10%">${column.ipLocation.latitude}</td>
                    <td width="10%">${column.ipLocation.longitude}</td>

                    <td>
                        <div class="btn-group">
                            <button type="button" data-toggle="dropdown"
                                    class="btn btn-primary dropdown-toggle">Action
                                <span class="caret"></span></button>
                            <ul class="dropdown-menu">
                                     <li><a
                                        href="./visit/${column.timeAsMillis()}/delete"><span
                                        class="glyphicon glyphicon-trash"></span>Delete</a></li>
                            </ul>
                        </div>
                    </td>
                </tr>
            </c:forEach>
        </table>

    </section>
</div>
</body>
</html>
