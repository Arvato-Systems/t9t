/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.components;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkforge.ckez.CKeditor;
import org.zkoss.image.AImage;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.annotation.ComponentAnnotation;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.FieldMappers;
import com.arvatosystems.t9t.component.datafields.DataFieldParameters;
import com.arvatosystems.t9t.component.datafields.TextDataField;
import com.arvatosystems.t9t.component.datafields.XenumDataField;
import com.arvatosystems.t9t.component.ext.IDataFieldFactory;
import com.arvatosystems.t9t.component.ext.IViewModelOwner;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.Multiplicity;
import de.jpaw.bonaparte.util.FieldGetter;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ByteArray;

/**
 * A row with 2 cells, a label and a field. The row has a generic getValue()
 * method and therefore we can use any object type.
 */
// the @ComponentAnnotation annotation informs ZK to invoke getValue() and
// update the viewmodel after the onChange event
// see
// https://www.zkoss.org/wiki/ZK_Developer's_Reference/MVVM/Advanced/Binding_Annotation_for_a_Custom_Component
@ComponentAnnotation("value:@ZKBIND(ACCESS=both,SAVE_EVENT=onChange)")
public class CkEditor28 extends Row {
    private static final long serialVersionUID = -770193551161941L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CkEditor28.class);

    protected ApplicationSession as;
    protected String viewModelId;
    protected CrudViewModel<BonaPortable, TrackingBase> crudViewModel;
    protected final IDataFieldFactory ff = Jdp.getRequired(IDataFieldFactory.class);
    protected Form28 form = null;
    protected String cellHeight = "32px";

    private XenumDataField mediaTypeDataField;
    private TextDataField textDataField;
    private CKeditor ckEditor;
    private Label label;
    private Cell cell1a;
    private Cell cell1b;
    private String dataFieldId;
    private Object deferredValue;
    private int colspan1 = 1;
    private int rows1 = 1;
    private Button uploadButton;
    private Image anchorToDisplayImage;
    private byte[] mediaDataByteArray;
    private Hbox dropuploadHbox;

    public CkEditor28() {
        super();
        cell1a = new Cell();
        cell1a.setParent(this);
        cell1b = new Cell();
        cell1b.setParent(this);
        label = new Label();
        label.setParent(cell1a);
        if (cellHeight != null) {
            cell1a.setHeight(cellHeight);
            cell1b.setHeight(cellHeight);
        }

        addEventListener(Events.ON_CREATE, (ev) -> myOnCreate());
    }

    @Override
    public void setId(String id) {
        // forward the id to the data field only
        super.setId(id);
        dataFieldId = id;
    }

    @Override
    public void setValue(Object t) {
        LOGGER.debug("{}.setValue({}) called (class {})", dataFieldId, t,
                t == null ? "N/A" : t.getClass().getSimpleName());
        deferredValue = t;

        MediaData mediaData = (MediaData) t;

        if (mediaData != null) {
            if (mediaTypeDataField != null) {
                mediaTypeDataField.setValue(mediaData.getMediaType());
            }

            if (textDataField != null) {
                textDataField.setValue(mediaData.getText());
            }

            if (ckEditor != null) {
                ckEditor.setValue(mediaData.getText());
            }

            if (mediaData.getRawData() != null) {
                org.zkoss.image.Image image = null;
                try {
                    image = new AImage("sample", mediaData.getRawData().getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mediaDataByteArray = mediaData.getRawData().getBytes();
                anchorToDisplayImage.setContent(image);
            }else {
                mediaDataByteArray = null;
                anchorToDisplayImage.setSrc(null);
            }

            switchEditor();

        } else {
            if (mediaTypeDataField != null) {
                mediaTypeDataField.getComponent().setRawValue(null);
            }

            if (textDataField != null) {
                textDataField.getComponent().setRawValue(null);
            }

            if (ckEditor != null) {
                ckEditor.setValue(null);
            }

            if (mediaDataByteArray != null) {
                mediaDataByteArray = null;
            }

            if (anchorToDisplayImage != null) {
                anchorToDisplayImage.setSrc(null);
            }

        }
    }

    @Override
    public Object getValue() {

        MediaData md = new MediaData();

        md.setMediaType((MediaXType) mediaTypeDataField.getValue());

        if (md.getMediaType().getBaseEnum() == MediaType.HTML) {
            md.setText(ckEditor.getValue());
            md.setRawData(null);
        } else if (md.getMediaType().getBaseEnum() == MediaType.TEXT) {
            md.setText(textDataField.getValue());
            md.setRawData(null);
        } else {
            md.setText(null);
            if (mediaDataByteArray != null) {
                ByteArray byteArray = new ByteArray(mediaDataByteArray);
                md.setRawData(byteArray);
            } else {
                md.setRawData(new ByteArray(new byte [0]));
            }
        }

        LOGGER.debug("{}.getValue() called, returns {}", dataFieldId, md);
        return md;
    }

    private void switchEditor() {
        textDataField.getComponent().setVisible(false);
        ckEditor.setVisible(false);
        setDropUploadVisibility(false);

        MediaXType mdt =  ((MediaData) getValue()).getMediaType();
        Enum<?> mediaType = mdt.getBaseEnum();
        if (mediaType == MediaType.HTML) {
            ckEditor.setVisible(true);
        } else if (mediaType == MediaType.GIF || mediaType == MediaType.JPG || mediaType == MediaType.PNG) {
            setDropUploadVisibility(true);
        } else {
            textDataField.getComponent().setVisible(true);
        }
    }

    // @Listen("onCreate")
    protected void myOnCreate() {
        // LOGGER.debug("cells 28 onCreate");
        IViewModelOwner vmOwner = GridIdTools.getAnchestorOfType(this, IViewModelOwner.class);
        LOGGER.debug("vmOwner is {}",
                vmOwner == null ? "NULL" : vmOwner.getClass().getSimpleName() + ":" + vmOwner.getViewModelId());
        viewModelId = GridIdTools.enforceViewModelId(vmOwner);
        crudViewModel = vmOwner.getCrudViewModel();
        as = vmOwner.getSession();
        String strippedFieldname = FieldMappers.stripIndexes(dataFieldId);

        FieldDefinition mediaDataExpected = FieldGetter
                .getFieldDefinitionForPathname(crudViewModel.dtoClass.getMetaData(), strippedFieldname);

        if (!mediaDataExpected.getDataType().toLowerCase().equals("mediadata")) {
            LOGGER.error("Field id {} is expected to be of MediaData type", dataFieldId);
        }

        // provide the label text
        boolean isRequired = mediaDataExpected.getIsRequired()
                && mediaDataExpected.getMultiplicity() == Multiplicity.SCALAR;

        String requiredMarker = isRequired ? "*" : "";
        label.setId(dataFieldId + ".l");
        label.setValue(as.translate(viewModelId, dataFieldId) + requiredMarker + ":");

        // check if we are within a form. This is done in order to register the fields for automatic enabling / disabling
        form = GridIdTools.findAnchestorOfType(this, Form28.class);

        // Create the media type box
        FieldDefinition mediaTypeEnum = FieldGetter.getFieldDefinitionForPathname(crudViewModel.dtoClass.getMetaData(),
                dataFieldId + ".mediaType");

        mediaTypeDataField = new XenumDataField(new DataFieldParameters(mediaTypeEnum, dataFieldId + ".mediaType", null, as, null, null), null);

        Component mediaTypeComponent = mediaTypeDataField.getComponent();
        if (mediaTypeComponent != null) {
            mediaTypeComponent.setId(dataFieldId + ".mediaType.c");
            ((Combobox)mediaTypeComponent).setStyle("margin-top: 5px;");

            // also forward the onChange event to allow saving of changed data
            mediaTypeComponent.addEventListener(Events.ON_CHANGE, (ev) -> {
                LOGGER.debug("onChange caught for {}, current value is {}", getId(), getValue());
                Events.postEvent(new Event(Events.ON_CHANGE, this, null));
                switchEditor();
            });
        }

        FieldDefinition text = FieldGetter.getFieldDefinitionForPathname(crudViewModel.dtoClass.getMetaData(),
                dataFieldId + ".text");

        textDataField = new TextDataField(new DataFieldParameters(text, dataFieldId + ".text", null, as, null, null));
        Component textFieldComponent = textDataField.getComponent();
        if (textFieldComponent != null) {
            textFieldComponent.setId(dataFieldId + ".text.c");
            textFieldComponent.setVisible(false);

            ((Textbox) textFieldComponent).setRows(rows1);

            // also forward the onChange event to allow saving of changed data
            textFieldComponent.addEventListener(Events.ON_CHANGE, (ev) -> {
                LOGGER.debug("onChange caught for {}, current value is {}", getId(), getValue());
                Events.postEvent(new Event(Events.ON_CHANGE, this, null));
            });
        }

        ckEditor = new CKeditor();
        ckEditor.setId(dataFieldId + ".text.e");
        ckEditor.setHflex("1");
        ckEditor.setVisible(false);
        ckEditor.setCustomConfigurationsPath("/js/config/ckeditor.config.js");

        // also forward the onChange event to allow saving of changed data
        ckEditor.addEventListener(Events.ON_CHANGE, (ev) -> {
            LOGGER.debug("onChange caught for {}, current value is {}", getId(), getValue());
            Events.postEvent(new Event(Events.ON_CHANGE, this, null));
        });

        constructDropUpload();

        Component groupboxForMediaData = createGroupBoxForMediaData(mediaTypeComponent, textFieldComponent, ckEditor, dropuploadHbox);

        setDropUploadVisibility(false);

        groupboxForMediaData.setParent(cell1b);

        if (form != null) {
            form.register(mediaTypeDataField);
            form.register(textDataField);
        }

    }

    private void constructDropUpload() {
        dropuploadHbox = new Hbox();
        dropuploadHbox.setHflex("1");

        anchorToDisplayImage = new Image();
        anchorToDisplayImage.setId(dataFieldId + "anchor.img");
        anchorToDisplayImage.setParent(dropuploadHbox);
        anchorToDisplayImage.setHeight("250px");
        anchorToDisplayImage.setWidth("250px");

        Vbox uploadSection = new Vbox();
        uploadSection.setHflex("1");
        uploadSection.setParent(dropuploadHbox);

        uploadButton = new Button();
        uploadButton.setId(dataFieldId + ".img.e");
        uploadButton.setParent(uploadSection);
        uploadButton.setUpload("true");
        uploadButton.setLabel(as.get().translate("IMAGE_UPLOAD", "uploadImage"));
        uploadButton.addEventListener(Events.ON_UPLOAD, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                if (event instanceof UploadEvent) {
                    UploadEvent uEvent = (UploadEvent) event;
                    Media image = uEvent.getMedia();
                    org.zkoss.image.Image imageUploaded = new AImage("sample", image.getByteData());
                    mediaDataByteArray = uEvent.getMedia().getByteData();
                    anchorToDisplayImage.setContent(imageUploaded);
                }
            }
        });
    }

    private void setDropUploadVisibility(boolean isVisible) {
        if (dropuploadHbox != null) {
            dropuploadHbox.setVisible(isVisible);
        }
    }

    private Component createGroupBoxForMediaData(Component mediaTypeComponent, Component textBox, Component ckEditor, Component dropuploadHbox) {

        Groupbox groupBox = new Groupbox();
        groupBox.setHflex("1");

        Vbox vbox = new Vbox();
        vbox.setHflex("1");

        mediaTypeComponent.setParent(vbox);
        textBox.setParent(vbox);
        ckEditor.setParent(vbox);
        dropuploadHbox.setParent(vbox);

        vbox.setParent(groupBox);

        return groupBox;
    }

    public int getColspan1() {
        return colspan1;
    }

    public void setColspan1(int colspan1) {
        this.colspan1 = colspan1;
        cell1b.setColspan(colspan1);
    }

    public int getRows1() {
        return rows1;
    }

    public void setRows1(int rows1) {
        this.rows1 = rows1;
    }
}
