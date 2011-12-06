<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>fmgCMS Administration</title>
    </head>
    <body>
        <h1>Hello Admin!</h1>
        <table>
            <c:forEach items="${pages}" var="page">
            <tr>
                <td>${page.path}</td>
                <td><a href="editPage.htm?page=${page.path}">edit</a></td>
            </tr>
            </c:forEach>
        </table>
    </body>
</html>
