/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */

//on edit page ready
function editPageReady(){
    //upload attachment (modal)
    $("#uploadAttachmentDialog").dialog({
        //height: 'auto',
        width: 400,
        autoOpen: false,
        modal: true,
        buttons: [{
            text: messages["upload"][locale],
            click: uploadAttachment
        },{
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //edit path (modal)
    $("#editPathDialog").dialog({
        //height: 'auto',
        width: 400,
        autoOpen: false,
        modal: true,
        buttons: [{
            text: messages["save"][locale],
            click: savePage
        },{
            text: messages["cancel"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });

    //edit html (modal)
    $("#editHtmlDialog").dialog({
        //height: 'auto',
        width: 600,
        autoOpen: false,
        modal: true,
        buttons: [{
            text: messages["ok"][locale],
            click: function() {
                $(this).dialog("close");
            }
        }]
    });
}

//save page properties
function savePage(){
    var page =$("#pageForm").serializeObject() ;
    $.ajax({
        url: 'savePage',
        data: page,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            location.href = 'editPage?path='+page.path;
        }
    });
}

//save the selected page attribute
function savePageAttribute(){
    var attributeId = $("#selectedAttributeId").val();
    if (attributeId=='') return;

    var attribute = new Object();
    attribute.id = attributeId;
    attribute.value= $("#attribute-"+attributeId).val();

    $.ajax({
        url: 'savePageAttribute',
        data: attribute,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            location.reload();
        }
    });
}

//remove the selected page attribute
function removePageAttribute(){
    var attributeId = $("#selectedAttributeId").val();
    if (attributeId=='') return;

    if(!confirm('You will delete this version of the attribute, are you sure?')) return;

    $.ajax({
        url: 'removePageAttribute',
        data: 'id='+attributeId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            location.reload();
        }
    });
}

//display selected attribute textarea
function onSelectedAttributeChange(){
    var attributeId = $("#selectedAttributeId").val();
    if (attributeId=='') return;

    //show the current attribute textarea
    $(".attribute").hide();
    $("#attribute-" + attributeId).show();

    //focus to the selected editable
    var editable = $("#pagePreview").contents().find("#attribute-editable-"+$("#id-to-attribute-"+attributeId).val());
    editable.focus();
}

//on attribute textarea value change, update preview
function onAttributeChange(attribute, id){
    var editable = $("#pagePreview").contents().find("#attribute-editable-"+attribute);
    editable.html($("#attribute-"+id).val());
    editable.focus();
}

//aloha edited content is changed, update texts
function onAlohaChange(attribute, html){
    var attributeId = $("#attribute-to-id-"+attribute).val();
    $("#attribute-"+attributeId).val(html);
}

//called when an editable is clicked
function onAlohaClick(attribute){
    var attributeId = $("#attribute-to-id-"+attribute).val();

    //show text area
    $(".attribute").hide();
    $("#attribute-"+attributeId).show();

    //make attribute selected
    $("#selectedAttributeId").find("option:selected").removeAttr("selected");
    $("#selectedAttributeId").find("option[value='"+attributeId+"']").attr("selected", "selected");
}

//called when user scrolls by mouse wheel on iframe
function onIFrameScroll(event){
    $("body").scrollTop($("body").scrollTop() - event.originalEvent.wheelDelta);
}

var contextPath = "";
function setContextPath(path){
    contextPath = path;
}

//called when user clicks a link in the preview iframe
function onNavigateAway(url, path){
    if (url.substring(url.length-5, url.length)=="?edit"){
        //auto size iframe
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
    //a link is clicked within iframe, reopen this page in edit mode
    location.href = "editPage?path=" + (path.substring(contextPath.length, path.length));
    return false;
}

//on page form submit
function pageFormSubmit(){
    //NOT USED
    var pageFormJson = $("#pageForm").serializeObject();
    alert(JSON.stringify(pageFormJson));
}

//upload attachment
function uploadAttachment(){
    $("#uploadAttachmentForm").submit();
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

//I18N messages-----------------------------------------------------------------
var messages = {
    "error": {en: "Error", tr: "Hata"},
    "status": {en: "Status", tr: "Durum"},
    "upload": {en: "Upload", tr: "Yükle"},
    "cancel": {en: "Cancel", tr: "İptal"},
    "ok": {en: "OK", tr: "Tamam"},
    "save": {en: "Save", tr: "Kaydet"},
    "remove": {en: "Remove", tr: "Kaldır"},
    "select": {en: "Select", tr: "Seç"}
};