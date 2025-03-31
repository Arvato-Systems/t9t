/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.viewmodel.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.AMedia;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import com.arvatosystems.t9t.ai.request.AiChatRequest;
import com.arvatosystems.t9t.ai.request.AiChatResponse;
import com.arvatosystems.t9t.base.JsonUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;
import com.arvatosystems.t9t.zkui.util.ZulUtils;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ByteArray;

public class AiChatViewModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiChatViewModel.class);

    private final IT9tRemoteUtils t9tRemoteUtils = Jdp.getRequired(IT9tRemoteUtils.class);

    private String userInput;
    private MediaData userUpload;
    private Media selectedMedia;

    private final List<ChatMessageEntry> messages = new ArrayList<>();
    private final String fromYou = ZulUtils.translate("aiChat", "from.you");
    private final String fromSystem = ZulUtils.translate("aiChat", "from.system");

    @Wire(".messageBox")
    protected Div msgBox;

    @Init
    public void init() {
        final AiChatResponse response = chatRequest();
        final String aiName = T9tUtil.nvl(response.getAiAssistant().getAiName(), fromSystem);
        populateMessage(aiName, response.getTextOutput(), getMedia(response.getMediaOutput()));
    }

    @Command
    @NotifyChange({ "userInput", "userUpload", "selectedMedia", "messages" })
    public void submit() {
        if ((userInput == null || userInput.isBlank()) && userUpload == null) {
            return;
        }
        final AiChatResponse response = chatRequest();
        final String aiName = T9tUtil.nvl(response.getAiAssistant().getAiName(), fromSystem);
        populateMessage(fromYou, userInput, getMedia(userUpload));
        populateMessage(aiName, response.getTextOutput(), getMedia(response.getMediaOutput()));
        userInput = null;
        userUpload = null;
        selectedMedia = null;
    }

    @Command
    @NotifyChange({ "userInput", "userUpload", "selectedMedia", "messages" })
    public void reset() {
        messages.clear();
        userInput = null;
        userUpload = null;
        selectedMedia = null;
        init();
    }

    public MediaData getUserUpload() {
        return userUpload;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(final String userInput) {
        this.userInput = userInput;
    }

    public Media getSelectedMedia() {
        return selectedMedia;
    }

    public List<ChatMessageEntry> getMessages() {
        return messages;
    }

    public int getUserInputMaxlength() {
        return AiChatRequest.meta$$userInput.getLength();
    }

    @Command
    @NotifyChange({ "selectedMedia" })
    public void uploadFile(@BindingParam("eventData") final UploadEvent event) {
        selectedMedia = event.getMedia();
        final String filename = selectedMedia.getName();
        final MediaTypeDescriptor descriptor = T9tUtil.getFormatByContentTypeOrFilename(selectedMedia.getContentType(), filename);
        if (descriptor == null) {
            Messagebox.show(ZulUtils.translate("err", "fileTypeNotSupported", selectedMedia.getContentType()), ZulUtils.translate("err", "title"),
                Messagebox.OK, Messagebox.ERROR);
            selectedMedia = null;
            return;
        }

        userUpload = new MediaData(descriptor.getMediaType());
        if (filename != null) {
            final Map<String, Object> zMap = new HashMap<>();
            zMap.put(T9tConstants.MEDIA_DATA_Z_KEY_FILENAME, filename);
            userUpload.setZ(zMap);
        }
        LOGGER.debug("Uploaded media has format {} and file name {}", selectedMedia.getFormat(), filename);
        if (descriptor.getIsText()) {
            userUpload.setText(selectedMedia.getStringData());
        } else {
            final byte[] byteData = selectedMedia.getByteData();
            userUpload.setRawData(new ByteArray(byteData));
        }
    }

    @Command
    public void downloadFile(@BindingParam("index") final int itemIndex) {
        final ChatMessageEntry entry = messages.get(itemIndex);
        Filedownload.save(entry.getMedia());
    }

    private AiChatResponse chatRequest() {
        final AiChatRequest request = new AiChatRequest();
        request.setUserInput(userInput);
        request.setUserUpload(userUpload);
        return t9tRemoteUtils.executeExpectOk(request, AiChatResponse.class);
    }

    private void populateMessage(final String from, final List<String> textMessages, final Media media) {
        if (textMessages != null) {
            boolean first = false;
            for (final String text : textMessages) {
                ChatMessageEntry chatMessage;
                if (!first) {
                    chatMessage = new ChatMessageEntry(from, text, media);
                    first = true;
                } else {
                    chatMessage = new ChatMessageEntry(null, text, null);
                }
                messages.add(chatMessage);
            }
        }
    }

    private void populateMessage(final String from, final String textMessage, final Media media) {
        messages.add(new ChatMessageEntry(from, textMessage, media));
    }

    private Media getMedia(final MediaData mediaData) {
        if (mediaData == null) {
            return null;
        }
        final Map<String, Object> zMap = mediaData.getZ();
        final String filename = JsonUtil.getZString(zMap, T9tConstants.MEDIA_DATA_Z_KEY_FILENAME, null);
        final MediaTypeDescriptor descriptor = MediaTypeInfo.getFormatByType(mediaData.getMediaType());
        final String contentType;
        if (descriptor == null) {
            contentType = null;  // unkown media type
        } else {
            contentType = descriptor.getMimeType();
        }
        if (mediaData.getRawData() != null) {
            return new AMedia(filename, null, contentType, mediaData.getRawData().getBytes());
        } else {
            return new AMedia(filename, null, contentType, mediaData.getText());
        }
    }
}
