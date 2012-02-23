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
            
        </script>
    </head>
    <body>
        <h1>Edit Template: ${template.name}</h1>
        <a href="home">&lt;&lt;Home</a>

        <h2>Template Attributes</h2>
        <ul>
            <c:forEach items="${template.templateAttributes}" var="attr">
                <li>
                    <a href="javascript:removeAttribute(${attr.id})">(x)</a>
                    <a href="javascript:updateAttribute(${attr.id})">(update)</a>
                    ${attr.attribute}
                </li>
                <textarea id="attribute-${attr.id}">${attr.value}</textarea>
            </c:forEach>
        </ul>
    </body>
</html>
