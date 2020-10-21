/**
 * CK Editor configuration to disable the default encoding on escape character
 * Ref:FT-3172
 */

CKEDITOR.editorConfig = function(config) {
    
    config.htmlEncodeOutput = false;
    config.entities = false;
    config.autoParagraph = false;
}