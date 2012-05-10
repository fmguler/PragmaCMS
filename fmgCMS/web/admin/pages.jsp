<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>fmgCMS - Pages</title>
        <link rel="shortcut icon" href="js/favicon.ico">
        <link href="js/bootstrap/css/bootstrap.css" rel="stylesheet">
        <script type="text/javascript" src="js/jquery-1.7.1.min.js"></script>
        <script type="text/javascript" src="js/bootstrap/js/bootstrap.js"></script>
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
                                <a href="javascript:createPage()" class="btn btn-primary btn-large">Create a Page</a>
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
                                <tr>
                                    <td><a href="${pageContext.request.contextPath}${page.path}/edit">${page.path}</a></td>
                                    <td><a href="editTemplate?id=${page.template.id}">${page.template.name}</a></td>
                                    <td>${page.newPath}</td>
                                    <td>
                                        <div class="btn-group">
                                            <a class="btn" href="${pageContext.request.contextPath}${page.path}/edit"><i class="icon-edit"></i> Edit Page</a>
                                            <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                            <ul class="dropdown-menu">
                                                <li><a href="${pageContext.request.contextPath}${page.path}" target="_blank"><i class="icon-eye-open"></i> View Page</a></li>
                                                <li class="divider"></li>
                                                <li><a href="#"><i class="icon-trash"></i> Delete Page</a></li>
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
    </body>
</html>
