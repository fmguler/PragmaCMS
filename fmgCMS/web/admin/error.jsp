<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>fmgCMS - Error</title>
        <link rel="shortcut icon" href="js/favicon.ico">
        <link href="js/bootstrap/css/bootstrap.css" rel="stylesheet">
        <script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
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
                            <h1>Error</h1>
                        </div>
                        <div class="alert alert-block alert-error fade in">
                            <p>${errorMessage}</p>
                            <c:if test="${not empty errorAction}">
                                <p>
                                    <a class="btn btn-primary" href="${errorActionUrl}">${errorAction}</a>
                                </p>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
            <footer>
                <p>&copy; PragmaCraft 2012</p>
            </footer>
        </div>
    </body>
</html>
