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
package com.arvatosystems.t9t.tfi.model.bean;


public class MvDrivenEvent {
    private String eventName;
    private String eventComponentId;
    private Object data;
    private String mvEventLocalization;


    public MvDrivenEvent(String eventName, String eventComponentId, Object data,  String mvEventLocalization) {
        this.eventName = eventName;
        this.eventComponentId = eventComponentId;
        this.data = data;
        this.mvEventLocalization = mvEventLocalization;
    }

    public MvDrivenEvent(String eventName, String eventComponentId,String mvEventLocalization) {
        this(eventName, eventComponentId, null,  mvEventLocalization);
    }


    public String getEventName() {
        return eventName;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventComponentId() {
        return eventComponentId;
    }
    public void setEventComponentId(String eventComponentId) {
        this.eventComponentId = eventComponentId;
    }
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
    public String getMvEventLocalization() {
        return mvEventLocalization;
    }
    public void setMvEventLocalization(String mvEventLocalization) {
        this.mvEventLocalization = mvEventLocalization;
    }

}
