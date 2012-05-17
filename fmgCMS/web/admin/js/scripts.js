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
    //upload attachment (modal)
    $("#uploadAttachmentDialog").dialog({
        //height: 'auto',
        width: 370,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-primary',
            text: messages["upload"][locale],
            click: uploadAttachment
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
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
            click: reviewChanges
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //revert (modal)
    $("#revertDialog").dialog({
        height: 500,
        width: 670,
        autoOpen: false,
        modal: true,
        buttons: [{
            'class': 'btn btn-danger',
            text: messages["button_revert_selected"][locale],
            click: revertPageAttribute
        },{
            'class': 'btn',
            text: messages["cancel"][locale],
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

//upload attachment
function uploadAttachment(){
    $("#uploadAttachmentForm").submit();
}

//save page properties
function renamePage(){
    $.ajax({
        url: 'renamePage',
        data: $("#renamePageForm").serializeObject(),
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                //TODO: shall we reload page? all paths should be updated
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
                page = result.page;
                var savedAttrs = result.savedAttributes;

                //add saved attributes to message
                var message = response.message+"<br/>";

                message += "<ul>";
                for (var i=0; i<savedAttrs.length; i++){
                    message += "<li>"+savedAttrs[i]+"</li>"
                }
                message += "</ul>";

                showStatusDialog(message);
            }
        }
    });
}

//revert attribute to selected history version
function revertPageAttribute(){
    var revertForm = $("#revertForm").serializeObject();
    $.ajax({
        url: 'revertPageAttribute',
        data: revertForm,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            if (response.status != "0") {
                showErrorDialog(response.message);
            } else {
                $('#revertDialog').dialog('close');
                showStatusDialog(response.message);

                //update the attribute state accordingly
                var attribute = attributeById(selectedAttributeId);
                attribute.value = attributeHistoryById(revertForm.attributeHistoryId).value;
                attribute.version = revertForm.attributeHistoryId;

                //update editable html
                var editable = $("#pagePreview").contents().find("#attribute-editable-"+attribute.attribute);
                editable.html(attribute.value);
            }
        }
    });
}

//edit page dialogs-------------------------------------------------------------

//rename dialog, calls renamePage
function renamePageDialog(){
    $('#renamePageDialog').dialog('open');
}

//upload dialog, calls uploadAttachment
function uploadAttachmentDialog(){
    $('#uploadAttachmentDialog').dialog('open')
}

//save dialog, calls publish/draft/review
function saveDialog(){
    $('#saveDialog').dialog('open');
}

//review changes, diff to published version
function reviewChanges(){
    alert("review");
}

//page all attributes history
function historyDialog(){
    alert("not implemented yet");
}

//revert dialog, previews attribute html to prev versions, or calls revertPageAttribute
function revertDialog(){
    if (!selectedAttributeId){
        alert("Please select an attribute first.");
        return;
    }
    //populate the dialog with history records of the current attribute
    revertDialogPopulate(attributeById(selectedAttributeId));

    //open the dialog
    $('#revertDialog').dialog('open');
}

//populates the revert dialog with the selected attribute history
function revertDialogPopulate(attribute){
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
                $('#revertDialogAttributeId').val(attribute.id);
                $('#previousVersions > tbody').empty();
                for (var i = 0; i < selectedAttributeHistory.length; i++) {
                    var current = (selectedAttributeHistory[i].id==attribute.version);
                    var attributeHistoryRow = current?"<tr style='background-color:#00ff00'>":"<tr>";
                    attributeHistoryRow += "<td><input type='radio' name='attributeHistoryId' value='"+selectedAttributeHistory[i].id+"' "+(current?"checked":"")+" /></td>";
                    attributeHistoryRow += "<td>"+selectedAttributeHistory[i].author + "</td>";
                    attributeHistoryRow += "<td>"+selectedAttributeHistory[i].comment + "</td>";
                    attributeHistoryRow += "<td>"+selectedAttributeHistory[i].date + "</td>";
                    attributeHistoryRow += "<td><a class='btn' href='javascript:revertAttributePreview("+i+")'>Preview</a></td>";
                    attributeHistoryRow += "</tr>";
                    $('#previousVersions > tbody').append(attributeHistoryRow);
                }
            }
        }
    });
}

//revert attribute to specified version - preview
function revertAttributePreview(version){
    //update the attribute value
    var attribute = attributeById(selectedAttributeId);
    attribute.value = selectedAttributeHistory[version].value;

    //update editable html
    var editable = $("#pagePreview").contents().find("#attribute-editable-"+attribute.attribute);
    editable.html(attribute.value);
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
    "button_revert_selected": {
        en: "Revert To Selected Version",
        tr: "Seçili Versiyona Geri Dön"
    },
    "confirm_remove_page": {
        en: "This page will be completely removed from the system. This action is permanent. Are you sure?",
        tr: "Bu sayfa sistemden tamamen silinecek. Bu işlem geri alınamaz. Emin misiniz?"
    }
};