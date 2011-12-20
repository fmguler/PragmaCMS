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
            setContextPath('${pageContext.request.contextPath}');
        </script>
    </head>
    <body>
        <h1>Edit Template: ${template.name}</h1>
        <a href="${pageContext.request.contextPath}/admin/home">&lt;&lt;Home</a>

        <h2>Template Attributes</h2>
        <c:if test="${not empty missingTemplateAttributes}">
            Missing Attributes:
            <select id="new-template-attribute">
                <c:forEach items="${missingTemplateAttributes}" var="attrEnum">
                    <option value="${attrEnum.attributeName}">${attrEnum.attributeName}</option>
                </c:forEach>
            </select>
            <a href="javascript:addTemplateAttribute(${template.id})">Add</a>
        </c:if>
        <ul>
            <c:forEach items="${template.templateAttributes}" var="attr">
                <li>
                    <a href="javascript:removeAttribute(${attr.attribute.id})">(x)</a>
                    <a href="javascript:updateAttribute(${attr.attribute.id})">(update)</a>
                    ${attr.attribute.attribute}
                </li>
                <textarea id="attribute-${attr.attribute.id}">${attr.attribute.value}</textarea>
            </c:forEach>
        </ul>
    </body>
</html>
