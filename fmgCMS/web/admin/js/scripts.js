/* 
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */

//on edit page ready
function editPageReady(){
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

var contextPath = "";
function setContextPath(path){
    contextPath = path;
}

//called when user clicks a link in the preview iframe
function onNavigateAway(url, path){    
    if (url.substring(url.length-5, url.length)=="?edit") return true;        
    location.href = "editPage?path=" + (path.substring(contextPath.length, path.length));
    return false;
}

//on page form submit
function pageFormSubmit(){
    //NOT USED
    var pageFormJson = $("#pageForm").serializeObject();
    alert(JSON.stringify(pageFormJson));
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
