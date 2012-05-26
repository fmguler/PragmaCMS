<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>fmgCMS - Resources</title>
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
            var resourceFolder = '${resourceFolder}';
            var addFolderParam = '${addFolderParam}';
            $(resourcesReady);
        </script>

    </head>
    <body>
        <c:set var="viewMenuResources" value="active" />
        <%@include file="_menu.jspf" %>

        <div class="container">
            <div class="content">
                <div class="row">
                    <div class="span12">
                        <div class="page-header">
                            <h1 style="display: inline">List of Resources</h1>
                            <div style="float: right">
                                <a href="javascript:$('#addFolderDialog').dialog('open')" class="btn btn-large">Add Folder</a>
                                <a href="javascript:$('#crawlDialog').dialog('open')" class="btn btn-warning btn-large">Crawl Web Page</a>
                                <a href="javascript:$('#uploadResourceDialog').dialog('open')" class="btn btn-primary btn-large">Upload Resource</a>
                            </div>
                        </div>
                        <h2>
                            Folder: <a href="resources">home</a>
                            <c:forEach items="${resourceFolderArray}" varStatus="i">
                                ${i.index == 0 ? '' : '/'}
                                <a href="resources?resourceFolder=<c:forEach begin="1" end="${i.index}" varStatus="j">/${resourceFolderArray[j.index]}</c:forEach>">${resourceFolderArray[i.index]}</a>
                            </c:forEach>
                        </h2>
                        <table class="table table-striped">
                            <tr>
                                <th>Resource Name</th>
                                <th>Last Modified</th>
                                <th>Size</th>
                                <th width="150">Actions</th>
                            </tr>
                            <c:forEach items="${resources}" var="resource">
                                <c:if test="${resource.directory}">
                                    <tr>
                                        <td>
                                            <a href="resources?resourceFolder=${resource.folder}${resource.name}">${resource.name}</a>
                                        </td>
                                        <td><fmt:formatDate value="${resource.lastModified}" pattern="dd.MM.yyyy HH:mm" /></td>
                                        <td>[FOLDER]</td>
                                        <td>
                                            <div class="btn-group">
                                                <a class="btn" href="resources?resourceFolder=${resource.folder}${resource.name}"><i class="icon-folder-open"></i> Open</a>
                                                <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                                <ul class="dropdown-menu">
                                                    <li><a href="javascript:removeResource('${resource.name}', true)"><i class="icon-trash"></i> Delete Folder</a></li>
                                                </ul>
                                            </div>
                                        </td>
                                    </tr>
                                </c:if>
                                <c:if test="${!resource.directory}">
                                    <tr>
                                        <td>
                                            <a href="${pageContext.request.contextPath}${resource.folder}${resource.name}?preview">${resource.name}</a>
                                        </td>
                                        <td><fmt:formatDate value="${resource.lastModified}" pattern="dd.MM.yyyy HH:mm" /></td>
                                        <td>${resource.contentLength}</td>
                                        <td>
                                            <div class="btn-group">
                                                <a class="btn" href="${pageContext.request.contextPath}${resource.folder}${resource.name}?preview"><i class="icon-file"></i> Open</a>
                                                <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                                <ul class="dropdown-menu">
                                                    <li><a href="downloadResource?resourcePath=${resource.folder}${resource.name}"><i class="icon-download"></i> Download Resource</a></li>
                                                    <li class="divider"></li>
                                                    <li><a href="javascript:removeResource('${resource.name}')"><i class="icon-trash"></i> Delete Resource</a></li>
                                                </ul>
                                            </div>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                        </table>
                    </div>
                </div>
            </div>
            <footer>
                <p>&copy; PragmaCraft 2012</p>
            </footer>
        </div>

        <!-- Upload Resource Dialog -->
        <div id="uploadResourceDialog" title="Upload Resource">
            <form id="uploadResourceForm" method="POST" action="uploadResource" enctype="multipart/form-data">
                <input type="hidden" name="resourceFolder" value="${resourceFolder}"/>
                <table class="style-full-width">
                    <tr>
                        <td>
                            Select the file to upload. You can upload zip files, they will be extracted to current folder.
                            Please note that items with same name will be overwritten.
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <br/>
                            <input id="uploadResourceDialogFile" type="file" name="resource" />
                            <div id="uploadResourceDialogProgress" class="progress progress-striped active style-hidden">
                                <div class="bar" style="width: 0%"></div>
                            </div>
                        </td>
                    </tr>
                </table>
            </form>
        </div>

        <!-- Add Folder Dialog -->
        <div id="addFolderDialog" title="Add Folder">
            <form id="addFolderForm">
                <input type="hidden" name="baseFolder" value="${resourceFolder}"/>
                <table class="style-full-width">
                    <tr>
                        <td><strong>Name:</strong></td>
                        <td><input type="text" name="name" value="new folder"/></td>
                    </tr>
                </table>
            </form>
        </div>

        <!-- Crawl Dialog -->
        <div id="crawlDialog" title="Crawl Web Page">
            <form id="crawlForm">
                <input type="hidden" name="baseFolder" value="${resourceFolder}"/>
                <table class="style-full-width">
                    <tr>
                        <td><strong>Page URL:</strong></td>
                        <td><input type="text" name="pageUrl" value="http://"/></td>
                    </tr>
                    <tr>
                        <td><strong>Whole Site:</strong></td>
                        <td><input type="checkbox" name="followLinks" value="false" /></td>
                    </tr>
                </table>
            </form>
        </div>
    </body>
</html>
