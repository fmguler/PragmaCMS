<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>fmgCMS - Edit Template : ${template.name}</title>
        <link rel="shortcut icon" href="js/favicon.ico">
        <link rel="stylesheet" href="js/bootstrap/css/bootstrap.css" >
        <link rel="stylesheet" href="js/jquery-ui-base/jquery-ui-1.8.20.custom.css">
        <link rel="stylesheet" href="js/prettydiff/diffview.css">
        <script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.8.20.custom.min.js"></script>
        <script type="text/javascript" src="js/bootstrap/js/bootstrap.js"></script>
        <script type="text/javascript" src="js/prettydiff/prettydiff-min.js"></script>
        <script type="text/javascript" src="js/scripts.js"></script>
        <script type="text/javascript">
            var locale = 'en';
            var contextPath = '${pageContext.request.contextPath}';
            var template = null;
            var templateCopy = null;
            var selectedAttributeId = null;
            var selectedAttributeHistory = null;
            var selectedElement = null;
            $(function(){editTemplateReady(${template.id}, '${template.path}')});
        </script>
    </head>
    <body>
        <c:set var="viewMenuEditTemplate" value="${template.path}" />
        <%@include file="_menu.jspf" %>

        <div class="container">
            <div class="content">
                <div class="row-fluid">
                    <div class="span12">
                        <div class="page-header style-position-fixed">
                            <div class="style-display-ib style-width-600">
                                <h2 class="style-display-inline">Editing Template: <span id="templateName">${fn:escapeXml(template.name)}</span> (${template.path})</h2>
                            </div>
                            <div class="style-float-right">
                                <div class="btn-group style-display-ib">
                                    <a class="btn btn-primary" href="javascript:inspectElement()"><i class="icon-map-marker icon-white"></i> Inspect Element</a>
                                    <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                    <ul class="dropdown-menu">
                                        <li><a href="javascript:void()"><i class="icon-plus"></i> Make Attribute</a></li>
                                        <li><a href="javascript:editTemplateHtmlDialog()"><i class="icon-pencil"></i> Edit HTML</a></li>
                                        <li><a href="javascript:void()"><i class="icon-font"></i> Edit Inline</a></li>
                                    </ul>
                                </div>
                                <div class="btn-group style-display-ib">
                                    <a class="btn btn-success" href="javascript:saveTemplateDialog()"><i class="icon-ok icon-white"></i> Save Changes</a>
                                    <a class="btn btn-success dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                    <ul class="dropdown-menu">
                                        <li><a href="javascript:void()"><i class="icon-check"></i> Review Changes</a></li>
                                        <li><a href="javascript:void()"><i class="icon-list"></i> Template History</a></li>
                                        <li class="divider"></li>
                                        <li><a href="javascript:viewTemplate()"><i class="icon-search"></i> View Template</a></li>
                                        <li><a href="javascript:renameTemplateDialog()"><i class="icon-edit"></i> Rename Template</a></li>
                                        <li class="divider"></li>
                                        <li><a href="javascript:removeTemplate(${template.id},true)"><i class="icon-trash"></i> Delete Template</a></li>
                                    </ul>
                                </div>
                            </div>
                            <div id="inspector-holder"></div>
                        </div>

                        <div id="page-header-placeholder"></div>

                        <iframe id="templatePreview" src="about:blank" width="100%" height="480" onLoad="onTemplateIFrameLoad(this.contentWindow.location.href, this.contentWindow.location.pathname)"></iframe>
                    </div>
                </div>
            </div>
            <footer>
                <p>&copy; PragmaCraft 2012</p>
            </footer>
        </div>

        <!-- Rename Template Dialog -->
        <div id="renameTemplateDialog" title="Rename Template">
            <form id="renameTemplateForm" onsubmit="return false;">
                <input type="hidden" name="templateId" value="${template.id}"/>
                <table class="style-full-width">
                    <tr>
                        <td><strong>Name:</strong></td>
                        <td class="span1"></td>
                        <td><input class="span4" type="text" name="name" value="${template.name}"/></td>
                    </tr>
                </table>
            </form>
        </div>

        <!-- Edit Template Html Dialog -->
        <div id="editTemplateHtmlDialog" title="Edit Template HTML">
            <div>
                <textarea class="span7" rows="20" id="editTemplateHtmlDialogTextarea" onchange="onEditTemplateHtmlDialogTextareaChange()">[No Attribute Selected]</textarea>
            </div>
        </div>
    </body>
</html>
