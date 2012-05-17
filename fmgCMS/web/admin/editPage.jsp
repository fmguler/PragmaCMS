<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

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
        <script type="text/javascript" src="js/jquery.iframe-post-form.js"></script>
        <script type="text/javascript" src="js/bootstrap/js/bootstrap.js"></script>
        <script type="text/javascript" src="js/scripts.js"></script>
        <script type="text/javascript">
            var locale = 'en';
            var contextPath = '${pageContext.request.contextPath}';
            var page = null;
            var selectedAttributeId = null;
            var selectedAttributeHistory = null;
            var pageAttachments = null;
            $(function(){editPageReady(${page.id}, '${page.path}')});
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
                                    <a class="btn" href="javascript:uploadAttachmentDialog()"><i class="icon-upload"></i> Upload Attachment</a>
                                    <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                    <ul class="dropdown-menu">
                                        <li><a href="javascript:viewAttachmentsDialog()"><i class="icon-list"></i> View Attachments</a></li>
                                    </ul>
                                </div>
                                <div class="btn-group style-display-ib">
                                    <a class="btn" href="javascript:renamePageDialog()"><i class="icon-edit"></i> Rename Page</a>
                                    <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                    <ul class="dropdown-menu">
                                        <li><a href="javascript:viewPage()"><i class="icon-eye-open"></i> View Page</a></li>
                                        <li class="divider"></li>
                                        <li><a href="javascript:removePage(${page.id},true)"><i class="icon-trash"></i> Delete Page</a></li>
                                    </ul>
                                </div>
                            </div>
                            <div class="well attribute-menu">
                                Selected Attribute:
                                <select class="style-margin-bottom-zero" id="selectedAttributeId" onchange="onSelectedAttributeChange()">
                                    <option value="">--Select--</option>
                                    <c:forEach items="${page.pageAttributes}" var="attr">
                                        <option value="${attr.id}">${attr.attribute}</option>
                                    </c:forEach>
                                </select>
                                &nbsp;&nbsp;
                                <a class="btn btn-success" href="javascript:saveDialog()"><i class="icon-ok icon-white"></i> Save Changes</a>
                                <a class="btn btn-primary" href="javascript:historyDialog()"><i class="icon-list icon-white"></i> Page History</a>
                                <a class="btn btn-danger" href="javascript:revertDialog()"><i class="icon-remove icon-white"></i> Revert</a>
                                <a class="btn btn-info" href="javascript:editHtmlDialog()"><i class="icon-pencil icon-white"></i> Edit HTML</a>
                            </div>
                        </div>

                        <div style="height:150px"></div>

                        <iframe id="pagePreview" src="about:blank" width="100%" height="480" onLoad="onIFrameLoad(this.contentWindow.location.href, this.contentWindow.location.pathname)"></iframe>
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
                <input type="hidden" name="pageId" value="${page.id}"/>
                <table class="style-full-width">
                    <tr>
                        <td><strong>Select the attachment to upload</strong></td>
                    </tr>
                    <tr>
                        <td>
                            <br/>
                            <input id="uploadAttachmentDialogFile" type="file" name="pageAttachment" />
                            <div id="uploadAttachmentDialogProgress" class="progress progress-striped active style-hidden">
                                <div class="bar" style="width: 0%"></div>
                            </div>
                        </td>
                    </tr>
                </table>
            </form>
        </div>

        <!-- View Attachments Dialog -->
        <div id="viewAttachmentsDialog" title="View Attachments">
            <table id="pageAttachments" class="style-full-width">
                <thead>
                    <tr>
                        <th><strong>Attachment Name</strong></th>
                        <th><strong>Attachment Size</strong></th>
                        <th><strong>Upload Date</strong></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>

                </tbody>
            </table>
        </div>

        <!-- Rename Page Dialog -->
        <div id="renamePageDialog" title="Rename Page">
            <form id="renamePageForm">
                <input type="hidden" name="pageId" value="${page.id}"/>
                <table class="style-full-width">
                    <tr>
                        <td><strong>New Path:</strong></td>
                        <td class="span1"></td>
                        <td><input class="span4" type="text" name="newPath" value="${page.path}"/></td>
                    </tr>
                    <tr>
                        <td><strong>Redirect Old Path:</strong></td>
                        <td></td>
                        <td><input type="checkbox" name="redirect" value="true" checked /></td>
                    </tr>
                </table>
            </form>
        </div>

        <!-- Edit Html Dialog -->
        <div id="editHtmlDialog" title="Edit Attribute HTML">
            <div>
                <textarea class="span7" rows="20" id="editHtmlDialogTextarea" onchange="onEditHtmlDialogTextareaChange()">[No Attribute Selected]</textarea>
            </div>
        </div>

        <!-- Save Dialog -->
        <div id="saveDialog" title="Save Changes">
            <table class="style-full-width">
                <tr>
                    <td><strong>Comment:</strong></td>
                </tr>
                <tr>
                    <td>
                        <textarea id="saveDialogComment" class="span7" rows="4">added new version</textarea>
                    </td>
                </tr>
            </table>
        </div>

        <!-- Revert Dialog -->
        <div id="revertDialog" title="Revert Attribute To Previous Versions">
            <form id="revertForm">
                <input type="hidden" id="revertDialogAttributeId" name="attributeId" value=""/>
                <table id="previousVersions" class="style-full-width">
                    <thead>
                        <tr>
                            <th></th>
                            <th><strong>Author</strong></th>
                            <th><strong>Comment</strong></th>
                            <th><strong>Date</strong></th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>

                    </tbody>
                </table>
            </form>
        </div>
    </body>
</html>
