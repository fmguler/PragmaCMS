<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>fmgCMS - Templates</title>
        <link rel="shortcut icon" href="js/favicon.ico">
        <link href="js/bootstrap/css/bootstrap.css" rel="stylesheet">        
        <script type="text/javascript" src="js/jquery-1.7.1.min.js"></script>
        <script type="text/javascript" src="js/bootstrap/js/bootstrap.js"></script>
    </head>
    <body>
        <c:set var="viewMenuTemplates" value="active" />
        <%@include file="_menu.jspf" %>

        <div class="container">
            <div class="content">
                <div class="row">
                    <div class="span12">
                        <div class="page-header">
                            <h1>List of Templates</h1>
                        </div>
                        <table class="table table-striped">
                            <tr>
                                <th>Template</th>
                                <th>Actions</th>
                            </tr>
                            <c:forEach items="${templates}" var="template">
                                <tr>
                                    <td><a href="editTemplate?id=${template.id}">${template.name}</a></td>
                                    <td>
                                        <div class="btn-group">
                                            <a class="btn btn-primary" href="editTemplate?id=${template.id}"><i class="icon-pencil icon-white"></i> Edit Template</a>
                                            <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                            <ul class="dropdown-menu">
                                                <li><a href="viewTemplate?id=${template.id}" target="_blank"><i class="icon-eye-open"></i> View Template</a></li>
                                                <li class="divider"></li>
                                                <li><a href="#"><i class="icon-trash"></i> Delete Template</a></li>
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
