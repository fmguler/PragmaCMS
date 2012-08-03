<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Pragma CMS - Edit Page : ${page.path}</title>
        <link rel="shortcut icon" href="js/favicon.ico">
        <link rel="stylesheet" href="js/bootstrap/css/bootstrap.css" >
        <link rel="stylesheet" href="js/jquery-ui-base/jquery-ui-1.8.20.custom.css">
        <link rel="stylesheet" href="js/prettydiff/diffview.css">
        <script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.8.20.custom.min.js"></script>
        <script type="text/javascript" src="js/jquery.iframe-post-form.js"></script>
        <script type="text/javascript" src="js/bootstrap/js/bootstrap.js"></script>
        <script type="text/javascript" src="js/prettydiff/prettydiff-min.js"></script>
        <script type="text/javascript" src="js/ace/ace.js" charset="utf-8"></script>
        <script type="text/javascript" src="js/ace/mode-html.js" charset="utf-8"></script>
        <script type="text/javascript" src="js/scripts.js"></script>
        <script type="text/javascript">
            var locale = 'en';
            var contextPath = '${pageContext.request.contextPath}';
            var page = null;
            var pageCopy = null;
            var selectedAttributeId = null;
            var selectedAttributeHistory = null;
            var pageAttachments = null;
            var editor = null;
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
                            <div class="style-display-ib style-width-600">
                                <h2 class="style-display-inline">Editing Page: <span id="pagePath">${page.path}</span></h2>
                            </div>
                            <div class="style-float-right">
                                <div class="btn-group style-display-ib">
                                    <a class="btn" href="javascript:uploadAttachmentDialog()"><i class="icon-upload"></i> Upload Attachment</a>
                                    <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                    <ul class="dropdown-menu">
                                        <li><a href="javascript:viewAttachmentsDialog()"><i class="icon-list"></i> View Attachments</a></li>
                                    </ul>
                                </div>
                                <div class="btn-group style-display-ib">
                                    <a class="btn btn-success" href="javascript:saveDialog()"><i class="icon-ok icon-white"></i> Save Changes</a>
                                    <a class="btn btn-success dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                    <ul class="dropdown-menu">
                                        <li><a href="javascript:reviewChangesDialog()"><i class="icon-check"></i> Review Changes</a></li>
                                        <li class="divider"></li>
                                        <li><a href="javascript:viewPage()"><i class="icon-search"></i> View Page</a></li>
                                        <li><a href="javascript:renamePageDialog()"><i class="icon-edit"></i> Rename Page</a></li>
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
                                <a class="btn btn-primary" href="javascript:attributeHistoryDialog()"><i class="icon-list icon-white"></i> Attribute History</a>
                                <a class="btn btn-info" href="javascript:editHtmlDialog()"><i class="icon-pencil icon-white"></i> Edit Attribute HTML</a>
                            </div>
                        </div>

                        <div id="page-header-placeholder"></div>

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
                        <td>Select the attachment to upload</td>
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
            <div id="editor" style="position: relative; width: 730px; height: 500px;"></div>
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

        <!-- Attribute History Dialog -->
        <div id="attributeHistoryDialog" title="Attribute History">
            <table id="previousVersions" class="style-full-width">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                        <th><strong>Author</strong></th>
                        <th><strong>Comment</strong></th>
                        <th><strong>Date</strong></th>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>

                </tbody>
            </table>
        </div>

        <!-- View Changes Dialog -->
        <div id="viewChangesDialog" title="View Changes">
        </div>

        <!-- Review Changes Dialog -->
        <div id="reviewChangesDialog" title="Review Changes">
            <table id="changedAttributes" class="style-full-width">
                <tbody>

                </tbody>
            </table>
        </div>
    </body>
</html>
