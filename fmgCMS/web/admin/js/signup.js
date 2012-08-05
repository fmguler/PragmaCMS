/*
 *  fmgCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */

//signup
function signup(){
    $.ajax({
        url: 'doSignup',
        data: $("#signupForm").serializeObject(),
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

//error dialog
function showErrorDialog(message){
    var errorDialog = $('<div></div>')
    .dialog({
        autoOpen: false,
        title: "Something is wrong",
        modal: true,
        width: 400
    });
    errorDialog.html("<p><strong>"+message+"</strong></p>");
    errorDialog.dialog("option", "buttons", [{
        'class': 'btn',
        text: "OK",
        click: function() {
            $(this).dialog("close");
        }
    }]);
    errorDialog.dialog('open');
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
