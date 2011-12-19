<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>fmgCMS Administration</title>
        <script type="text/javascript" src="${pageContext.request.contextPath}/admin/js/jquery-1.7.1.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/admin/js/jquery-ui-1.8.16.custom.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/admin/js/scripts.js"></script>
        <script type="text/javascript">
            $(editPageReady);
            setContextPath('${pageContext.request.contextPath}');
        </script>
    </head>
    <body>
        <h1>Edit Page: ${path}</h1>
        <a href="${pageContext.request.contextPath}/admin/home">&lt;&lt;Home</a>
        <a href="">Add New Attribute</a>
        <h2>Page Attributes</h2>
        <form id="pageForm">
            <input type="hidden" name="path" value="${path}" />
            <ul>
                <c:forEach items="${page.pageAttributes}" var="attr">
                    <li>
                        <a href="">(x)</a>
                        <a href="javascript:updateAttribute('${attr.attribute.id}')">(update)</a>
                        ${attr.attribute.attribute} 
                    </li>
                    <textarea id="attribute-${attr.attribute.id}" cols="60" rows="10">${attr.attribute.value}</textarea>
                </c:forEach>
            </ul>
        </form>

        <h2>Template Attributes (${page.template.name})</h2>
        <ul>
            <c:forEach items="${page.template.templateAttributes}" var="attr">
                <li>${attr.attribute.attribute}</li>
                <textarea>${attr.attribute.value}</textarea>
            </c:forEach>
        </ul>
    </body>
</html>
