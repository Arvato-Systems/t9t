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
package com.arvatosystems.t9t.barcode.be.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.arvatosystems.t9t.doc.services.IImageCustomizer;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

/**
 * Special variant of the QR code, used by the Swiss payment slip.
 * It produces a QR code with a Swiss cross in the middle.
 */
@Named("SwissQR")
@Singleton
public class SwissQRCustomizer implements IImageCustomizer {

    // dimensions: The swiss QR code is 46x46mm and the Swiss logo area is 7x7mm, of which 6x6mm should be used by the boxed cross.
    private static final double WIDTH_OF_AREA   = 7.0 / 46.0;
    private static final double START_OF_AREA   = (1.0 - WIDTH_OF_AREA) * 0.5;
    private static final double WIDTH_OF_AREA_2 = 5.0 / 46.0;
    private static final double START_OF_AREA_2 = (1.0 - WIDTH_OF_AREA_2) * 0.5;
    private static final double CROSS_WIDTH     = 35.0 / 9.0 / 46.0;
    private static final double CROSS_HEIGHT    =  7.0 / 6.0 / 46.0;
    private static final double CROSS_START_H   = (1.0 - CROSS_WIDTH) * 0.5;
    private static final double CROSS_START_V   = (1.0 - CROSS_HEIGHT) * 0.5;

    @Override
    public void customizeImage(final BufferedImage image, final int black, final int white) throws Exception {
        // obtain size of image
        final int w = image.getWidth();
        final int h = image.getHeight();

        // create graphics context
        final Graphics2D graphics = image.createGraphics();

        // create black square within white square
        graphics.setColor(new Color(white));
        graphics.fillRect(mult(w, START_OF_AREA), mult(h, START_OF_AREA), mult(w, WIDTH_OF_AREA), mult(h, WIDTH_OF_AREA));
        graphics.setColor(new Color(black));
        graphics.fillRect(mult(w, START_OF_AREA_2), mult(h, START_OF_AREA_2), mult(w, WIDTH_OF_AREA_2), mult(h, WIDTH_OF_AREA_2));

        // draw Swiss cross
        graphics.setColor(new Color(white));
        graphics.fillRect(mult(w, CROSS_START_H), mult(h, CROSS_START_V), mult(w, CROSS_WIDTH), mult(h, CROSS_HEIGHT));
        graphics.fillRect(mult(w, CROSS_START_V), mult(h, CROSS_START_H), mult(w, CROSS_HEIGHT), mult(h, CROSS_WIDTH));
        graphics.dispose();
    }

    protected int mult(final int a, final double factor) {
        return (int)(a * factor);
    }
}
