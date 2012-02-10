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
            setContextPath('${pageContext.request.contextPath}');
        </script>
    </head>
    <body>
        <div style="float: left; width: 500px">
            <h1>${path}</h1>
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
                    <option value="${attr.id}">${attr.attribute}</option>
                </c:forEach>
            </select>
            <a href="javascript:removePageAttribute()">(remove this version)</a>
            <a href="javascript:savePageAttribute()">(save as new version)</a>
        </div>
        <div style="float: left;">
            <c:forEach items="${page.pageAttributes}" var="attr">
                <textarea id="attribute-${attr.id}" class="attribute" style="display:none"  cols="55" rows="6" onchange="onAttributeChange('${attr.attribute}', ${attr.id})">${attr.value}</textarea>
                <input type="hidden" id="attribute-to-id-${attr.attribute}" value="${attr.id}" />
                <input type="hidden" id="id-to-attribute-${attr.id}" value="${attr.attribute}" />
            </c:forEach>
        </div>
        <p/>
        <iframe id="pagePreview" src="${pageContext.request.contextPath}${path}?edit" width="100%" height="480" onLoad="onNavigateAway(this.contentWindow.location.href, this.contentWindow.location.pathname)"></iframe>
    </body>
</html>
