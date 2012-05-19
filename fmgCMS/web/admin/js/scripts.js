/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */

//page inits--------------------------------------------------------------------

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
        post : function(){
            if ($("#uploadAttachmentDialogFile").val() == ""){
                alert("Please select a file");
                return false;
            }

            //fake progress bar :)
            $("#uploadAttachmentDialogFile").fadeOut('fast', function(){
                $("#uploadAttachmentDialogProgress").fadeIn('fast');
                setTimeout("uploadProgress(0)", 100);
            });

            return true;
        },
        complete : function (responseStr){
            var response = $.parseJSON(responseStr.substring(responseStr.indexOf('{'), responseStr.indexOf('</pre>')));
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                pageAttachments = response.object;
                $('#uploadAttachmentDialog').dialog('close');
                showStatusDialog(response.message);
            }
        }
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
        width: 570,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn',
            text: messages["apply"][locale],
            click: function() {}
        },{
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
        width: 700,
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
}

//list pages actions------------------------------------------------------------

//add page
function addPage(){
    $.ajax({
        url: 'addPage',
        data: $("#addPageForm").serializeObject(),
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

//edit page ajax actions--------------------------------------------------------

//view the current page
function viewPage(){
    window.open(contextPath + page.path);
}

//save page properties
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

//edit page dialogs-------------------------------------------------------------

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
    $('#saveDialog').dialog('open');
}

//review changes, diff to published version
function reviewChangesDialog(){
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
    api.source = pageCopy.pageAttributes[elemIndex].value;
    api.diff = page.pageAttributes[elemIndex].value;
    api.mode = "diff";
    api.diffview = "inline";
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
                    var attributeHistoryRow = current?"<tr style='background-color:#00ff00'>":"<tr>";
                    attributeHistoryRow += "<td><input type='radio' name='attribute1' value='"+i+"' "+(current?"checked":"")+" /></td>";
                    attributeHistoryRow += "<td><input type='radio' name='attribute2' value='"+i+"' "+(i==0?"checked":"")+" /></td>";
                    attributeHistoryRow += "<td>"+selectedAttributeHistory[i].author + "</td>";
                    attributeHistoryRow += "<td>"+selectedAttributeHistory[i].comment + "</td>";
                    attributeHistoryRow += "<td>"+selectedAttributeHistory[i].date + "</td>";
                    attributeHistoryRow += "<td><a class='btn' href='javascript:attributeHistoryDialogRevert("+i+")'>Revert</a></td>";
                    attributeHistoryRow += "<td><a class='btn' href='javascript:revertPageAttribute("+attribute.id+","+selectedAttributeHistory[i].id+")'>Publish</a></td>";
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
    api.source = selectedAttributeHistory[attr1].value;
    api.diff = selectedAttributeHistory[attr2].value;
    api.mode = "diff";
    api.diffview = "inline";
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
    $('#viewChangesDialog').dialog('option', "width", 800);
    $('#viewChangesDialog').dialog('open');


    //adjust width the table is smaller
    if($("#viewChangesDialog").find("table").width() < 750){
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

    //update the textarea
    $("#editHtmlDialogTextarea").val(attributeById(selectedAttributeId).value);

    //show the dialog
    $('#editHtmlDialog').dialog('open');
}

//edit page events--------------------------------------------------------------

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

//on attribute textarea value change, update preview
function onEditHtmlDialogTextareaChange(){
    if (!selectedAttributeId) return;

    //update the attribute value
    var attribute = attributeById(selectedAttributeId);
    attribute.value = $("#editHtmlDialogTextarea").val();

    //update editable html
    var editable = $("#pagePreview").contents().find("#attribute-editable-"+attribute.attribute);
    editable.html(attribute.value);
    editable.focus();
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
        if (!confirm("Any unchanged changes will be lost if you navigate away from this page, are you sure?")) event.preventDefault();
    });

    //an edit mode page is opened, adjust the host page
    if (url.substring(url.length-5, url.length)=="&edit"){
        var iframeWidth = $("#pagePreview").contents().find("body")[0].scrollWidth;
        var iframeHeight = $("#pagePreview").contents().find("body")[0].scrollHeight;

        //adjust page elems width
        $(".content").width(iframeWidth);
        $(".container").width(iframeWidth);
        $(".page-header").width(iframeWidth);

        //resize iframe to actual content
        $("#pagePreview").width(iframeWidth);
        $("#pagePreview").height(iframeHeight+30);

        return true;
    }

    //a link is clicked within the iframe, reopen this page in edit mode
    location.href = "editPage?path=" + (path.substring(contextPath.length, path.length));
    return false;
}

//edit page utility-------------------------------------------------------------

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
function uploadProgress(percent){
    if (!$("#uploadAttachmentDialogProgress").is(":visible")) return;
    $("#uploadAttachmentDialogProgress").find("div").css("width", percent+"%");
    var newPercent = percent+1;
    if (newPercent>=100) return;
    progressTimer = setTimeout("uploadProgress("+newPercent+")", 100);
}

//utility-----------------------------------------------------------------------

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

//I18N messages-----------------------------------------------------------------
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
    }
};