<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Pragma CMS - Account Info</title>
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
            $(accountReady);
        </script>
    </head>
    <body>
        <c:set var="viewMenuAccount" value="active" />
        <%@include file="_menu.jspf" %>

        <div class="container">
            <div class="content">
                <div class="row">
                    <div class="span12">
                        <div class="page-header">
                            <h1 style="display: inline">Account Info</h1>
                            <div style="float: right">
                                <a href="javascript:editSiteDialog()" class="btn btn-large">Add Site</a>
                                <a href="javascript:editAuthorDialog()" class="btn btn-primary btn-large">Add Author</a>
                            </div>
                        </div>
                        <h2>Contact Info</h2>
                        <table class="table">
                            <tr>
                                <th>Company</th>
                                <td>${account.company}</td>
                                <th>Country</th>
                                <td>${account.country}</td>
                            </tr>
                            <tr>
                                <th>Address</th>
                                <td>${account.address}</td>
                                <th>Phone</th>
                                <td>${account.phone}</td>
                            </tr>
                            <tr>
                                <th>City</th>
                                <td>${account.city}</td>
                                <th>State</th>
                                <td>${account.state}</td>
                            </tr>
                            <tr>
                                <th>Primary Contact Author</th>
                                <td>${account.primaryContact.firstName} ${account.primaryContact.lastName} (${account.primaryContact.username}) ${account.primaryContact.email}</td>
                                <td colspan="2">
                                    <a class="btn" href="javascript:editAccountDialog(${account.id})"><i class="icon-edit"></i> Edit Contact Info</a>
                                </td>
                            </tr>
                        </table>
                        <h2>Sites</h2>
                        <table class="table table-striped">
                            <tr>
                                <th>Site ID</th>
                                <th>Domains</th>
                                <th width="150">Actions</th>
                            </tr>
                            <c:forEach items="${account.sites}" var="site">
                                <tr>
                                    <td>${site.id}</td>
                                    <td>${site.domains}</td>
                                    <td>
                                        <div class="btn-group">
                                            <a class="btn" href="javascript:editSiteDialog(${site.id})"><i class="icon-edit"></i> Edit Site</a>
                                            <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                            <ul class="dropdown-menu">
                                                <li><a href="javascript:removeSite(${site.id})"><i class="icon-trash"></i> Delete Site</a></li>
                                            </ul>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </table>
                        <h2>Authors</h2>
                        <table class="table table-striped">
                            <tr>
                                <th>Username</th>
                                <th>First Name</th>
                                <th>Last Name</th>
                                <th>E-Mail</th>
                                <th width="150">Actions</th>
                            </tr>
                            <c:forEach items="${account.authors}" var="author">
                                <tr>
                                    <td>${author.username}</td>
                                    <td>${author.firstName}</td>
                                    <td>${author.lastName}</td>
                                    <td>${author.email}</td>
                                    <td>
                                        <div class="btn-group">
                                            <a class="btn" href="javascript:editAuthorDialog(${author.id})"><i class="icon-edit"></i> Edit Author</a>
                                            <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                            <ul class="dropdown-menu">
                                                <li><a href="javascript:removeAuthor(${author.id})"><i class="icon-trash"></i> Delete Author</a></li>
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

        <!-- Edit Site Dialog -->
        <div id="editSiteDialog" title="Edit Site">
            <form id="editSiteForm">
                <input type="hidden" name="id" />
                <table class="style-full-width">
                    <tr>
                        <td><strong>Domains:</strong></td>
                        <td><textarea name="domains"></textarea></td>
                    </tr>
                </table>
            </form>
        </div>

        <!-- Edit Author Dialog -->
        <div id="editAuthorDialog" title="Edit Author">
            <form id="editAuthorForm">
                <input type="hidden" name="id" />
                <input type="hidden" name="account.id" />
                <table class="style-full-width">
                    <tr>
                        <td><strong>Username:</strong></td>
                        <td><input type="text" name="username" /></td>
                    </tr>
                    <tr>
                        <td><strong>Email:</strong></td>
                        <td><input type="text" name="email" /></td>
                    </tr>
                    <tr>
                        <td><strong>First Name:</strong></td>
                        <td><input type="text" name="firstName" /></td>
                    </tr>
                    <tr>
                        <td><strong>Last Name:</strong></td>
                        <td><input type="text" name="lastName" /></td>
                    </tr>
                    <tr>
                        <td><strong>Password (Enter to reset):</strong></td>
                        <td><input type="password" name="password" /></td>
                    </tr>
                </table>
            </form>
        </div>
    </body>
</html>
