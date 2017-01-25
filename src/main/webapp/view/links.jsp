<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Links</title>
</head>
<body>
<%@include file="header.jsp" %>
<section>

    <c:choose>
        <c:when test="${shortStat!=null}">
            <h3>Domain Statistics</h3>
            <table border="1" width="50%">
                <c:forEach items="${shortStat}" var="column">
                    <tr>
                        <c:forEach items="${column}" var="row">
                            <td width="50%">
                                    ${row}
                            </td>
                        </c:forEach>
                    </tr>
                </c:forEach>
            </table>
        </c:when>
    </c:choose>

    <h3>Links</h3>
    <table border="1" cellpadding="3" cellspacing="0">
        <tr>
            <td>
                <button
                        onclick="location.href='/actions/links?page=${1}&owner=${owner}'">
                    ${1}
                </button>
            </td>
            <td>
                <c:choose>
                    <c:when test="${currentPage lt 7}">
                        <button disabled="disabled"> <<</button>
                    </c:when>
                    <c:otherwise>
                        <button
                                onclick="location.href='/actions/links?page=${currentPage - 6}&owner=${owner}'">
                            <<
                        </button>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:choose>
                    <c:when test="${currentPage lt 2}">
                        <button disabled="disabled"> <</button>
                    </c:when>
                    <c:otherwise>
                        <button
                                onclick="location.href='/actions/links?page=${currentPage - 1}&owner=${owner}'">
                            <
                        </button>
                    </c:otherwise>
                </c:choose>
            </td>
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
                        <td><b>
                            <button
                                    onclick="location.href='/actions/links?page=${currentPage}&owner=${owner}'">
                                <b>${i}</b>
                            </button>
                        </b></td>
                    </c:when>
                    <c:otherwise>
                        <td>
                            <button
                                    onclick="location.href='/actions/links?page=${i}&owner=${owner}'">
                                    ${i}
                            </button>

                        </td>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
            <td>
                <c:choose>
                    <c:when test="${currentPage gt numberOfPages-1}">
                        <button disabled="disabled"> ></button>
                    </c:when>
                    <c:otherwise>
                        <button
                                onclick="location.href='/actions/links?page=${currentPage + 1}&owner=${owner}'">
                            >
                        </button>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:choose>
                    <c:when test="${currentPage gt numberOfPages-6}">
                        <button disabled="disabled"> >></button>
                    </c:when>
                    <c:otherwise>
                        <button
                                onclick="location.href='/actions/links?page=${currentPage + 6}&owner=${owner}'">
                            >>
                        </button>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <button
                        onclick="location.href='/actions/links?page=${numberOfPages}&owner=${owner}'">
                    ${numberOfPages}
                </button>
            </td>

        </tr>
    </table>

    <br>

    <table border="1" width="50%">
        <tr>
            <td width="10%"><b>Key</b></td>
            <td width="40%"><b>Link</b></td>
            <td width="10%"><b>Days</b></td>
            <td width="10%"><b>Visits</b></td>
            <td width="20%"><b>Short Link</b></td>
            <td width="10%"><b>Delete</b></td>
            <td width="10%"><b>Update</b></td>
        </tr>
        <c:forEach items="${list}" var="column">
            <tr>
                <td width="10%">${column.getKey()}</td>
                <td width="40%"><a href="//${column.getLink()}">${column.getLink()}</a></td>
                <td width="10%">${column.getDays()}</td>
                <td width="10%">${column.getVisits()}</td>
                <td width="20%"><a href="${column.getShortLink()}">${column.getShortLink()}</a>
                </td>
                <td width="10%"><a
                        href="/actions/user/${column.getUserName()}/links/delete/?key=${column.getKey()}">Delete
                </a></td>
                <td width="10%"><a
                        href="/actions/user/${column.getUserName()}/links/edit/?key=${column.getKey()}">Edit
                </a></td>
            </tr>
        </c:forEach>
    </table>

</section>
</body>
</html>
