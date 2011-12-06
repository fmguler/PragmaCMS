<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>fmgCMS Administration</title>
    </head>
    <body>
        <h1>Edit Page: ${pageUrl}</h1>
        
        <h2>Page Attributes</h2>
        <ul>
            <c:forEach items="${page.pageAttributes}" var="attr">
                <li>${attr.attribute.attribute}</li>
                <textarea>${attr.attribute.value}</textarea>
            </c:forEach>
        </ul>
        
        <h2>Template Attributes (${page.template.name})</h2>
        <ul>
            <c:forEach items="${page.template.templateAttributes}" var="attr">
                <li>${attr.attribute.attribute}</li>
                <textarea>${attr.attribute.value}</textarea>
            </c:forEach>
        </ul>
    </body>
</html>
