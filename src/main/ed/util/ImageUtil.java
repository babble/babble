// ImageUtil.java

/**
*    Copyright (C) 2008 10gen Inc.
*
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.util;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.plugins.jpeg.*;

import ed.js.*;
import ed.appserver.*;

/** @expose */
public class ImageUtil {

    /**
     */
    public static JSFile imgToJpg( BufferedImage img , double compressionQuality , String filename )
        throws IOException {

        String ext = "jpg";
        if ( filename != null && filename.indexOf( "." ) >= 0 ){
            String test = MimeTypes.getExtension( filename );
            String mime = MimeTypes.get( test );
            if ( mime != null && mime.startsWith( "image/" ) )
                ext = test;
        }
        String mime = MimeTypes.get( ext );

        ByteArrayOutputStream out = convertImage(img, compressionQuality, ext);

        return new JSInputFile( filename , mime , out.toByteArray() );
    }

    /**
     * Convert a BufferedImage to a String representation of that image,
     * in the format appropriate for the file extension ext.
     *
     * @param img image to be converted. must be non-null.
     * @param compressionQuality the quality paramater to be used for lossy encodings [0.0, 1.0].
     * @param ext the extension of the desired image type. defaults to "jpg",
     *            which results in a jpeg image.
     * @param enc the encoding to use for the resulting String.
     * @return the representation of the BufferedImage as a string.
     */
    public static String imgToString(BufferedImage img, double compressionQuality, String ext, String enc)
        throws IOException {
        if (img == null) {
            throw new IllegalArgumentException("img must not be null");
        }

        if (ext == null) {
            ext = "jpg";
        }

        if (enc == null) {
            throw new IllegalArgumentException("enc must not be null");
        }

        return convertImage(img, compressionQuality, ext).toString(enc);
    }

    private static ByteArrayOutputStream convertImage(BufferedImage img, double compressionQuality, String ext)
        throws IOException {

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName( ext );
        if ( ! writers.hasNext() )
            throw new RuntimeException( "no writer for : " + ext );

        ImageWriter writer = writers.next();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream( out );
        writer.setOutput(ios);

        ImageWriteParam iwparam = new MyImageWriteParam();
        iwparam.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );
        iwparam.setCompressionQuality( (float)compressionQuality );

        writer.write(null, new IIOImage( img, null, null), iwparam);

        ios.flush();
        writer.dispose();
        ios.close();

        return out;
    }

    public static class MyImageWriteParam extends JPEGImageWriteParam {
        public MyImageWriteParam() {
            super(Locale.getDefault());
        }

        public void setCompressionQuality(float quality) {
            if (quality < 0.0F || quality > 1.0F) {
                throw new IllegalArgumentException("Quality out-of-bounds!");
            }
            this.compressionQuality = 256 - (quality * 256);
        }
    }

    public static BufferedImage getScaledInstance(BufferedImage img ,
                                           double targetWidth ,
                                                  double targetHeight ){
        return getScaledInstance( img , (int)targetWidth , (int)targetHeight , true );
    }

    public static BufferedImage getScaledInstance(BufferedImage img,
                                                  int targetWidth,
                                                  int targetHeight,
                                                  boolean higherQuality){

        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    /**
     * Rotates an image by the specified number of degrees. Only handles rotations through
     * multiples of 90 degrees.
     *
     * @param img image to be rotated. must be non-null
     * @param degrees angle through which the image is rotated. must be a multiple of 90.
     * @return a new BufferedImage, the result of applying the rotation to img
     */
    public static BufferedImage getRotatedInstance(final BufferedImage img, final int degrees) {
        if (img == null) {
            throw new IllegalArgumentException("img must not be null");
        }
        if (degrees % 90 != 0) {
            throw new IllegalArgumentException("can only rotate images in multiples of 90 degrees");
        }
        int turns = (degrees / 90) % 4;

        // TODO maybe we should short-circuit here if turns is 0 => just return a copy of img.

        int result_width = (turns % 2 == 0) ? img.getWidth() : img.getHeight();
        int result_height = (turns % 2 == 0) ? img.getHeight() : img.getWidth();

        BufferedImage result = new BufferedImage(result_width, result_height, img.getType());

        for (int i = 0; i < result_width; i++) {
            for (int j = 0; j < result_height; j++) {
                int target_x;
                int target_y;

                switch (turns) {
                case 0:
                    target_x = i;
                    target_y = j;
                case 1:
                    target_x = j;
                    target_y = result_width - i - 1;
                    break;
                case 2:
                    target_x = result_width - i - 1;
                    target_y = result_height - j - 1;
                    break;
                case 3:
                    target_x = result_height - j - 1;
                    target_y = i;
                    break;
                default:
                    throw new RuntimeException("this should never happen");
                }

                result.setRGB(i, j, img.getRGB(target_x, target_y));
            }
        }
        return result;
    }

    /**
     * Flips an image horizontally. The edge that was the left becomes the right, and vice versa.
     *
     * @param img image to be flipped. must be non-null
     * @return a new BufferedImage, the result of flipping img
     */
    public static BufferedImage getHorizontallyFlippedInstance(final BufferedImage img) {
        if (img == null) {
            throw new IllegalArgumentException("img must not be null");
        }

        BufferedImage result = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                result.setRGB(img.getWidth() - i - 1, j, img.getRGB(i, j));
            }
        }
        return result;
    }

    /**
     * Flips an image vertically. The edge that was the top becomes the bottom, and vice versa.
     *
     * @param img image to be flipped. must be non-null
     * @return a new BufferedImage, the result of flipping img
     */
    public static BufferedImage getVerticallyFlippedInstance(final BufferedImage img) {
        if (img == null) {
            throw new IllegalArgumentException("img must not be null");
        }

        BufferedImage result = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                result.setRGB(i, img.getHeight() - j - 1, img.getRGB(i, j));
            }
        }
        return result;
    }

    /**
     * Crops an image. The bounding box to crop to is given by left_x * width, top_y * height
     * right_x * width, and bottom_y * height.
     *
     * @param img      image to be cropped. must be non-null
     * @param left_x   the left border of the bounding box, proportional to the image width.
     *                 specified as a float value from 0.0 to 1.0 inclusive.
     * @param top_y    the top border of the bounding box, proportional to the image height.
     *                 specified as a float value from 0.0 to 1.0 inclusive.
     * @param right_x  the right border of the bounding box, proportional to the image width.
     *                 specified as a float value from 0.0 to 1.0 inclusive.
     * @param bottom_y the bottom border of the bounding box, proportional to the image height.
     *                 specified as a float value from 0.0 to 1.0 inclusive.
     * @return a new BufferedImage, cropped to the bounding box specified.
     */
    public static BufferedImage getCroppedInstance(final BufferedImage img,
                                                   final float left_x,
                                                   final float top_y,
                                                   final float right_x,
                                                   final float bottom_y) {
        if (img == null) {
            throw new IllegalArgumentException("img must not be null");
        }

        int left = (int)(left_x * img.getWidth());
        int width = (int)(right_x * img.getWidth()) - left;
        int top = (int)(top_y * img.getHeight());
        int height = (int)(bottom_y * img.getHeight()) - top;

        BufferedImage result = new BufferedImage(width, height, img.getType());
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                result.setRGB(i, j, img.getRGB(left + i, top + j));
            }
        }
        return result;
    }
}
