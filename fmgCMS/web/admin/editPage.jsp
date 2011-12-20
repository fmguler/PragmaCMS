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
        <h1>Edit Page: ${path} (<a href="${pageContext.request.contextPath}/admin/editTemplate?id=${page.template.id}">${page.template.name}</a>)</h1>
        <a href="${pageContext.request.contextPath}/admin/home">&lt;&lt;Home</a>
        <a href="${path}">Visit Page</a>

        <form id="pageForm">
            <h2>Page Properties</h2>
            Path: <input type="text" name="path" value="${path}"/>
            Template:
            <select name="template.id">
                <option selected value="${page.template.id}">${page.template.name}</option>
                <c:forEach items="${templates}" var="template"><option value="${template.id}">${template.name}</option></c:forEach>
            </select>
            <input type="hidden" name="id" value="${page.id}"/>
            <a href="javascript:savePage()">Save</a>

            <h2>Page Attributes</h2>
            <c:if test="${not empty missingPageAttributes}">
                Missing Attributes:
                <select id="new-page-attribute">
                    <c:forEach items="${missingPageAttributes}" var="attrEnum">
                        <option value="${attrEnum.attributeName}">${attrEnum.attributeName}</option>
                    </c:forEach>
                </select>
                <a href="javascript:addPageAttribute(${page.id})">Add</a>
            </c:if>

            <ul>
                <c:forEach items="${page.pageAttributes}" var="attr">
                    <li>
                        <a href="javascript:removeAttribute(${attr.attribute.id})">(x)</a>
                        <a href="javascript:updateAttribute(${attr.attribute.id})">(update)</a>
                        ${attr.attribute.attribute} 
                    </li>
                    <textarea id="attribute-${attr.attribute.id}" cols="60" rows="10">${attr.attribute.value}</textarea>
                </c:forEach>
            </ul>
        </form>
    </body>
</html>
