<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Pragma CMS - My Profile</title>
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
            $(profileReady);
        </script>
    </head>
    <body>
        <c:set var="viewMenuProfile" value="active" />
        <%@include file="_menu.jspf" %>

        <div class="container">
            <div class="content">
                <div class="row">
                    <div class="span12">
                        <div class="page-header">
                            <h1 style="display: inline">My Profile</h1>
                            <div style="float: right">
                                <a href="javascript:editProfileDialog()" class="btn btn-primary btn-large">Edit Profile</a>
                            </div>
                        </div>
                        <table class="table">
                            <tr>
                                <th>Username</th>
                                <td>${author.username}</td>
                            </tr>
                            <tr>
                                <th>First Name</th>
                                <td>${author.firstName}</td>
                            </tr>
                            <tr>
                                <th>Last Name</th>
                                <td>${author.lastName}</td>
                            </tr>
                            <tr>
                                <th>Email</th>
                                <td>${author.email}</td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
            <%@include file="_footer.jspf" %>
        </div>

        <%@include file="_scripts.jspf" %>
    </body>
</html>
