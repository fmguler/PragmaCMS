<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Pragma CMS - Templates</title>
        <link rel="shortcut icon" href="js/favicon.ico">
        <link rel="stylesheet" href="js/bootstrap/css/bootstrap.css">
        <link rel="stylesheet" href="js/jquery-ui-base/jquery-ui-1.8.20.custom.css">
        <script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.8.20.custom.min.js"></script>
        <script type="text/javascript" src="js/bootstrap/js/bootstrap.js"></script>
        <script type="text/javascript" src="js/scripts.js"></script>
        <script type="text/javascript">
            var locale = 'en';
            var contextPath = '${pageContext.request.contextPath}';
            $(templatesReady);
        </script>
    </head>
    <body>
        <c:set var="viewMenuTemplates" value="active" />
        <%@include file="_menu.jspf" %>

        <div class="container">
            <div class="content">
                <div class="row">
                    <div class="span12">
                        <div class="page-header">
                            <h1 style="display: inline">List of Templates</h1>
                            <div style="float: right">
                                <button onclick="$('#addTemplateDialog').dialog('open')" class="btn btn-primary btn-large">Add Template</button>
                            </div>
                        </div>
                        <table class="table table-striped">
                            <tr>                                
                                <th>Resource Path</th>
                                <th>Actions</th>
                            </tr>
                            <c:forEach items="${templates}" var="template">
                                <tr id="template-${template.id}">                                    
                                    <td>${template.path}</td>
                                    <td>
                                        <div class="btn-group">
                                            <a class="btn" href="editTemplate?id=${template.id}"><i class="icon-edit"></i> Edit Template</a>
                                            <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                            <ul class="dropdown-menu">
                                                <li><a href="${pageContext.request.contextPath}${template.path}?static" target="_blank"><i class="icon-search"></i> View Template</a></li>
                                                <li class="divider"></li>
                                                <li><a href="javascript:removeTemplate(${template.id})"><i class="icon-trash"></i> Delete Template</a></li>
                                            </ul>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </table>
                    </div>
                </div>
            </div>
            <footer>
                <p>&copy; PragmaCraft 2012</p>
            </footer>
        </div>

        <!-- Add Template Dialog -->
        <div id="addTemplateDialog" title="Add Template">
            <form id="addTemplateForm">
                <table class="style-full-width">                    
                    <tr>
                        <td><strong>Resource Path:</strong></td>
                        <td><input type="text" name="path" value=""/></td>
                    </tr>
                </table>
            </form>
        </div>
    </body>
</html>
