<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<%@include file="header.jsp" %>

<body>
<section>
    <h3>Free links</h3>
    <table border="1" cellpadding="3" cellspacing="0">
        <tr>
            <td>
                <button
                        onclick="location.href='/actions/freelinks?page=${1}'">
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
                                onclick="location.href='/actions/freelinks?page=${currentPage - 6}'">
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
                                onclick="location.href='/actions/freelinks?page=${currentPage
                                 - 1}'">
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
                                    onclick="location.href='/actions/freelinks?page=${currentPage}'">
                                <b>${i}</b>
                            </button>
                        </b></td>
                    </c:when>
                    <c:otherwise>
                        <td>
                            <button
                                    onclick="location.href='/actions/freelinks?page=${i}'">
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
                                onclick="location.href='/actions/freelinks?page=${currentPage + 1}'">
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
                                onclick="location.href='/actions/freelinks?page=${currentPage + 6}'">
                            >>
                        </button>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <button
                        onclick="location.href='/actions/freelinks?page=${numberOfPages}'">
                    ${numberOfPages}
                </button>
            </td>

        </tr>
    </table>

    <br>

    <table border="1" width="50%">
        <tr>
            <td width="20%"><b>Free link</b></td>
            <td width="10%"><b></b></td>
        </tr>
        <c:forEach items="${list}" var="column">
            <tr>
                <td width="20%">${column}</td>
                <td width="10%">
                    <a href="/actions/freelinks/${column}/delete">Delete
                    </a></td>
            </tr>
        </c:forEach>
    </table>

</section>
</body>
</html>
