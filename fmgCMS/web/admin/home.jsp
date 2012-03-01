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
        <a href="logout">logout</a>
        <h2>All Pages</h2>
        <table>
            <tr>
                <th>Page Path</th>
                <th>Template</th>
                <th>Redirects To</th>
                <td></td>
            </tr>
            <c:forEach items="${pages}" var="page">
                <tr>
                    <td><a href="${pageContext.request.contextPath}${page.path}/edit">${page.path}</a></td>
                    <td><a href="editTemplate?id=${page.template.id}">${page.template.name}</a></td>
                    <td>${page.newPath}</td>
                    <td></td>
                </tr>
            </c:forEach>
        </table>
        
        <h2>All Templates</h2>
        <table>
            <tr>
                <th>Template</th>
                <td></td>
            </tr>
            <c:forEach items="${templates}" var="template">
                <tr>
                    <td><a href="editTemplate?id=${template.id}">${template.name}</a></td>
                    <td></td>
                </tr>
            </c:forEach>
        </table>
    </body>
</html>
