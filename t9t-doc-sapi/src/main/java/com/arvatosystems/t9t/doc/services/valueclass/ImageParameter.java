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
package com.arvatosystems.t9t.doc.services.valueclass;

import com.arvatosystems.t9t.barcode.api.FlipMode;

public class ImageParameter {
    public final int       width;
    public final int       height;
    public final Integer   rotation;   // rotation in degrees
    public final FlipMode  flipMode;
    public final Double    scale;

    public ImageParameter(int width, int height, Integer rotation, FlipMode flipMode, Double scale) {
        this.width    = width;
        this.height   = height;
        this.rotation = rotation;
        this.flipMode = flipMode;
        this.scale    = scale;
    }
}
