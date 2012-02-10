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
