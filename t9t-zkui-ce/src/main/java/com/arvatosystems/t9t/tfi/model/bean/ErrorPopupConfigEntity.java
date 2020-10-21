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


public class ErrorPopupConfigEntity {
    private String returnCode;
    private String popupTitle;
    private String errorIntroduction;
    private String popupImg;

    public ErrorPopupConfigEntity(String returnCode, String popupTitle, String popupImg, String errorIntroduction) {
        super();
        this.returnCode = returnCode;
        this.popupTitle = popupTitle;
        this.errorIntroduction = errorIntroduction;
        this.popupImg = popupImg;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getPopupTitle() {
        return popupTitle;
    }

    public void setPopupTitle(String popupTitle) {
        this.popupTitle = popupTitle;
    }

    public String getErrorIntroduction() {
        return errorIntroduction;
    }

    public void setErrorIntroduction(String errorIntroduction) {
        this.errorIntroduction = errorIntroduction;
    }

    public String getPopupImg() {
        return popupImg;
    }

    public void setPopupImg(String popupImg) {
        this.popupImg = popupImg;
    }

    @Override
    public String toString() {
        return "ErrorPopupEntity [returnCode=" + returnCode + ", popupTitle=" + popupTitle + ", errorIntroduction=" + errorIntroduction + ", popupImg="
                + popupImg + "]";
    }






}
