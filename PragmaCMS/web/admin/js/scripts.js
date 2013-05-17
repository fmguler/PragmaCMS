/*
 *  PragmaCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */

//------------------------------------------------------------------------------
//PAGES INITS
//------------------------------------------------------------------------------

//on pages ready
function pagesReady(){
    //add page
    $("#addPageDialog").dialog({
        //height: 'auto',
        width: 400,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["ok"][locale],
            click: addPage
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //if this page is opened with addPage param, open add page dialog
    if(window.location.href.indexOf("addPage")>0){
        $("#addPageDialog").find("[name=path]").val(getParameterByName("addPage"));
        $('#addPageDialog').dialog('open');
    }
}

//on edit page ready
function editPageReady(pageId, pagePath){
    //to make ajax upload
    $('#uploadAttachmentForm').iframePostForm({
        json : false,
        post : uploadAttachmentDialogPost,
        complete : uploadAttachmentDialogComplete
    });

    //upload attachment (modal)
    $("#uploadAttachmentDialog").dialog({
        //height: 'auto',
        width: 370,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["upload"][locale],
            click: function(){
                $("#uploadAttachmentForm").submit();
            }
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //view attachment (modal)
    $("#viewAttachmentsDialog").dialog({
        height: 500,
        width: 570,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["ok"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //rename page (modal)
    $("#renamePageDialog").dialog({
        //height: 'auto',
        width: 470,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["save"][locale],
            click: renamePage
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //edit html (modal)
    $("#editHtmlDialog").dialog({
        //height: 'auto',
        width: 750,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["ok"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //save (modal)
    $("#saveDialog").dialog({
        //height: 'auto',
        width: 570,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-success',
            text: messages["button_save_and_publish"][locale],
            click: function(){
                savePageAttributes(true);
            }
        },{
            'class': 'btn btn-inverse',
            text: messages["button_save_as_draft"][locale],
            click: function(){
                savePageAttributes(false);
            }
        },{
            'class': 'btn btn-primary',
            text: messages["button_review_changes"][locale],
            click: reviewChangesDialog
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //attribute history (modal)
    $("#attributeHistoryDialog").dialog({
        height: 500,
        width: 750,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["button_view_changes"][locale],
            click: attributeHistoryDialogViewChanges
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //view changes (modal)
    $("#viewChangesDialog").dialog({
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["ok"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //review changes (modal)
    $("#reviewChangesDialog").dialog({
        width: 370,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["ok"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //get the page as json
    $.ajax({
        url: 'getPage',
        data: 'pageId='+pageId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                page = response.object;
                pageCopy = $.extend(true, {}, page);
            }
        }
    });

    //get the page attachments as json
    $.ajax({
        url: 'getPageAttachments',
        data: 'pageId='+pageId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                pageAttachments = response.object;
            }
        }
    });

    //load the page preview iframe
    var pagePreviewSrc = contextPath+pagePath+"?time="+(new Date()).getTime()+"&edit";
    $("#pagePreview").attr("src", pagePreviewSrc);

    //configure the ACE code editor
    editor = ace.edit("editor");
    var HtmlMode = require("ace/mode/html").Mode;
    editor.getSession().setMode(new HtmlMode());
    editor.getSession().setUseWrapMode(true);
    editor.getSession().on('change', onEditHtmlDialogUpdate);
}

//on resources ready
function resourcesReady(){
    //upload resource (modal)
    $("#uploadResourceDialog").dialog({
        //height: 'auto',
        width: 370,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["upload"][locale],
            click: uploadResource
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //add folder
    $("#addFolderDialog").dialog({
        //height: 'auto',
        width: 400,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["ok"][locale],
            click: addFolder
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });
    
    //duplicate resource
    $("#duplicateResourceDialog").dialog({
        //height: 'auto',
        width: 400,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["ok"][locale],
            click: duplicateResource
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //crawl web page
    $("#crawlDialog").dialog({
        //height: 'auto',
        width: 400,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["ok"][locale],
            click: crawlWebPage
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //if this page is opened with addFolder param, open add folder dialog
    if(addFolderParam){
        $("#addFolderDialog").find("[name='name']").val(addFolderParam);
        $('#addFolderDialog').dialog('open');
    }
}

//on templates ready
function templatesReady(){
    //add template
    $("#addTemplateDialog").dialog({
        //height: 'auto',
        width: 400,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["ok"][locale],
            click: addTemplate
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //if this page is opened with addTemplate param, open add template dialog
    if(window.location.href.indexOf("addTemplate")>0){
        $("#addTemplateDialog").find("[name=path]").val(getParameterByName("addTemplate"));        
        $('#addTemplateDialog').dialog('open');
    }
}

//on edit template ready
function editTemplateReady(templateId, templatePath){
    //rename template (modal)
    $("#renameTemplateDialog").dialog({
        //height: 'auto',
        width: 470,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["save"][locale],
            click: renameTemplate
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //action (modal)
    $("#actionDialog").dialog({
        //height: 'auto',
        width: 470,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-success',
            text: messages["button_edit_html"][locale],
            click: function() {
                $(this).dialog("close");
                editTemplateHtmlDialog();
            }
        },{
            'class': 'btn btn-inverse',
            text: messages["button_edit_inline"][locale],
            click: function() {
                //$(this).dialog("close");
                editTemplateInlineDialog();
            }
        },{
            'class': 'btn btn-primary',
            text: messages["button_make_attribute"][locale],
            click: function() {
                $(this).dialog("close");
                makeAttributeDialog();
            }
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //edit template html (modal)
    $("#editTemplateHtmlDialog").dialog({
        //height: 'auto',
        width: 750,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["update"][locale],
            click: editTemplateHtmlDialogUpdate
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //make attribute (modal)
    $("#makeAttributeDialog").dialog({
        //height: 'auto',
        width: 470,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["button_make_attribute"][locale],
            click: makeAttribute
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //save template (modal)
    $("#saveTemplateDialog").dialog({
        //height: 'auto',
        width: 570,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-success',
            text: messages["button_save_and_publish"][locale],
            click: function(){
                saveTemplate(true);
            }
        },{
            'class': 'btn btn-inverse',
            text: messages["button_save_as_draft"][locale],
            click: function(){
                saveTemplate(false);
            }
        },{
            'class': 'btn btn-primary',
            text: messages["button_review_changes"][locale],
            click: reviewTemplateChangesDialog
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //template history (modal)
    $("#templateHistoryDialog").dialog({
        height: 500,
        width: 750,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["button_view_changes"][locale],
            click: templateHistoryDialogViewChanges
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //view changes (modal)
    $("#viewChangesDialog").dialog({
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["ok"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //get the template as json
    $.ajax({
        url: 'getTemplate',
        data: 'templateId='+templateId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                template = response.object;
            }
        }
    });

    //get the original template html as json
    $.ajax({
        url: 'getTemplateHtml',
        data: 'original=true&templateId='+templateId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                templateHtml = response.object;
            }
        }
    });

    //load the page preview iframe
    var templatePreviewSrc = contextPath+templatePath+"?time="+(new Date()).getTime()+"&static&edit";
    $("#page-header-placeholder").after('<iframe id="templatePreview" src="'+templatePreviewSrc+'" width="100%" height="480" onLoad="onTemplateIFrameLoad(this.contentWindow.location.href, this.contentWindow.location.pathname)"></iframe>');

    //configure the ACE code editor
    editor = ace.edit("editor");
    var HtmlMode = require("ace/mode/html").Mode;
    editor.getSession().setMode(new HtmlMode());
    editor.getSession().setUseWrapMode(true);
}

//on account ready
function accountReady(){
    //edit site(modal)
    $("#editSiteDialog").dialog({
        //height: 'auto',
        width: 470,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["save"][locale],
            click: saveSite
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //edit author(modal)
    $("#editAuthorDialog").dialog({
        //height: 'auto',
        width: 470,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["save"][locale],
            click: saveAuthor
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });
}

//on profile ready
function profileReady(){

}

//------------------------------------------------------------------------------
//PAGES ACTIONS
//------------------------------------------------------------------------------

//add page
function addPage(){
    var addPageForm = $("#addPageForm").serializeObject();
    if (addPageForm["template.id"] == null){
        showErrorDialog(messages["error_template_not_selected"][locale]);
        return;
    }
    
    $.ajax({
        url: 'addPage',
        data: addPageForm,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                location.href = 'editPage?path='+response.object.path;
            }
        }
    });
}

//remove page
function removePage(pageId, goback){
    if(!confirm(messages["confirm_remove_page"][locale])) return;

    $.ajax({
        url: 'removePage',
        data: 'pageId='+pageId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                //we're in edit page mode, go back to page list
                if (goback) location.href = 'pages';

                //hide the page row
                $("#page-"+pageId).css({
                    "background-color" : "#fbcdcd"
                }, 'fast').fadeOut("fast");
            }
        }
    });
}

//------------------------------------------------------------------------------
//EDIT PAGE AJAX ACTIONS
//------------------------------------------------------------------------------

//view the current page
function viewPage(){
    window.open(contextPath + page.path);
}

//rename page path
function renamePage(){
    var renamePageForm = $("#renamePageForm").serializeObject();
    $.ajax({
        url: 'renamePage',
        data: renamePageForm,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                $("#pagePath").text(renamePageForm.newPath);
                $('#renamePageDialog').dialog('close');
                showStatusDialog(response.message);
            }
        }
    });
}

//save all page attributes
function savePageAttributes(publish){
    var pageAttributes = new Object();
    pageAttributes.pageId = page.id;
    pageAttributes.comment = $("#saveDialogComment").val();
    pageAttributes.publish = publish;

    //set the values as attribute-id = value
    for(var i=0; i<page.pageAttributes.length; i++){
        pageAttributes["attribute-"+page.pageAttributes[i].id]=page.pageAttributes[i].value;
    }

    //save with ajax
    $.ajax({
        url: 'savePageAttributes',
        data: pageAttributes,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                $('#saveDialog').dialog('close');

                //get result
                var result = response.object;
                var savedAttrs = result.savedAttributes;
                page = result.page;
                pageCopy = $.extend(true, {}, page);

                //add saved attributes to message
                var message = response.message+"<br/>";
                message += "<ul>";
                for (var i=0; i<savedAttrs.length; i++){
                    message += "<li>"+savedAttrs[i]+"</li>"
                }
                message += "</ul>";

                //show message with saved attrs
                showStatusDialog(message);
            }
        }
    });
}

//revert attribute to selected history version
function revertPageAttribute(attributeId, attributeHistoryId){
    if(!confirm(messages["confirm_revert_page_attribute"][locale])) return;

    $.ajax({
        url: 'revertPageAttribute',
        data: 'attributeId='+attributeId+'&attributeHistoryId='+attributeHistoryId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                $('#revertDialog').dialog('close');
                showStatusDialog(response.message);

                //update the attribute state accordingly
                var attribute = attributeById(attributeId);
                attribute.value = attributeHistoryById(attributeHistoryId).value;
                attribute.version = attributeHistoryId;

                //update editable html
                var editable = $("#pagePreview").contents().find("#attribute-editable-"+attribute.attribute);
                editable.html(attribute.value);
            }
        }
    });
}

//remove page attachment
function removePageAttachment(attachmentId, elemIndex){
    if(!confirm(messages["confirm_remove_page_attachment"][locale])) return;

    $.ajax({
        url: 'removePageAttachment',
        data: 'attachmentId='+attachmentId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                //hide the attachment row
                $("#attachment-"+attachmentId).css({
                    "background-color" : "#fbcdcd"
                }, 'fast').fadeOut("fast");
                //remove the element from attachments array
                pageAttachments.splice(elemIndex,1);
            }
        }
    });
}

//revert template to selected history version
function removePageAttributeHistory(attributeHistoryId){
    if(!confirm(messages["confirm_remove_page_attribute_history"][locale])) return;

    $.ajax({
        url: 'removePageAttributeHistory',
        data: 'attributeHistoryId='+attributeHistoryId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                $("#history-"+attributeHistoryId).css({
                    "background-color" : "#fbcdcd"
                }, 'fast').fadeOut("fast");
            }
        }
    });
}

//------------------------------------------------------------------------------
//EDIT PAGE DIALOGS
//------------------------------------------------------------------------------

//rename dialog, calls renamePage
function renamePageDialog(){
    $('#renamePageDialog').dialog('open');
}

//upload dialog
function uploadAttachmentDialog(){
    $('#uploadAttachmentDialog').dialog('open');

    //make sure that progres bar is hidden
    $("#uploadAttachmentDialogProgress").find("div").css("width", "0%");
    $("#uploadAttachmentDialogProgress").hide();
    $("#uploadAttachmentDialogFile").show();
    $("#uploadAttachmentDialogFile").val("");
}

//called on upload dialog post
function uploadAttachmentDialogPost(){
    if ($("#uploadAttachmentDialogFile").val() == ""){
        alert("Please select a file");
        return false;
    }

    //fake progress bar :)
    $("#uploadAttachmentDialogFile").fadeOut('fast', function(){
        $("#uploadAttachmentDialogProgress").fadeIn('fast');
        setTimeout("uploadProgress('#uploadAttachmentDialogProgress', 0)", 100);
    });

    return true;
}

//called on upload dialog complete
function uploadAttachmentDialogComplete(responseStr){
    var response = $.parseJSON(responseStr.substring(responseStr.indexOf('{'), responseStr.indexOf('</pre>')));
    if (response.status != "0") {
        showErrorDialog(response.message);
    } else {
        pageAttachments = response.object;
        $('#uploadAttachmentDialog').dialog('close');
        showStatusDialog(response.message);
    }
}

//view attachments dialog, calls removeAttachment
function viewAttachmentsDialog(){
    $('#viewAttachmentsDialog').dialog('open');

    //populate with attachments
    $('#pageAttachments > tbody').empty();
    for (var i = 0; i < pageAttachments.length; i++) {
        var pageAttachmentRow = "<tr id='attachment-"+pageAttachments[i].id+"'>";
        pageAttachmentRow += "<td>"+pageAttachments[i].name + "</td>";
        pageAttachmentRow += "<td>"+pageAttachments[i].contentLength + "</td>";
        pageAttachmentRow += "<td>"+pageAttachments[i].lastModified + "</td>";
        pageAttachmentRow += "<td><a class='btn' href='javascript:removePageAttachment("+pageAttachments[i].id+","+i+")'>Delete</a></td>";
        pageAttachmentRow += "</tr>";
        $('#pageAttachments > tbody').append(pageAttachmentRow);
    }
}

//save dialog, calls publish/draft/review
function saveDialog(){
    //tell aloha to update current attribute value
    $("#pagePreview")[0].contentWindow.forceAlohaChange();

    //show the dialog
    $('#saveDialog').dialog('open');
}

//review changes, diff to published version
function reviewChangesDialog(){
    //tell aloha to update current attribute value
    $("#pagePreview")[0].contentWindow.forceAlohaChange();

    //show the dialog
    $('#reviewChangesDialog').dialog('open');

    //populate with changed attributes
    $('#changedAttributes > tbody').empty();
    var changedAttrCount = 0;
    for (var i = 0; i < page.pageAttributes.length; i++) {
        if(page.pageAttributes[i].value == pageCopy.pageAttributes[i].value) continue; //no change
        var changedAttributeRow = "<tr>";
        changedAttributeRow += "<td>"+page.pageAttributes[i].attribute + "</td>";
        changedAttributeRow += "<td><a class='btn' href='javascript:reviewChangesDialogViewChanges("+i+")'>View Changes</a></td>";
        changedAttributeRow += "<td><a class='btn' href='javascript:reviewChangesDialogRevert("+i+")'>Revert</a></td>";
        changedAttributeRow += "</tr>";
        $('#changedAttributes > tbody').append(changedAttributeRow);
        changedAttrCount++;
    }

    //no change
    if(!changedAttrCount){
        $('#reviewChangesDialog').dialog('close');
        showStatusDialog("No attribute is changed!");
    }
}

//view changes between history attributes
function reviewChangesDialogViewChanges(elemIndex){
    //diff
    var api = new Object();
    api.source = pageCopy.pageAttributes[elemIndex].value == "" ? "[Empty]" : pageCopy.pageAttributes[elemIndex].value;
    api.diff = page.pageAttributes[elemIndex].value == "[Empty]" ? "" : page.pageAttributes[elemIndex].value;
    api.mode = "diff";
    api.diffview = "sidebyside";
    api.sourcelabel = "Original"
    api.difflabel = "Changed"
    var result = prettydiff(api);

    //open view changes dialog
    viewChangesDialog(result[0]);
}

//revert attribute to original version
function reviewChangesDialogRevert(elemIndex){
    //update the attribute value
    var attribute = page.pageAttributes[elemIndex];
    attribute.value = pageCopy.pageAttributes[elemIndex].value;

    //update editable html
    var editable = $("#pagePreview").contents().find("#attribute-editable-"+attribute.attribute);
    editable.html(attribute.value);
}

//attribute history dialog, previews attribute html to prev versions, or calls revertPageAttribute
function attributeHistoryDialog(){
    if (!selectedAttributeId){
        alert("Please select an attribute first.");
        return;
    }
    //populate the dialog with history records of the current attribute
    attributeHistoryDialogPopulate(attributeById(selectedAttributeId));

    //open the dialog
    $('#attributeHistoryDialog').dialog('open');
}

//populates the history dialog with the selected attribute history
function attributeHistoryDialogPopulate(attribute){
    $.ajax({
        url: 'getPageAttributeHistories',
        data: 'pageId='+page.id+"&attribute="+attribute.attribute,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                selectedAttributeHistory = response.object;
                $('#previousVersions > tbody').empty();
                for (var i = 0; i < selectedAttributeHistory.length; i++) {
                    var current = (selectedAttributeHistory[i].id==attribute.version);
                    var attributeHistoryRow = current?"<tr id='history-"+selectedAttributeHistory[i].id+"' style='background-color:#00ff00'>":"<tr id='history-"+selectedAttributeHistory[i].id+"'>";
                    attributeHistoryRow += "<td><input type='radio' name='attribute1' value='"+i+"' "+(current?"checked":"")+" /></td>";
                    attributeHistoryRow += "<td><input type='radio' name='attribute2' value='"+i+"' "+(i==0?"checked":"")+" /></td>";
                    attributeHistoryRow += "<td>"+selectedAttributeHistory[i].author + "</td>";
                    attributeHistoryRow += "<td>"+selectedAttributeHistory[i].comment + "</td>";
                    attributeHistoryRow += "<td>"+selectedAttributeHistory[i].date + "</td>";
                    attributeHistoryRow += "<td><a class='btn' href='javascript:attributeHistoryDialogRevert("+i+")'>Revert</a></td>";
                    attributeHistoryRow += "<td><a class='btn' href='javascript:revertPageAttribute("+attribute.id+","+selectedAttributeHistory[i].id+")'>Publish</a></td>";
                    attributeHistoryRow += current?"<td></td>":"<td><a class='btn' href='javascript:removePageAttributeHistory("+selectedAttributeHistory[i].id+")'>Remove</a></td>";
                    attributeHistoryRow += "</tr>";
                    $('#previousVersions > tbody').append(attributeHistoryRow);
                }
            }
        }
    });
}

//view changes between history attributes
function attributeHistoryDialogViewChanges(){
    var attr1 = $("#attributeHistoryDialog").find('input:radio[name=attribute1]:checked').val();
    var attr2 = $("#attributeHistoryDialog").find('input:radio[name=attribute2]:checked').val();

    if (!attr1 || !attr2){
        alert("Please choose both attributes to be compared.");
        return;
    }
    if (attr1 == attr2){
        alert("You are trying to compare the attribute with itself. Please select two different attributes to compare.");
        return;
    }

    //diff
    var api = new Object();
    api.source = selectedAttributeHistory[attr1].value == "" ? "[Empty]" : selectedAttributeHistory[attr1].value;
    api.diff = selectedAttributeHistory[attr2].value == "" ? "[Empty]" : selectedAttributeHistory[attr2].value;
    api.mode = "diff";
    api.diffview = "sidebyside";
    api.sourcelabel = selectedAttributeHistory[attr1].date;
    api.difflabel = selectedAttributeHistory[attr2].date;
    var result = prettydiff(api);

    //open view changes dialog
    viewChangesDialog(result[0]);
}

//revert attribute to specified version - preview
function attributeHistoryDialogRevert(version){
    //update the attribute value
    var attribute = attributeById(selectedAttributeId);
    attribute.value = selectedAttributeHistory[version].value;

    //update editable html
    var editable = $("#pagePreview").contents().find("#attribute-editable-"+attribute.attribute);
    editable.html(attribute.value);
}

//view changes table
function viewChangesDialog(changesTable){
    //set the diff table in the view changes dialog
    $("#viewChangesDialog").html(changesTable);
    $("#viewChangesDialog").find("tfoot").remove();

    //show the dialog
    $('#viewChangesDialog').dialog('option', "height", 600);
    $('#viewChangesDialog').dialog('option', "width", 900);
    $('#viewChangesDialog').dialog('open');


    //adjust width the table is smaller
    if($("#viewChangesDialog").find("table").width() < 850){
        $('#viewChangesDialog').dialog('option', "width", $("#viewChangesDialog").find("table").width()+50);
    }

    //adjust height the table is smaller
    if($("#viewChangesDialog").find("table").height() < 550){
        $('#viewChangesDialog').dialog('option', "height", $("#viewChangesDialog").find("table").height()+110);
    }

    //center the dialog
    $('#viewChangesDialog').dialog('option', "position", "center");
}

//edit page attribute contents (html)
function editHtmlDialog(){
    if (!selectedAttributeId){
        alert("Please select an attribute first.");
        return;
    }

    //tell aloha to update current attribute value
    $("#pagePreview")[0].contentWindow.forceAlohaChange();

    //update the textarea
    editor.setValue(attributeById(selectedAttributeId).value);
    editor.gotoLine(0);

    //show the dialog
    $('#editHtmlDialog').dialog('open');
}

//------------------------------------------------------------------------------
//EDIT PAGE EVENTS
//------------------------------------------------------------------------------

//display selected attribute textarea
function onSelectedAttributeChange(){
    //set the selected attribute id, used in all other places to detect current attribute
    selectedAttributeId = $("#selectedAttributeId").val();

    //no current attribute is selected
    if (selectedAttributeId == ''){
        selectedAttributeId = null;
        return;
    }

    //focus to the selected editable
    var editable = $("#pagePreview").contents().find("#attribute-editable-"+attributeById(selectedAttributeId).attribute);
    editable.focus();
}

//edit page attribute html update button
function onEditHtmlDialogUpdate(){
    if (!selectedAttributeId) return;

    //update the attribute value
    var attribute = attributeById(selectedAttributeId);
    attribute.value = editor.getValue();

    //update editable html
    var editable = $("#pagePreview").contents().find("#attribute-editable-"+attribute.attribute);
    editable.html(attribute.value);
}

//aloha edited content is changed, update texts
function onAlohaChange(attributeName, html){
    var attribute = attributeByName(attributeName);
    attribute.value = html;
}

//called when an editable is clicked
function onAlohaClick(attributeName){
    var attribute = attributeByName(attributeName);

    //make attribute selected
    $("#selectedAttributeId").find("option:selected").removeAttr("selected");
    $("#selectedAttributeId").find("option[value='"+attribute.id+"']").attr("selected", "selected");
    selectedAttributeId = attribute.id;
}

//called when user scrolls by mouse wheel on iframe
function onIFrameScroll(event){
    $("body").scrollTop($("body").scrollTop() - event.originalEvent.wheelDelta);
}

//called when preview iframe is loaded
function onIFrameLoad(url, path){
    //initial opening, no src
    if (url == 'about:blank') return true;

    //prevent links
    $("#pagePreview").contents().find("a").click(function(event) {
        if ($(event.currentTarget).parents().hasClass('aloha-floatingmenu')) return true; //do not meddle aloha
        if (!confirm("Any unsaved changes will be lost if you navigate away from this page, are you sure?")) event.preventDefault();
    });

    //an edit mode page is opened, adjust the host page
    if (url.substring(url.length-5, url.length)=="&edit"){
        var iframeWidth = $("#pagePreview").contents().find("body")[0].scrollWidth;
        var iframeHeight = $("#pagePreview").contents().find("body")[0].scrollHeight;

        //adjust page elems width when the document is ready
        setTimeout(function() {
            if (iframeWidth>940){
                $(".content").width(iframeWidth);
                $(".container").width(iframeWidth);
                $(".page-header").width(iframeWidth);
            }
            $("#page-header-placeholder").height($(".page-header").outerHeight());

            //resize iframe to actual content
            $("#pagePreview").width(iframeWidth);
            $("#pagePreview").height(iframeHeight+100);
        }, 100);

        return true;
    }

    //a link is clicked within the iframe, reopen this page in edit mode
    location.href = "editPage?path=" + (path.substring(contextPath.length, path.length));
    return false;
}

//------------------------------------------------------------------------------
//EDIT PAGE UTILITY
//------------------------------------------------------------------------------

//attribute by id
function attributeById(attributeId){
    for(var i=0; i<page.pageAttributes.length; i++){
        if (attributeId==page.pageAttributes[i].id) return page.pageAttributes[i];
    }
    alert("Error - no attribute with id: "+attributeId);
    return null;
}

//attribute by name
function attributeByName(attribute){
    for(var i=0; i<page.pageAttributes.length; i++){
        if (attribute==page.pageAttributes[i].attribute) return page.pageAttributes[i];
    }
    alert("Error - no attribute named: "+attribute);
    return null;
}

//attribute history by id
function attributeHistoryById(attributeHistoryId){
    for(var i=0; i<selectedAttributeHistory.length; i++){
        if (attributeHistoryId==selectedAttributeHistory[i].id) return selectedAttributeHistory[i];
    }
    alert("Error - no attribute history with id: "+attributeHistoryId);
    return null;
}

//set upload progress bar
function uploadProgress(progressBar, percent){
    if (!$(progressBar).is(":visible")) return;
    $(progressBar).find("div").css("width", percent+"%");
    var newPercent = percent+1;
    if (newPercent>=100) return;
    setTimeout("uploadProgress('"+progressBar+"',"+newPercent+")", 100);
}

//------------------------------------------------------------------------------
//RESOURCES ACTIONS
//------------------------------------------------------------------------------

//upload the resource, with regular submit
function uploadResource(){
    if ($("#uploadResourceDialogFile").val() == ""){
        alert("Please select a file");
        return;
    }

    //post the form, start upload
    $("#uploadResourceForm").submit();

    //fake progress bar :)
    $("#uploadResourceDialogFile").fadeOut('fast', function(){
        $("#uploadResourceDialogProgress").fadeIn('fast');
        setTimeout("uploadProgress('#uploadResourceDialogProgress',0)", 100);
    });
}

//add folder
function addFolder(){
    var addFolderForm =  $("#addFolderForm").serializeObject();
    $.ajax({
        url: 'addFolder',
        data: addFolderForm,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                location.href = 'resources?resourceFolder='+resourceFolder+addFolderForm.name;
            }
        }
    });
}

//duplicate resource
function duplicateResource(){
    var dupliateResourceForm =  $("#duplicateResourceForm").serializeObject();
    $.ajax({
        url: 'duplicateResource',
        data: dupliateResourceForm,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {                
                location.href = 'resources?resourceFolder='+resourceFolder;
            }
        }
    });
}

//remove resource, if folder=true remove folder
function removeResource(name, folder){
    if(!folder && !confirm(messages["confirm_remove_resource"][locale])) return;
    if(folder && !confirm(messages["confirm_remove_resource_folder"][locale])) return;

    //create the data like this or there will be encoding problem.
    var data = new Object();
    data.resourcePath = resourceFolder + '/' +name;

    //remove via ajax
    $.ajax({
        url: 'removeResource',
        data: data,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                location.href = 'resources?resourceFolder='+resourceFolder;
            }
        }
    });
}

//crawl web page
function crawlWebPage(){
    var crawlForm =  $("#crawlForm").serializeObject();
    $.ajax({
        url: 'crawlWebPage',
        data: crawlForm,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                $('#crawlDialog').dialog('close');
                showStatusDialog(response.message);
            }
        }
    });
}

//------------------------------------------------------------------------------
//RESOURCES PAGE DIALOGS
//------------------------------------------------------------------------------

//duplicate resource dialog, calls duplicateResource
function duplicateResourceDialog(resourcePath, name){
    $("#duplicateResourceDialog").find("[name='resourcePath']").val(resourcePath);
    $("#duplicateResourceDialog").find("[name='newName']").val("copy-of-"+name);
    $('#duplicateResourceDialog').dialog('open');    
}

//------------------------------------------------------------------------------
//TEMPLATES ACTIONS
//------------------------------------------------------------------------------

//add template
function addTemplate(){    
    $.ajax({
        url: 'addTemplate',
        data: $("#addTemplateForm").serializeObject(),
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                location.href = 'templates';
            }
        }
    });
}

//remove template
function removeTemplate(templateId, goback){
    if(!confirm(messages["confirm_remove_template"][locale])) return;

    $.ajax({
        url: 'removeTemplate',
        data: 'templateId='+templateId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                //we're in edit template mode, go back to template list
                if (goback) location.href = 'templates';

                //hide the template row
                $("#template-"+templateId).css({
                    "background-color" : "#fbcdcd"
                }, 'fast').fadeOut("fast");
            }
        }
    });
}

//------------------------------------------------------------------------------
//EDIT TEMPLATE AJAX ACTIONS
//------------------------------------------------------------------------------

//view the current template
function viewTemplate(templatePath){
    window.open(contextPath + templatePath + "?static");
}

//rename template
function renameTemplate(){
    var renameTemplateForm = $("#renameTemplateForm").serializeObject();
    $.ajax({
        url: 'renameTemplate',
        data: renameTemplateForm,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                $("#templateName").text(renameTemplateForm.name);
                $('#renameTemplateDialog').dialog('close');
                showStatusDialog(response.message);
            }
        }
    });
}

//inspect element from the preview frame
function inspectElement(){
    $("#templatePreview")[0].contentWindow.Firebug.Inspector.toggleInspect();
}

//make attribute
function makeAttribute(){
    var attribute = $("#makeAttributeDialogAttribute").val();
    if(!/^[\w]{3,100}$/.test(attribute)){
        showErrorDialog("Attribute should not contain spaces or special characters");
        return;
    }

    //get the element id
    var elemId = getSelectedElementId();
    if (!elemId) return;

    //set the default value of the html
    $.ajax({
        url: 'getTemplateElementHtml',
        data: 'templateId='+template.id+'&elemId='+elemId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
            //fail silently
            } else {
                newAttributes[attribute] = response.object;
            }
        }
    });

    //update the html in server
    var data = new Object();
    data.templateId = template.id;
    data.elemId = elemId;
    data.html = ("${"+attribute+"}");

    $.ajax({
        url: 'updateTemplateElementHtml',
        data: data,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                //the new html fragment
                $(selectedElement).html(response.object);
                //update the tree
                $("#templatePreview")[0].contentWindow.Firebug.HTML.fmgUpdateTree();
                //close the dialog
                $('#makeAttributeDialog').dialog('close');
            }
        }
    });
}

//save template
function saveTemplate(publish){
    var templateForm = new Object();
    templateForm.templateId = template.id;
    templateForm.comment = $("#saveTemplateDialogComment").val();
    templateForm.publish = publish;
    templateForm.newAttributes = newAttributes;

    //save with ajax
    $.ajax({
        url: 'saveTemplate',
        data: templateForm,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                $('#saveTemplateDialog').dialog('close');

                //get result
                var result = response.object;
                var addedAttrs = result.addedAttributes;
                if (publish){
                    templateHtml = result.templateHtml;
                    template = result.template;
                }

                //add added attributes to message
                var message = response.message+"<br/>";
                message += "<ul>";
                for (var i=0; i<addedAttrs.length; i++){
                    message += "<li>"+addedAttrs[i]+"</li>"
                }
                message += "</ul>";

                //show message with saved attrs
                showStatusDialog(message);
            }
        }
    });
}

//revert template to selected history version
function revertTemplate(templateId, templateHistoryId, publish, silent){
    if(!silent && publish && !confirm(messages["confirm_revert_template"][locale])) return;
    if(!silent && !publish && !confirm(messages["confirm_revert_preview_template"][locale])) return;

    $.ajax({
        url: 'revertTemplate',
        data: 'templateId='+templateId+'&templateHistoryId='+templateHistoryId+"&publish="+publish,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                //show the status
                $('#templateHistoryDialog').dialog('close');
                showStatusDialog(response.message);

                //get result
                var result = response.object;
                if (publish){
                    templateHtml = result.templateHtml;
                    template = result.template;
                }

                //re-load the page preview iframe
                $("#templatePreview").remove();
                var templatePreviewSrc = contextPath+template.path+"?time="+(new Date()).getTime()+"&static&edit";
                $("#page-header-placeholder").after('<iframe id="templatePreview" src="'+templatePreviewSrc+'" width="100%" height="480" onLoad="onTemplateIFrameLoad(this.contentWindow.location.href, this.contentWindow.location.pathname)"></iframe>');
            }
        }
    });
}

//revert current changes
function revertTemplateChanges(){
    if(!confirm(messages["confirm_revert_changes_template"][locale])) return;
    revertTemplate(template.id, template.version, false, true);
}

//revert template to selected history version
function removeTemplateHistory(templateHistoryId){
    if(!confirm(messages["confirm_remove_template_history"][locale])) return;

    $.ajax({
        url: 'removeTemplateHistory',
        data: 'templateHistoryId='+templateHistoryId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                $("#history-"+templateHistoryId).css({
                    "background-color" : "#fbcdcd"
                }, 'fast').fadeOut("fast");
            }
        }
    });
}

//------------------------------------------------------------------------------
//EDIT TEMPLATE DIALOGS
//------------------------------------------------------------------------------

//rename dialog, calls renameTemplate
function renameTemplateDialog(){
    $('#renameTemplateDialog').dialog('open');
}

//edit template element html
function editTemplateHtmlDialog(){
    //get the element id
    var elemId = getSelectedElementId();
    if (!elemId) return;

    //show the dialog
    editor.setValue("Please wait...");
    $('#editTemplateHtmlDialog').dialog('open');

    //populate the textarea with the actual html of the selected element
    editTemplateHtmlDialogPopulate(elemId);
}

//populate the edit template html dialog with the actual html of the selected element
function editTemplateHtmlDialogPopulate(elemId){
    $.ajax({
        url: 'getTemplateElementHtml',
        data: 'templateId='+template.id+'&elemId='+elemId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                $('#editTemplateHtmlDialog').dialog('close');
                showErrorDialog(response.message);
            } else {
                editor.setValue(response.object);
                editor.gotoLine(0);
            }
        }
    });
}

//edit template html update button
function editTemplateHtmlDialogUpdate(){
    //get the element id
    var elemId = getSelectedElementId();
    if (!elemId) return;

    //update the html in server and regenerate children
    var data = new Object();
    data.templateId = template.id;
    data.elemId = elemId;
    data.html = editor.getValue();

    $.ajax({
        url: 'updateTemplateElementHtml',
        data: data,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                //the new html fragment
                $(selectedElement).html(response.object);
                //update the tree
                $("#templatePreview")[0].contentWindow.Firebug.HTML.fmgUpdateTree();
                //close the dialog
                $('#editTemplateHtmlDialog').dialog('close');
            }
        }
    });
}

//edit template element inline
function editTemplateInlineDialog(){
    alert("This feature is not implemented yet, sorry...");
}

//make template element attribute
function makeAttributeDialog(){
    if (!selectedElement){
        alert("Please select an element first.");
        return;
    }

    if (!getSelectedElementId()) return;

    //show the dialog
    $('#makeAttributeDialog').dialog('open');
}

//save template dialog, calls saveTemplate / reviewTemplateChanges
function saveTemplateDialog(){
    //show the dialog
    $('#saveTemplateDialog').dialog('open');
}

//review template changes, diff to published version
function reviewTemplateChangesDialog(){
    $.ajax({
        url: 'getTemplateHtml',
        data: 'original=false&templateId='+template.id,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                var currentTemplateHtml = response.object;
                if (templateHtml == currentTemplateHtml){
                    showStatusDialog("Nothing is changed!");
                    return;
                }

                //diff
                var api = new Object();
                api.source = templateHtml;
                api.diff = currentTemplateHtml;
                api.mode = "diff";
                api.diffview = "sidebyside";
                api.sourcelabel = "Original"
                api.difflabel = "Changed"
                var result = prettydiff(api);

                //open view changes dialog
                viewChangesDialog(result[0]);
            }
        }
    });
}

//template history, list previous versions, view changes
function templateHistoryDialog(){
    //populate the dialog with history records of the template
    $.ajax({
        url: 'getTemplateHistories',
        data: 'templateId='+template.id,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                templateHistory = response.object;
                $('#previousVersions > tbody').empty();
                for (var i = 0; i < templateHistory.length; i++) {
                    var current = (templateHistory[i].id==template.version);
                    var templateHistoryRow = current?"<tr id='history-"+templateHistory[i].id+"' style='background-color:#00ff00'>":"<tr id='history-"+templateHistory[i].id+"'>";
                    templateHistoryRow += "<td><input type='radio' name='attribute1' value='"+i+"' "+(current?"checked":"")+" /></td>";
                    templateHistoryRow += "<td><input type='radio' name='attribute2' value='"+i+"' "+(i==0?"checked":"")+" /></td>";
                    templateHistoryRow += "<td>"+templateHistory[i].author + "</td>";
                    templateHistoryRow += "<td>"+templateHistory[i].comment + "</td>";
                    templateHistoryRow += "<td>"+templateHistory[i].date + "</td>";
                    templateHistoryRow += "<td><a class='btn' href='javascript:revertTemplate("+template.id+","+templateHistory[i].id+",false)'>Revert</a></td>";
                    templateHistoryRow += "<td><a class='btn' href='javascript:revertTemplate("+template.id+","+templateHistory[i].id+",true)'>Publish</a></td>";
                    templateHistoryRow += current?"<td></td>":"<td><a class='btn' href='javascript:removeTemplateHistory("+templateHistory[i].id+")'>Remove</a></td>";
                    templateHistoryRow += "</tr>";
                    $('#previousVersions > tbody').append(templateHistoryRow);
                }
            }
        }
    });

    //open the dialog
    $('#templateHistoryDialog').dialog('open');
}

//view changes between template histories
function templateHistoryDialogViewChanges(){
    var attr1 = $("#templateHistoryDialog").find('input:radio[name=attribute1]:checked').val();
    var attr2 = $("#templateHistoryDialog").find('input:radio[name=attribute2]:checked').val();

    if (!attr1 || !attr2){
        alert("Please choose both templates to be compared.");
        return;
    }
    if (attr1 == attr2){
        alert("You are trying to compare the template with itself. Please select two different templates to compare.");
        return;
    }

    //diff
    var api = new Object();
    api.source = templateHistory[attr1].html;
    api.diff = templateHistory[attr2].html;
    api.mode = "diff";
    api.diffview = "sidebyside";
    api.sourcelabel = templateHistory[attr1].date;
    api.difflabel = templateHistory[attr2].date;
    var result = prettydiff(api);

    //open view changes dialog
    viewChangesDialog(result[0]);
}

//------------------------------------------------------------------------------
//EDIT TEMPLATE EVENTS
//------------------------------------------------------------------------------

//called when an html element is selected via inspect or tree
function onElementSelected(elem){
    selectedElement = elem;
    if (selectedElement.tagName == 'HTML') return;
    $('#actionDialog').dialog('open');
}

//called when inspecting starts
function onStartInspecting(){
    $("#inspectButton").text("Inspecting...");
}

//called when inspecting stops
function onStopInspecting(){
    $("#inspectButton").text("Inspect Element");
}

//called when preview iframe is loaded
function onTemplateIFrameLoad(url, path){
    //initial opening, no src
    if (url == 'about:blank') return true;

    //prevent links
    $("#templatePreview").contents().find("a").click(function(event) {
        if ($(event.currentTarget).parents().hasClass('aloha-floatingmenu')) return true; //do not meddle aloha
        if (!confirm("Any unsaved changes will be lost if you navigate away from this page, are you sure?")) event.preventDefault();
    });

    //a template is opened, adjust the host page
    if (url.substring(url.length-5, url.length)=="&edit"){
        var iframeWidth = $("#templatePreview").contents().find("body")[0].scrollWidth;
        var iframeHeight = $("#templatePreview").contents().find("body")[0].scrollHeight;

        //adjust page elems width when the document is ready
        setTimeout(function() {
            if (iframeWidth>940){
                $(".content").width(iframeWidth);
                $(".container").width(iframeWidth);
                $(".page-header").width(iframeWidth);
            }
            $("#page-header-placeholder").height($(".page-header").outerHeight());

            //resize iframe to actual content
            $("#templatePreview").width(iframeWidth);
            $("#templatePreview").height(iframeHeight+100);
        }, 100);

        return true;
    }

    //a link is clicked within the iframe, reopen the template of this page in edit mode
    location.href = "editTemplate?id=0&ofPage=" + (path.substring(contextPath.length, path.length));
    return false;
}

//------------------------------------------------------------------------------
//EDIT TEMPLATE UTILITY
//------------------------------------------------------------------------------

//get the id of the selected element
function getSelectedElementId(){
    if (!selectedElement){
        alert("Please select an element first.");
        return null;
    }

    //get the element id
    var elemId = $(selectedElement).attr("fmgcms-id");
    var elemChanged = !elemId;

    //if this node is script generated, it won't have element id, find the nearest parent element which has it
    while(!elemId){
        selectedElement = selectedElement.parentNode;
        if (!selectedElement){
            alert("You cannot modify this element!");
            return null; //this shouldn't happen
        }
        elemId = $(selectedElement).attr("fmgcms-id");
    }

    //select the current element on tree if it is changed
    if (elemChanged) $("#templatePreview")[0].contentWindow.Firebug.HTML.select(selectedElement)

    return elemId;
}

//------------------------------------------------------------------------------
//ACCOUNT AJAX ACTIONS
//------------------------------------------------------------------------------

//save the account
function saveAccount(){

}

//remove the account
function removeAccount(){
    if(!confirm(messages["confirm_remove_account"][locale])) return;

    $.ajax({
        url: 'removeAccount',
        data: 'confirmationCode=SWUvtwB7C2ApEr4EpD0d',
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                location.href = 'http://pragmacms.com/admin/signup';
            }
        }
    });
}

//save the site
function saveSite(){
    $.ajax({
        url: 'saveSite',
        data: $("#editSiteForm").serializeObject(),
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                location.href = 'account';
            }
        }
    });
}

//remove the site
function removeSite(siteId){
    if(!confirm(messages["confirm_remove_site"][locale])) return;

    $.ajax({
        url: 'removeSite',
        data: 'confirmationCode=nROqS9RUPH7Yh9WXdN8P&siteId='+siteId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                location.href = 'account';
            }
        }
    });
}

//switch to the site, but before check if already logged in
function switchSite(siteId, domains){
    var primaryDomain = domains.split(/\s+/)[0];    
    $.ajax({
        url: 'http://'+primaryDomain+'/admin/checkLogin',
        data: 'check',
        dataType: 'jsonp',     
        jsonpCallback: 'jsonCallback',
        type: 'GET',
        success: function(response) {                        
            if (response.status != "0") {                                          
                switchSiteFinal(siteId);
            } else {
                location.href = 'http://'+primaryDomain+'/admin/pages';
            }
        },
        error: function(err){            
            switchSiteFinal(siteId);
        }
    });    
}

//switch to the site
function switchSiteFinal(siteId){    
    $.ajax({
        url: 'switchSite',
        data: 'siteId='+siteId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                location.href = response.object;                
            }
        }
    });
}

//save the author
function saveAuthor(){
    $.ajax({
        url: 'saveAuthor',
        data: $("#editAuthorForm").serializeObject(),
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                location.href = 'account';
            }
        }
    });
}

//remove the author
function removeAuthor(authorId){
    if(!confirm(messages["confirm_remove_author"][locale])) return;

    $.ajax({
        url: 'removeAuthor',
        data: 'authorId='+authorId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                location.href = 'account';
            }
        }
    });
}

//------------------------------------------------------------------------------
//ACCOUNT DIALOGS
//------------------------------------------------------------------------------

//edit author dialog
function editAccountDialog(){
    showErrorDialog("This feature is not implemented yet, sorry.. Please contact us if you need this feature.");
}

//edit site dialog
function editSiteDialog(siteId){
    //clear the form first
    $("#editSiteForm").find("input").val("");
    $("#editSiteForm").find("textarea").val("");

    //if editing fill existing data
    if (siteId) editSiteDialogPopulate(siteId);

    //show the dialog
    $("#editSiteDialog").dialog('open');
}

//populate the edit site form
function editSiteDialogPopulate(siteId){
    $.ajax({
        url: 'getSite.htm',
        data: "siteId="+siteId,
        dataType: 'json',
        cache: false,
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
                $("#editSiteDialog").dialog('close');
            } else {
                var site = response.object;

                //update form
                $.each(site, function(key, value) {
                    $("#editSiteForm").find("[name="+key+"]").val(value);
                });
            }
        }
    });
}

//edit author dialog
function editAuthorDialog(authorId){
    //clear the form first
    $("#editAuthorForm").find("input").val("");

    //if editing fill existing data
    if (authorId) editAuthorDialogPopulate(authorId);

    //show the dialog
    $("#editAuthorDialog").dialog('open');
}

//populate the edit author form
function editAuthorDialogPopulate(authorId){
    $.ajax({
        url: 'getAuthor.htm',
        data: "authorId="+authorId,
        dataType: 'json',
        cache: false,
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
                $("#editAuthorDialog").dialog('close');
            } else {
                var author = response.object;

                //update form
                $.each(author, function(key, value) {
                    if (key=="password") return;
                    $("#editAuthorForm").find("[name="+key+"]").val(value);
                });
            }
        }
    });
}

//edit profile dialog
function editProfileDialog(){
    showErrorDialog("This feature is not implemented yet, sorry.. Please contact us if you need this feature.");
}

//------------------------------------------------------------------------------
//COMMON UTILITY
//------------------------------------------------------------------------------

//fill a target with item values
function fillRecursively(targetPrefix, item){
    $.each(item, function(key, value) {
        if(typeof(value) == 'object') return  fillRecursively(targetPrefix+'-'+key, value);
        //alert (targetPrefix+key+":"+value);
        var target = $(targetPrefix+'-'+key);
        if (target.length) target.text(value); //fill if exists
    });
}

//error dialog
function showErrorDialog(message){
    var errorDialog = $('<div></div>')
    .dialog({
        autoOpen: false,
        title: messages["error"][locale],
        modal: true

    });
    errorDialog.html("<p><strong>"+message+"</strong></p>");
    errorDialog.dialog("option", "buttons", [{
        'class': 'btn',
        text: messages["ok"][locale],
        click: function() {
            $(this).dialog("close");
        }
    }]);
    errorDialog.dialog('open');
}

//status dialog
function showStatusDialog(message){
    var statusDialog = $('<div></div>')
    .dialog({
        autoOpen: false,
        title: messages["status"][locale],
        modal: true

    });
    statusDialog.html("<p><center><strong>"+message+"</strong></center></p>");
    statusDialog.dialog("option", "buttons", [{
        'class': 'btn',
        text: messages["ok"][locale],
        click: function() {
            $(this).dialog("close");
        }
    }]);
    statusDialog.dialog('open');
}

//serialize jquery objects to json
$.fn.serializeObject = function()
{
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};

//get param from query string
function getParameterByName(name){
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.search);
    if(results == null)
        return "";
    else
        return decodeURIComponent(results[1].replace(/\+/g, " "));
}

//------------------------------------------------------------------------------
//118N MESSAGES
//------------------------------------------------------------------------------
var messages = {
    "error": {
        en: "Error",
        tr: "Hata"
    },
    "status": {
        en: "Status",
        tr: "Durum"
    },
    "upload": {
        en: "Upload",
        tr: "Yükle"
    },
    "cancel": {
        en: "Cancel",
        tr: "İptal"
    },
    "ok": {
        en: "OK",
        tr: "Tamam"
    },
    "save": {
        en: "Save",
        tr: "Kaydet"
    },
    "remove": {
        en: "Remove",
        tr: "Kaldır"
    },
    "select": {
        en: "Select",
        tr: "Seç"
    },
    "apply": {
        en: "Apply",
        tr: "Uygula"
    },
    "update": {
        en: "Update",
        tr: "Güncelle"
    },
    "button_save_and_publish": {
        en: "Save and Publish",
        tr: "Kaydet ve Yayınla"
    },
    "button_save_as_draft": {
        en: "Save as Draft",
        tr: "Taslak Olarak Kaydet"
    },
    "button_review_changes": {
        en: "Review Changes",
        tr: "Değişiklikleri Gözden Geçir"
    },
    "button_view_changes": {
        en: "View Changes",
        tr: "Değişiklikleri Göster"
    },
    "button_edit_html": {
        en: "Edit HTML",
        tr: "HTML Düzenle"
    },
    "button_edit_inline": {
        en: "Edit Inline",
        tr: "Anlık Düzenle"
    },
    "button_make_attribute": {
        en: "Make Attribute",
        tr: "Öğe Yap"
    },
    "confirm_remove_page": {
        en: "This page will be completely removed from the system. This action is permanent. Are you sure?",
        tr: "Bu sayfa sistemden tamamen silinecek. Bu işlem geri alınamaz. Emin misiniz?"
    },
    "confirm_remove_page_attachment": {
        en: "This attachment will be completely removed from the system. This action is permanent. Are you sure?",
        tr: "Bu dosya sistemden tamamen silinecek. Bu işlem geri alınamaz. Emin misiniz?"
    },
    "confirm_revert_page_attribute": {
        en: "This attribute will be reverted to selected version, and this version will be published. Are you sure?",
        tr: "Bu öğe seçili versiyona geri döndürülecek ve bu versiyon yayınlanacak. Emin misiniz?"
    },
    "confirm_remove_page_attribute_history": {
        en: "This attribute history record will be removed from the system. This action is permanent. Are you sure?",
        tr: "Bu öğe geçmişi kaydı sistemden kaldırılacak. Bu işlem geri alınamaz. Emin misiniz?"
    },
    "confirm_remove_resource": {
        en: "This resource will be completely removed from the system. Please make sure that it is not referenced from any template. This action is permanent. Are you sure?",
        tr: "Bu kaynak sistemden tamamen silinecek. Lütfen önce herhangi bir şablondan kullanılmadığından emin olun. Bu işlem geri alınamaz. Emin misiniz?"
    },
    "confirm_remove_resource_folder": {
        en: "This resource folder will be completely removed from the system. This action is permanent. Are you sure?",
        tr: "Bu kaynak dizini sistemden tamamen silinecek. Bu işlem geri alınamaz. Emin misiniz?"
    },
    "confirm_remove_template": {
        en: "Warning! This template will be completely removed from the system with all the pages using it. This action is permanent. Are you sure?",
        tr: "Uyarı! Bu şablon tüm sayfalarıyla birlikte sistemden tamamen silinecek. Bu işlem geri alınamaz. Emin misiniz?"
    },
    "confirm_remove_template_history": {
        en: "This template history record will be removed from the system. This action is permanent. Are you sure?",
        tr: "Bu şablon geçmişi kaydı sistemden kaldırılacak. Bu işlem geri alınamaz. Emin misiniz?"
    },
    "confirm_revert_template": {
        en: "This template will be reverted to selected version, and this version will be published. Are you sure?",
        tr: "Bu şablon seçili versiyona geri döndürülecek ve bu versiyon yayınlanacak. Emin misiniz?"
    },
    "confirm_revert_preview_template": {
        en: "This template will be reverted to selected version, but this version will not be published (preview). Your current changes will be lost. Are you sure?",
        tr: "Bu şablon seçili versiyona geri döndürülecek ama bu versiyon yayınlanmayacak (önizleme). Mevcut değişiklikleriniz kaybolacak. Emin misiniz?"
    },
    "confirm_revert_changes_template": {
        en: "This template will be reverted to last published version. Your current changes will be lost. Are you sure?",
        tr: "Bu şablon son yayınlanmış versiyona geri döndürülecek. Mevcut değişiklikleriniz kaybolacak. Emin misiniz?"
    },
    "confirm_remove_site": {
        en: "WARNING!!! This site will be completely removed from the system with all of its pages, templates and resources. This action is permanent. Are you sure? Please think twice...",
        tr: "UYARI!!! Bu site tüm sayfaları, şablonları ve kaynakları ile birlikte sistemden tamamen silinecek. Bu işlem geri alınamaz. Emin misiniz? Lütfen tekrar düşünün..."
    },
    "confirm_remove_author": {
        en: "This author will be completely removed from the account. This action is permanent. Are you sure?",
        tr: "Bu yazar hesaptan silinecek. Bu işlem geri alınamaz. Emin misiniz?"
    },
    "confirm_remove_account": {
        en: "WARNING!!! This account will be completely removed from the system with all of its its sites, authors, pages, templates and resources. This action is permanent. Are you sure? Instead you can remove individual sites. Please think twice...",
        tr: "UYARI!!! Bu hesap tüm siteleri, yazarları, sayfaları, şablonları ve kaynakları ile birlikte sistemden tamamen silinecek. Bu işlem geri alınamaz. Emin misiniz? Bunun yerine siteleri de silebilirsiniz. Lütfen tekrar düşünün..."
    },
    "error_template_not_selected": {
        en: "Template is not selected. Please select a template.",
        tr: "Şablon seçilmedi. Lütfen bir şablon seçin."
    }
};