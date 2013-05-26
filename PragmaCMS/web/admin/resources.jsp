<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Pragma CMS - Resources</title>
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
            var duplicateResourceParam = '${duplicateResourceParam}';
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
                                <button onclick="$('#addFolderDialog').dialog('open')" class="btn btn-large">Add Folder</button>
                                <button onclick="$('#crawlDialog').dialog('open')" class="btn btn-warning btn-large">Crawl Web Page</button>
                                <button onclick="$('#uploadResourceDialog').dialog('open')" class="btn btn-primary btn-large">Upload Resource</button>
                            </div>
                        </div>
                        <h2>
                            Folder: <a href="resources">home</a>
                            <c:forEach items="${resourceFolderArray}" begin="1" varStatus="i">
                                /
                                <a href="resources?resourceFolder=<c:forEach begin="1" end="${i.index}" varStatus="j">/${resourceFolderArray[j.index]}</c:forEach>">${resourceFolderArray[i.index]}</a>
                            </c:forEach>
                        </h2>
                        <table class="table table-striped">
                            <tr>
                                <th>Resource Name</th>
                                <th>Last Modified</th>
                                <th>Size</th>
                                <th width="160">Actions</th>
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
                                                <a class="btn" href="resources?resourceFolder=${resource.folder}${resource.name}"><i class="icon-folder-open"></i> Open Folder</a>
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
                                            <a href="${pageContext.request.contextPath}${resource.folder}${resource.name}?static" target="_blank">${resource.name}</a>
                                        </td>
                                        <td><fmt:formatDate value="${resource.lastModified}" pattern="dd.MM.yyyy HH:mm" /></td>
                                        <td>${resource.contentLength}</td>
                                        <td>
                                            <div class="btn-group">
                                                <c:if test="${fn:endsWith(resource.name, '.htm')||fn:endsWith(resource.name, '.html')}">
                                                    <a class="btn btn-success" href="editTemplateOfResource?path=${resource.folder}${resource.name}"><i class="icon-edit"></i> Edit Template </a>
                                                    <a class="btn btn-success dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                                    <ul class="dropdown-menu">                                                                                                        
                                                        <li><a href="${pageContext.request.contextPath}${resource.folder}${resource.name}?static" target="_blank"><i class="icon-search"></i> View Template</a></li>
                                                        <li><a href="javascript:duplicateResourceDialog('${resource.folder}${resource.name}', '${resource.name}')"><i class="icon-repeat"></i> Duplicate Template (Copy)</a></li>
                                                        <li><a href="downloadResource?resourcePath=${resource.folder}${resource.name}"><i class="icon-download"></i> Download Resource</a></li>
                                                        <li class="divider"></li>
                                                        <li><a href="javascript:removeResource('${resource.name}')"><i class="icon-trash"></i> Delete Resource</a></li>
                                                    </ul>                                                    
                                                </c:if>
                                                <c:if test="${!(fn:endsWith(resource.name, '.htm')||fn:endsWith(resource.name, '.html'))}">
                                                    <a class="btn" href="${pageContext.request.contextPath}${resource.folder}${resource.name}?static" target="_blank"><i class="icon-search"></i> Preview</a>
                                                    <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                                    <ul class="dropdown-menu">                                                                                                        
                                                        <li><a href="downloadResource?resourcePath=${resource.folder}${resource.name}"><i class="icon-download"></i> Download Resource</a></li>
                                                        <li class="divider"></li>
                                                        <li><a href="javascript:removeResource('${resource.name}')"><i class="icon-trash"></i> Delete Resource</a></li>
                                                    </ul>                                                    
                                                </c:if>

                                            </div>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                        </table>
                    </div>
                </div>
            </div>
            <%@include file="_footer.jspf" %>
        </div>

        <!-- Upload Resource Dialog -->
        <div id="uploadResourceDialog" title="Upload Resource">
            <form id="uploadResourceForm" method="POST" action="uploadResource" enctype="multipart/form-data">
                <input type="hidden" name="resourceFolder" value="${resourceFolder}"/>
                <table class="style-full-width">
                    <tr>
                        <td>
                            <p>
                                Select the file to upload. You can upload zip files, they will be extracted to current folder.
                                <em>Please note that items with same name will be overwritten. Add a new folder if you're not sure.</em>
                            </p>
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

        <!-- Duplicate Resource Dialog -->
        <div id="duplicateResourceDialog" title="Duplicate Template">
            <form id="duplicateResourceForm">
                <input type="hidden" name="resourcePath" value=""/>
                <table class="style-full-width">
                    <tr>
                        <td><strong>Name:</strong></td>
                        <td><input type="text" name="newName" value=""/></td>
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
                        <td colspan="2">
                            <p>
                                Enter the the page URL to crawl. You can crawl the whole site, but this may take too long.
                                All of the page resources will be downloaded to the current folder.
                                <em>Please note that items with same name will be overwritten. Add a new folder if you're not sure.</em>
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td><strong>Page URL:</strong></td>
                        <td><input type="text" name="pageUrl" value="http://"/></td>
                    </tr>
                    <tr>
                        <td><strong>Whole Site:</strong></td>
                        <td><input type="checkbox" name="followLinks" value="false" />  <i>Experimental. Be careful! </i> </td>
                    </tr>
                </table>
            </form>
        </div>

        <%@include file="_scripts.jspf" %>
    </body>
</html>
