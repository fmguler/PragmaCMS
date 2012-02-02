<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>fmgCMS Administration</title>
        <script type="text/javascript" src="js/jquery-1.7.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.8.16.custom.min.js"></script>
        <script type="text/javascript" src="js/scripts.js"></script>
        <script type="text/javascript">
            $(editPageReady);
        </script>
    </head>
    <body>
        <div style="float: left; width: 500px">
            <h1>Edit Page: ${path}</h1>
            <a href="home">&lt;&lt;Home</a>
            <a href="${pageContext.request.contextPath}${path}">Visit Page</a>
            <form id="pageForm">
                <p>
                    Path: <input type="text" name="path" value="${path}"/>
                    Template:
                    <select name="template.id">
                        <option selected value="${page.template.id}">${page.template.name}</option>
                        <c:forEach items="${templates}" var="template"><option value="${template.id}">${template.name}</option></c:forEach>
                    </select>
                    <input type="hidden" name="id" value="${page.id}"/>
                    <a href="javascript:savePage()">Save</a>
                </p>
            </form>
            <c:if test="${not empty missingPageAttributes}">
                Missing Attributes:
                <select id="new-page-attribute">
                    <c:forEach items="${missingPageAttributes}" var="attrEnum">
                        <option value="${attrEnum.attributeName}">${attrEnum.attributeName}</option>
                    </c:forEach>
                </select>
                <a href="javascript:addPageAttribute(${page.id})">Add</a>
                <a href="javascript:addAllPageAttributes('${path}')">Add All</a>
            </c:if>
        </div>
        <div>
            Attribute:
            <select id="selectedAttributeId" onchange="onSelectedAttributeChange()">
                <option value="">--Select--</option>
                <c:forEach items="${page.pageAttributes}" var="attr">
                    <option value="${attr.attribute.id}">${attr.attribute.attribute}</option>
                </c:forEach>
            </select>
            <a href="javascript:removeAttribute()">(remove)</a>
            <a href="javascript:updateAttribute()">(update)</a>
        </div>
        <div style="float: left;">
            <c:forEach items="${page.pageAttributes}" var="attr">
                <textarea id="attribute-${attr.attribute.id}" class="attribute" style="display:none"  cols="55" rows="6">${attr.attribute.value}</textarea>
                <input type="hidden" id="attribute-to-id-${attr.attribute.attribute}" value="${attr.attribute.id}" />
            </c:forEach>
        </div>
        <p/>
        <iframe id="pagePreview" src="${pageContext.request.contextPath}${path}?edit" width="100%" height="600"></iframe>
    </body>
</html>
