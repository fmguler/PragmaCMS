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
            if (response.status != "0") {
                alert(statusResponse.message);
            } else {
                
        }
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
