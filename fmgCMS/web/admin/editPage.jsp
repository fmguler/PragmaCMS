<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>fmgCMS - Edit Page : ${page.path}</title>
        <link rel="shortcut icon" href="js/favicon.ico">
        <link rel="stylesheet" href="js/bootstrap/css/bootstrap.css" >
        <link rel="stylesheet" href="js/jquery-ui-base/jquery-ui-1.8.20.custom.css">
        <script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.8.20.custom.min.js"></script>
        <script type="text/javascript" src="js/bootstrap/js/bootstrap.js"></script>
        <script type="text/javascript" src="js/scripts.js"></script>
        <script type="text/javascript">
            var locale = 'en';
            var contextPath = '${pageContext.request.contextPath}';
            var pageAttachments = [<c:forEach items="${pageAttachments}" var="attch">{id: ${attch.id}, name: "${attch.name}"},</c:forEach>0];
            $(editPageReady);
        </script>
    </head>
    <body>
        <c:set var="viewMenuEditPage" value="${page.path}" />
        <%@include file="_menu.jspf" %>

        <div class="container">
            <div class="content">
                <div class="row-fluid">
                    <div class="span12">
                        <div class="page-header style-position-fixed">
                            <h2 class="style-display-inline">Editing Page: ${page.path}</h2>
                            <div class="style-float-right">
                                <div class="btn-group style-display-ib">
                                    <a class="btn" href="javascript:$('#uploadAttachmentDialog').dialog('open')"><i class="icon-upload"></i> Upload Attachment</a>
                                    <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                    <ul class="dropdown-menu">
                                        <li><a href="#"><i class="icon-list"></i> View Attachments</a></li>
                                    </ul>
                                </div>
                                <div class="btn-group style-display-ib">
                                    <a class="btn" href="javascript:$('#renamePageDialog').dialog('open')"><i class="icon-edit"></i> Rename Path</a>
                                    <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                    <ul class="dropdown-menu">
                                        <li><a href="${pageContext.request.contextPath}${page.path}" target="_blank"><i class="icon-eye-open"></i> View Page</a></li>
                                        <li class="divider"></li>
                                        <li><a href="javascript:removePage(${page.id},true)"><i class="icon-trash"></i> Delete Page</a></li>
                                    </ul>
                                </div>
                            </div>
                            <div class="well attribute-menu">
                                Current Attribute:
                                <select class="style-margin-bottom-zero" id="selectedAttributeId" onchange="onSelectedAttributeChange()">
                                    <option value="">--Select--</option>
                                    <c:forEach items="${page.pageAttributes}" var="attr">
                                        <option value="${attr.id}">${attr.attribute}</option>
                                    </c:forEach>
                                </select>
                                &nbsp;&nbsp;
                                <a class="btn btn-success" href="javascript:savePageAttribute()"><i class="icon-ok icon-white"></i> Save</a>
                                <a class="btn btn-primary" href="#"><i class="icon-list icon-white"></i> View Revisions / History</a>
                                <a class="btn btn-danger" href="javascript:removePageAttribute()"><i class="icon-remove icon-white"></i> Remove This Version</a>
                                <a class="btn btn-info" href="javascript:$('#editHtmlDialog').dialog('open')"><i class="icon-pencil icon-white"></i> Edit HTML</a>
                            </div>
                        </div>

                        <div style="height:150px"></div>

                        <%--div>
                            Attachment:
                            <select id="selectedAttachmentId">
                                <option value="">--Select--</option>
                                <c:forEach items="${pageAttachments}" var="attch">
                                    <option value="${attch.id}">${attch.name}</option>
                                </c:forEach>
                            </select>
                            <a href="javascript:removePageAttachment()">(remove attachment)</a>
                        </div--%>
                        <iframe id="pagePreview" src="${pageContext.request.contextPath}${page.path}?edit" width="100%" height="480" onLoad="onNavigateAway(this.contentWindow.location.href, this.contentWindow.location.pathname)"></iframe>
                    </div>
                </div>
            </div>
            <footer>
                <p>&copy; PragmaCraft 2012</p>
            </footer>
        </div>

        <!-- Upload Attachment Dialog -->
        <div id="uploadAttachmentDialog" title="Upload Attachment">
            <form id="uploadAttachmentForm" method="POST" action="uploadPageAttachment" enctype="multipart/form-data">
                <input type="hidden" name="page.id" value="${page.id}"/>
                <input type="hidden" name="page.path" value="${page.path}"/>
                <table>
                    <tr>
                        <td><strong>Select the attachment to upload</strong></td>
                    </tr>
                    <tr>
                        <td>
                            <br/>
                            <input type="file" name="uploadedattachment" />
                        </td>
                    </tr>
                </table>
            </form>
        </div>

        <!-- Rename Page Dialog -->
        <div id="renamePageDialog" title="Rename Page">
            <form id="renamePageForm">
                <input type="hidden" name="pageId" value="${page.id}"/>
                <table>
                    <tr>
                        <td>New Path:</td>
                        <td><input type="text" name="newPath" value="${page.path}"/></td>
                    </tr>
                    <tr>
                        <td>Redirect Old Path:</td>
                        <td><input type="checkbox" name="redirect" value="true" checked /></td>
                    </tr>
                </table>
            </form>
        </div>

        <!-- Edit Html Dialog -->
        <div id="editHtmlDialog" title="Edit Attribute HTML">
            <div>
                <c:forEach items="${page.pageAttributes}" var="attr">
                    <textarea class="span7" rows="20" id="attribute-${attr.id}" class="attribute" style="display:none" onchange="onAttributeChange('${attr.attribute}', ${attr.id})">${attr.value}</textarea>
                    <input type="hidden" id="attribute-to-id-${attr.attribute}" value="${attr.id}" />
                    <input type="hidden" id="id-to-attribute-${attr.id}" value="${attr.attribute}" />
                </c:forEach>
            </div>
        </div>
    </body>
</html>
