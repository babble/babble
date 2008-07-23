// JSCaptcha.java

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

package ed.js;

import java.io.*;
import java.nio.*;
import java.util.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.font.*;

import nl.captcha.servlet.*;
import nl.captcha.obscurity.*;
import nl.captcha.obscurity.imp.*;
import nl.captcha.text.*;
import nl.captcha.text.imp.*;
import nl.captcha.util.*;
import nl.captcha.sandbox.*;

import com.sun.image.codec.jpeg.*;

import ed.net.httpserver.*;

public class JSCaptcha {

    /** Creates a captcha image from a string and adds it to the HTTP response.
     * @param s String to use for the captcha text
     * @param response Response to which to add header and data
     */
    public synchronized void img( String s , HttpResponse response )
        throws IOException {

        byte bb[] = img( s );

        response.setHeader( "Content-Type" , "image/jpeg" );
        response.setData( ByteBuffer.wrap( bb ) );
    }

    /** Creates an captacha image from a string.
     * @param s String to use for captcha text
     * @return The image in the form of an array of bytes
     */
    public synchronized byte[] img( String s )
        throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        _producer.createImage( out , s );
        return out.toByteArray();
    }

    /** @unexpose */
    final CaptchaProducer _producer = new MyCaptchaProducer();

    /** @unexpose */
    static final Properties _defaultProperties = new Properties();
    static {
        _defaultProperties.put( Constants.SIMPLE_CAPTCHA_TEXTPRODUCER_FONTS , "45" );
        _defaultProperties.put( Constants.SIMPLE_CAPTCHA_TEXTPRODUCER_FONTA , "Arial" );
    }
    static class MyCaptchaProducer implements CaptchaProducer  {

        WordRenederer _wordRenderer =  null;
        GimpyEngine _gimpy = null;
        BackgroundProducer _backGroundImp = null;
        TextProducer _textProducer= null;

        int _width = 200;
        int _height = 70;

	public MyCaptchaProducer() {

            _backGroundImp = (BackgroundProducer) Helper.ThingFactory.loadImpl(Helper.ThingFactory.BGIMP, _defaultProperties );
            _textProducer = (TextProducer) Helper.ThingFactory.loadImpl(Helper.ThingFactory.TXTPRDO, _defaultProperties );

            //_gimpy = new WaterRiple(); // default - old sai style
            //_gimpy = new ShadowGimpyImp(); // awful
            _gimpy = new FishEyeGimpyImp();

            _wordRenderer = new MyWordRenderer();
	}

	public void createImage(OutputStream stream, String text)
            throws IOException {

            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(stream);

            BufferedImage bi = _wordRenderer.renderWord( text, _width , _height);

            _gimpy.setProperties( _defaultProperties );
            bi = _gimpy.getDistortedImage( bi );
            bi = _backGroundImp.addBackground(bi);

            JPEGEncodeParam param =  encoder.getDefaultJPEGEncodeParam(bi);
            param.setQuality(Integer.MAX_VALUE,true);
            encoder.encode(bi,param);
	}

        public void setTextProducer(TextProducer textP) {
            _textProducer = textP;
	}

	public String createText(){
            String capText = _textProducer.getText();
            return capText;
	}

        public void setWordRenderer(WordRenederer renederer) {
            _wordRenderer = renederer;
	}


        public void setProperties( Properties p ){
            throw new RuntimeException( "go away" );
        }

        public void setBackGroundImageProducer(BackgroundProducer background) {
            _backGroundImp = background;
	}

        public void setObscurificator( GimpyEngine e ){
            _gimpy = e;
        }
    }

    static class MyWordRenderer implements WordRenederer {

	public BufferedImage renderWord (String word, int width, int height) {

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2D = image.createGraphics();
            g2D.setColor(Color.black);

            RenderingHints hints = new RenderingHints( RenderingHints.KEY_ANTIALIASING,
                                                       RenderingHints.VALUE_ANTIALIAS_ON );

            hints.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                                         RenderingHints.VALUE_RENDER_QUALITY));

            g2D.setRenderingHints(hints);

            Font[] fonts = Helper.getFonts( _defaultProperties );
            Random generator = new Random();

            char[] wc =word.toCharArray();
            Color fontColor = Helper.getColor( _defaultProperties , Constants.SIMPLE_CAPTCHA_TEXTPRODUCER_FONTC,Color.black);
            g2D.setColor(fontColor);
            FontRenderContext frc = g2D.getFontRenderContext();

            int startPosX = width / 10;

            for (int i = 0;i<wc.length;i++) {

                char[] itchar = new char[]{wc[i]};
                int choiceFont = generator.nextInt(fonts.length) ;
                Font itFont = fonts[choiceFont];
                g2D.setFont(itFont);
                LineMetrics lmet = itFont.getLineMetrics(itchar,0,itchar.length,frc);
                GlyphVector gv = itFont.createGlyphVector(frc, itchar);
                double charWitdth = gv.getVisualBounds().getWidth();

                g2D.drawChars(itchar,0,itchar.length,startPosX ,(int)(height*.7) );
                startPosX = startPosX+(int)charWitdth+2;

            }

            return image;
	}

	public void setProperties(Properties properties) {
            throw new RuntimeException( "go away" );
	}

    }

}
