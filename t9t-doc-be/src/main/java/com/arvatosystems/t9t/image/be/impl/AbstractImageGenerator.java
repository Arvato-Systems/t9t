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
package com.arvatosystems.t9t.image.be.impl;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.barcode.api.FlipMode;
import com.arvatosystems.t9t.doc.services.IImageGenerator;
import com.arvatosystems.t9t.doc.services.valueclass.ImageParameter;
import com.google.zxing.common.BitMatrix;

import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.util.ByteArray;

public abstract class AbstractImageGenerator implements IImageGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImageGenerator.class);

    static {
        System.setProperty("java.awt.headless", "true");
    }

    protected MediaData toImage(BitMatrix b, ImageParameter params) throws Exception {
        final int width = b.getWidth();
        final int height = b.getHeight();
        final boolean rotated = params.rotation != null && params.rotation.intValue() == 90;
        final BufferedImage image = new BufferedImage(
                rotated ? height : width,
                rotated ? width : height,
                BufferedImage.TYPE_INT_RGB); // create an empty image
        final int white = 255 << 16 | 255 << 8 | 255;
        final int black = 0;
        for (int i = 0; i < width; i++) {
            int sx = FlipMode.FLIP_HORIZONTALLY == params.flipMode ? width - 1 - i : i;
            for (int j = 0; j < height; j++) {
                int sy = FlipMode.FLIP_VERTICALLY == params.flipMode ? height - 1 - j : j;
                if (rotated)
                    image.setRGB(j, i, b.get(sx, sy) ? black : white); // set pixel one by one
                else
                    image.setRGB(i, j, b.get(sx, sy) ? black : white); // set pixel one by one
            }
        }
//        if ((params.rotation != null && params.rotation.intValue() != 0) ||
//            (params.scale    != null && params.scale.doubleValue() != 1.0) ||
//            (params.flipMode != null && params.flipMode != FlipMode.NO_FLIPPING)) {
//            // at least some kind of transformation is done
//            image = transform(image, params);
//        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(8000);
        ImageIO.write(image, "png", baos);
        MediaData m = new MediaData();
        m.setMediaType(MediaTypes.MEDIA_XTYPE_PNG);
        m.setRawData(ByteArray.fromByteArrayOutputStream(baos));
        LOGGER.debug("Generated barcode image has {} bytes in PNG format for dimensions {} x {}", m.getRawData().length(), width, height);
        return m;
    }

    BufferedImage transform(BufferedImage src, ImageParameter params) {
        // transform
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Graphics2D g2d = dest.createGraphics();

        AffineTransform origAT = g2d.getTransform();
        AffineTransform transform = new AffineTransform();
        double scale = 1.0;

        if (params.rotation != null && params.rotation.intValue() != 0) {
            transform.rotate(Math.toRadians(params.rotation), src.getWidth()/2, src.getHeight()/2);
        }
        if (params.scale    != null && params.scale.doubleValue() != 1.0) {
            scale = params.scale.doubleValue();
        }
        transform.scale(scale, scale);
        g2d.transform(transform);

        g2d.drawImage(src, 0, 0, null);

        g2d.setTransform(origAT);
        g2d.dispose();

        return dest;
    }
}
