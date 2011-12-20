/* 
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */

//on edit page ready
function editPageReady(){
}

var contextPath = "";
//set the context path (since the urls are absolute)
function setContextPath(path){
    contextPath = path;
}

//save page properties
function savePage(){
    var page =$("#pageForm").serializeObject() ;
    $.ajax({
        url: contextPath + '/admin/savePage',
        data: page,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            location.href = contextPath + page.path + '/edit';
        }
    });
}

//add an attribute to a page
function addPageAttribute(pageId){
    $.ajax({
        url: contextPath + '/admin/addPageAttribute',
        data: 'pageId='+pageId+'&attributeName='+$("#new-page-attribute").val(),
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            location.reload();
        }
    });
}

//add an attribute to a template
function addTemplateAttribute(templateId){
    $.ajax({
        url: contextPath + '/admin/addTemplateAttribute',
        data: 'templateId='+templateId+'&attributeName='+$("#new-template-attribute").val(),
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            location.reload();
        }
    });
}

//update page attributes
function updateAttribute(attributeId){
    var attribute = new Object();
    attribute.id = attributeId;
    attribute.value= $("#attribute-"+attributeId).val();
    
    $.ajax({
        url: contextPath + '/admin/updateAttribute',
        data: attribute,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            
        }
    });
}

//remove an attribute
function removeAttribute(attributeId){
    $.ajax({
        url: contextPath + '/admin/removeAttribute',
        data: 'id='+attributeId,
        dataType: 'json',
        type: 'POST',
        success: function(response) {
            location.reload();
        }
    });
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
