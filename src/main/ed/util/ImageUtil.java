// ImageUtil.java

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

        return new JSInputFile( filename , mime , out.toByteArray() );
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

}
