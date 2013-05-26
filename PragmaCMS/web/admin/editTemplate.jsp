<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Pragma CMS - Edit Template : ${template.name}</title>
        <link rel="shortcut icon" href="js/favicon.ico">
        <link rel="stylesheet" href="js/bootstrap/css/bootstrap.css" >
        <link rel="stylesheet" href="js/jquery-ui-base/jquery-ui-1.8.20.custom.css">
        <link rel="stylesheet" href="js/prettydiff/diffview.css">
        <script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.8.20.custom.min.js"></script>
        <script type="text/javascript" src="js/bootstrap/js/bootstrap.js"></script>
        <script type="text/javascript" src="js/prettydiff/prettydiff-min.js"></script>
        <script type="text/javascript" src="js/ace/ace.js" charset="utf-8"></script>
        <script type="text/javascript" src="js/ace/mode-html.js" charset="utf-8"></script>
        <script type="text/javascript" src="js/scripts.js"></script>
        <script type="text/javascript">
            var locale = 'en';
            var contextPath = '${pageContext.request.contextPath}';
            var template = null;
            var templateHtml = null;
            var templateHistory = null;
            var newAttributes = new Object();
            var templateAttributes = null;
            var selectedElement = null;
            var editor = null;
            var editorTrackChange = false;
            var editorLastChange = new Date().getTime();            
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
                                <h2 class="style-display-inline">Editing Template:</h2> 
                                <h3><span id="templatePath">${fn:escapeXml(template.path)}</span></h3>
                            </div>
                            <div class="style-float-right">
                                <div class="btn-group style-display-ib">
                                    <a class="btn btn-primary" href="javascript:inspectElement()"><i class="icon-map-marker icon-white"></i> <span id ="inspectButton">Inspect Element</span></a>
                                    <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                    <ul class="dropdown-menu">                                                                                
                                        <li><a href="javascript:makeAttributeDialog()"><i class="icon-plus"></i> Create Editable Region</a></li>                                        
                                    </ul>
                                </div>
                                <div class="btn-group style-display-ib">
                                    <a class="btn btn-success" href="javascript:saveTemplateDialog()"><i class="icon-ok icon-white"></i> Save Changes</a>
                                    <a class="btn btn-success dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                                    <ul class="dropdown-menu">
                                        <li><a href="javascript:reviewTemplateChangesDialog()"><i class="icon-check"></i> Review Changes</a></li>
                                        <li><a href="javascript:revertTemplateChanges()"><i class="icon-repeat"></i> Revert Changes</a></li>
                                        <li><a href="javascript:templateHistoryDialog()"><i class="icon-list"></i> Template History</a></li>
                                        <li class="divider"></li>
                                        <li><a href="javascript:viewTemplate('${template.path}')"><i class="icon-search"></i> View Template</a></li>                                        
                                        <li class="divider"></li>
                                        <li><a href="javascript:removeTemplate(${template.id},true)"><i class="icon-trash"></i> Delete Template</a></li>
                                    </ul>
                                </div>
                            </div>
                            <ul class="nav nav-tabs nav-tabs-right" id="templateTab">
                                <li><a data-toggle="tab" href="#tab-editor">Editor</a></li>                                    
                                <li class="active"><a data-toggle="tab" href="#tab-inspector">Inspector</a></li>               
                            </ul>
                            <div class="tab-content">                                
                                <div class="tab-pane" id="tab-editor">
                                    <div id="editor" style="position: relative; height: 200px;">Please select an element via inspector...</div>
                                </div>               
                                <div class="tab-pane active " id="tab-inspector">
                                    <div id="inspector-holder"></div>
                                </div>
                            </div>                            
                        </div>
                        <div id="page-header-placeholder"></div>
                    </div>
                </div>
            </div>
            <%@include file="_footer.jspf" %>
        </div>

        <!-- Make Attribute Dialog -->
        <div id="makeAttributeDialog" title="Create Editable Region">
            <table class="style-full-width">
                <tr>
                        <td colspan="3">
                            <p>
                                The selected element will be set as editable region in all pages created from this template.
                                <em>Please note that existing pages will also be updated with the default value of current fragment.</em>
                            </p>
                        </td>
                    </tr>
                <tr>
                    <td><strong>Editable Name:</strong></td>
                    <td class="span1"></td>
                    <td><input id="makeAttributeDialogAttribute" class="span3" type="text" name="attribute" value=""/></td>
                </tr>
            </table>
        </div>

        <!-- Save Template Dialog -->
        <div id="saveTemplateDialog" title="Save Template Changes">
            <table class="style-full-width">
                <tr>
                    <td><strong>Comment:</strong></td>
                </tr>
                <tr>
                    <td>
                        <textarea id="saveTemplateDialogComment" class="span7" rows="4">added new version</textarea>
                    </td>
                </tr>
            </table>
        </div>

        <!-- Template History Dialog -->
        <div id="templateHistoryDialog" title="Template History">
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
        
        <%@include file="_scripts.jspf" %>
    </body>
</html>
