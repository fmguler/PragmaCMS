<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Pragma CMS - Pages</title>
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
            $(pagesReady);
        </script>
    </head>
    <body>
        <c:set var="viewMenuPages" value="active" />
        <%@include file="_menu.jspf" %>

        <div class="container">
            <div class="content">
                <div class="row">
                    <div class="span12">
                        <div class="page-header">
                            <h1 style="display: inline">List of Pages</h1>
                            <div style="float: right">
                                <button onclick="$('#addPageDialog').dialog('open')" class="btn btn-primary btn-large">Add Page</button>
                            </div>
                        </div>
                        <table class="table table-striped">
                            <tr>
                                <th>Page Path</th>
                                <th>Template</th>
                                <th>Redirects To</th>
                                <th width="150">Actions</th>
                            </tr>
                            <c:forEach items="${pages}" var="page">
                                <tr id="page-${page.id}">
                                    <td><a href="${pageContext.request.contextPath}${page.path}/edit">${page.path}</a></td>
                                    <td><a href="editTemplate?id=${page.template.id}">${page.template.name}</a></td>
                                    <td>${page.newPath}</td>
                                    <td>
                                        <div class="btn-group">
                                            <a class="btn" href="${pageContext.request.contextPath}${page.path}/edit"><i class="icon-edit"></i> Edit Page</a>
                                            <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                            <ul class="dropdown-menu">
                                                <li><a href="${pageContext.request.contextPath}${page.path}" target="_blank"><i class="icon-search"></i> View Page</a></li>
                                                <li class="divider"></li>
                                                <li><a href="javascript:removePage(${page.id})"><i class="icon-trash"></i> Delete Page</a></li>
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

        <!-- Add Page Dialog -->
        <div id="addPageDialog" title="Add Page">
            <form id="addPageForm">
                <table class="style-full-width">
                    <tr>
                        <td><strong>Path:</strong></td>
                        <td><input type="text" name="path" value="/the-page-address"/></td>
                    </tr>
                    <tr>
                        <td><strong>Template:</strong></td>
                        <td>
                            <select name="template.id">
                                <c:forEach items="${templates}" var="template"><option value="${template.id}">${template.name}</option></c:forEach>
                            </select>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </body>
</html>
