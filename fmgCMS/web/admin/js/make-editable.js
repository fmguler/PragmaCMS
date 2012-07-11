/*
 *  fmgCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */

var Aloha = window.Aloha || ( window.Aloha = {} );

Aloha.settings = {
    locale: 'en',
    plugins: {
        format: {
            config : [ 'b', 'i','u','del','sub','sup', 'p', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'pre'],
            editables : {
                heading1 	: [ 'b', 'i', 'u', 'del', 'sub', 'sup'  ],
                heading2 	: [ 'b', 'i', 'u', 'del', 'sub', 'sup'  ],
                heading3 	: [ 'b', 'i', 'u', 'del', 'sub', 'sup'  ]
            }
        }
    },
    sidebar: {
        disabled: true
    }
};

// Make #content editable once Aloha is loaded and ready.
Aloha.ready(function() {
    Aloha.jQuery('.editable').aloha();
    Aloha.bind("aloha-smart-content-changed", function(e) {
        var id = e.currentTarget.getActiveEditable().obj.context.id;
        var attribute = id.substring(19);
        var html = Aloha.getEditableById(id).getContents();
        window.parent.onAlohaChange(attribute, html);
    });
});

//when the user tries to scroll by mouse wheel (since the iframe is auto sized it won't work)
Aloha.jQuery(function(){
    Aloha.jQuery('body').bind('mousewheel', function(event) {
        window.parent.onIFrameScroll(event);
        return false;
    });
});

//force aloha to trigger content change event (to send latest html)
function forceAlohaChange(){
    //this triggers aloha-smart-content-changed event
    if (Aloha.activeEditable) Aloha.activeEditable.blur();
}
