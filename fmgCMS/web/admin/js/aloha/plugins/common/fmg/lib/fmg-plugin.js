define([
    'aloha/jquery',
    'aloha/plugin',
    'aloha/floatingmenu',
    'css!fmg/css/image.css',
    'css!link/css/link.css'
    ],

    function(aQuery, Plugin, FloatingMenu) {
        var Aloha = window.Aloha;
        var GENTICS = window.GENTICS;
        var jQuery = aQuery;

        //set global instance, to access from dialog
        window.fmgPlugin =  Plugin.create( 'fmg', {
            imageObj: null,

            init: function() {
                // Executed on plugin initialization
                var tabFmg = "Resim";
                FloatingMenu.createScope(this.name, 'Aloha.continuoustext');
                this.addUIInsertButton(tabFmg);
                this.addUISrcButtons(tabFmg);
                this.subscribeEvents();
            },

            subscribeEvents: function(){
                //to use reference to plugin
                var that = this;

                //triger clickImage when an img tag inside container is clicked
                Aloha.bind('aloha-editable-created', function( event, editable) {
                    try {
                    // this will disable mozillas image resizing facilities
                    //document.execCommand( 'enableObjectResizing', false, false);
                    } catch (e) {
                        Aloha.Log.error( e, 'Could not disable enableObjectResizing');
                    // this is just for others, who will not support disabling enableObjectResizing
                    }

                    // Inital click on images will be handled here
                    editable.obj.delegate( 'img', 'mouseup', function (event) {
                        that.clickImage(event);
                    //event.stopPropagation();
                    });
                });

                //hide plugin tab if an img is unselected
                Aloha.bind('aloha-selection-changed', function(event, rangeObject, originalEvent) {
                    var foundMarkup;

                    if (Aloha.activeEditable !== null) {
                        foundMarkup = that.findImgMarkup( rangeObject );

                        // Enable image specific ui components if the element is an image
                        if (foundMarkup) {
                            that.insertImgButton.hide();
                            FloatingMenu.setScope(that.name);
                            that.imgSrcField.setTargetObject(foundMarkup, 'src');
                            FloatingMenu.activateTabOfButton('imgsrc');
                        } else {
                            that.insertImgButton.show();
                            that.imgSrcField.setTargetObject(null);
                        }

                        // TODO this should not be necessary here!
                        FloatingMenu.doLayout();
                    }

                });

                //handle typing src
                this.imgSrcField.addListener('keyup', function(obj, event) {
                    var newValue = jQuery( that.imgSrcField.extButton.el.dom ).attr( 'value' );
                    var queryValue = that.imgSrcField.getQueryValue();
                    jQuery( '.x-layer x-combo-list,' +
                        '.x-combo-list-inner,' +
                        '.x-combo-list' ).show();
                });
            },

            addUIInsertButton: function(tabId) {
                var that = this;
                this.insertImgButton = new Aloha.ui.Button({
                    'name' : 'insertimage',
                    'iconClass': 'aloha-button aloha-image-insert',
                    'size' : 'small',
                    'onclick' : function () {
                        that.insertImg();
                    },
                    'tooltip' : "Insert Image",
                    'toggle' : false
                });

                FloatingMenu.addButton('Aloha.continuoustext', this.insertImgButton, "Insert", 2);
            },

            addUISrcButtons: function(tabId) {
                var that = this;

                //not used
                var imgSrcLabel = new Aloha.ui.Button({
                    'label': "Source",
                    'tooltip': "Image Source",
                    'size': 'small'
                });

                //(not used) add the title field for images
                var imgTitleLabel = new Aloha.ui.Button({
                    'label': "Title",
                    'tooltip': "Image Title",
                    'size': 'small'
                });
                this.imgTitleField = new Aloha.ui.AttributeField();
                this.imgTitleField.setObjectTypeFilter();

                //src field, our precious
                this.imgSrcField = new Aloha.ui.AttributeField({
                    'name' : 'imgsrc'
                });
                //fmg: bind to repository (aloha repositories sucks)
                //this.imgSrcField.setTemplate( '<span><b>{name}</b><br/>{url}</span>' );
                //this.imgSrcField.setObjectTypeFilter("fmg-image");

                //select buttom
                this.selectImgButton = new Aloha.ui.Button({
                    'label' : 'Seç',
                    'size' : 'small',
                    'onclick' : function () {
                        that.selectFromPopup();
                    },
                    'tooltip' : "Resim Seç",
                    'toggle' : false
                });

                //important: setting scope to this.name makes it hide when an img is unselected
                FloatingMenu.addButton(this.name, this.imgSrcField, tabId, 2);
                FloatingMenu.addButton(this.name, this.selectImgButton, tabId, 3);
            },

            insertImg: function(){
                var range = Aloha.Selection.getRangeObject(),
                imagePluginUrl = Aloha.getPluginUrl('fmg'),
                imagetag, newImg;

                if ( range.isCollapsed() ) {
                    imagetag = '<img src="' + imagePluginUrl + '/img/blank.jpg" title="" />';
                    newImg = jQuery(imagetag);
                    // add the click selection handler
                    //newImg.click( Aloha.Image.clickImage ); - Using delegate now
                    GENTICS.Utils.Dom.insertIntoDOM(newImg, range, jQuery(Aloha.activeEditable.obj));

                } else {
                    alert('Lütfen resim eklemek için eklenecek yere tıklayın.');
                }
            },

            findImgMarkup: function() {
                var range = Aloha.Selection.getRangeObject();
                var rangeTree = range.getRangeTree();
                for (var i = 0 ; i < rangeTree.length ; i++) {
                    if (rangeTree[i].type == 'full' && rangeTree[i].domobj.nodeName.toLowerCase() == 'img') {
                        return rangeTree[i].domobj;
                    }
                }
                return undefined;
            },

            clickImage: function( e ) {
                this.imageObj = jQuery(e.target);
                //this.imageObj.attr('src')

                FloatingMenu.setScope(this.name);
                this.imgSrcField.setTargetObject(this.imageObj, 'src');
                FloatingMenu.activateTabOfButton('imgsrc');

                //select the image
                var range = Aloha.createRange();

                //çok zor oldu, bunu yapmak. text dahil indeksi, parentin altında arasında.
                //ff, chrome, ve ie9'da çalışıyor.
                var imgindex = this.imageObj.parent().contents().index(this.imageObj);

                //setStart and setEnd take dom node and the offset as parameters
                range.setStart( this.imageObj.parent().get(0), imgindex);
                range.setEnd( this.imageObj.parent().get(0), imgindex+1);

                //add the range to the selection
                Aloha.getSelection().removeAllRanges();
                Aloha.getSelection().addRange( range );
            },

            //fmg: select the image from popup, get the images from global
            selectFromPopup: function(){
                this.selectDialog = $('<div></div>')
                .dialog({
                    autoOpen: false,
                    title: 'Resim Seç',
                    modal: true

                });

                //prepare dialog html from global pageAttachments
                var dialogHtml = "<p><ul>";
                var pageAttachments = window.parent.pageAttachments;
                for (var i = 0; i<pageAttachments.length;i++){
                    dialogHtml += "<li><a href=\"javascript:window.fmgPlugin.onImgSelected("+pageAttachments[i].id+", '"+pageAttachments[i].name+"')\">"+pageAttachments[i].name+"</a></li>";
                }
                dialogHtml += "</ul></p>";

                this.selectDialog.html(dialogHtml);
                this.selectDialog.dialog("option", "buttons", {
                    "İptal": function() {
                        $(this).dialog("close");

                    }
                });
                this.selectDialog.dialog('open');
            },

            onImgSelected: function(id, name){
                this.imageObj.attr("src", "page-attachment/"+id+"/"+name);
                this.selectDialog.dialog("close");
            }
        });

        return window.fmgPlugin;
    });