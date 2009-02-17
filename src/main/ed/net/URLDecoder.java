// URLDecoder.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

// copied from http://svn.apache.org/viewvc/harmony/enhanced/classlib/trunk/modules/luni/src/main/java/java/net/URLDecoder.java?revision=535560

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ed.net;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * This class is used to decode a string which is encoded in the
 * <code>application/x-www-form-urlencoded</code> MIME content type.
 */
public class URLDecoder {

    static Charset defaultCharset;

    /**
     * Decodes the string argument which is assumed to be encoded in the
     * <code>x-www-form-urlencoded</code> MIME content type.
     * <p>
     * '+' will be converted to space, '%' and two following hex digit
     * characters are converted to the equivalent byte value. All other
     * characters are passed through unmodified.
     * <p>
     * e.g. "A+B+C %24%25" -> "A B C $%"
     * 
     * @param s
     *            java.lang.String The encoded string.
     * @return java.lang.String The decoded version.
     * 
     * @deprecated use URLDecoder#decode(String, String) instead
     */
    @Deprecated
    public static String decode(String s) {

        if (defaultCharset == null) {
            try {
                defaultCharset = Charset.forName(
                        System.getProperty("file.encoding")); //$NON-NLS-1$
            } catch (IllegalCharsetNameException e) {
                // Ignored
            } catch (UnsupportedCharsetException e) {
                // Ignored
            }

            if (defaultCharset == null) {
                defaultCharset = Charset.forName("ISO-8859-1"); //$NON-NLS-1$
            }
        }
        return decode(s, defaultCharset);
    }

    /**
     * Decodes the string argument which is assumed to be encoded in the
     * <code>x-www-form-urlencoded</code> MIME content type using the
     * specified encoding scheme.
     * <p>
     * '+' will be converted to space, '%' and two following hex digit
     * characters are converted to the equivalent byte value. All other
     * characters are passed through unmodified.
     * 
     * <p>
     * e.g. "A+B+C %24%25" -> "A B C $%"
     * 
     * @param s
     *            java.lang.String The encoded string.
     * @param enc
     *            java.lang.String The encoding scheme to use
     * @return java.lang.String The decoded version.
     */
    public static String decode(final String s, final String enc)
            throws UnsupportedEncodingException {

        if (enc == null) {
            throw new NullPointerException();
        }

        // If the given encoding is an empty string throw an exception.
        if (enc.length() == 0) {
            throw new UnsupportedEncodingException( "empyy encoding" );
        }

        if (s.indexOf('%') == -1) {
            if (s.indexOf('+') == -1)
                return s;
            char str[] = s.toCharArray();
            for (int i = 0; i < str.length; i++) {
                if (str[i] == '+')
                    str[i] = ' ';
            }
            return new String(str);
        }
        
        Charset charset = null;
        try {
            charset = Charset.forName(enc);
        } catch (IllegalCharsetNameException e) {
            throw (UnsupportedEncodingException) (new UnsupportedEncodingException(
                    enc).initCause(e));
        } catch (UnsupportedCharsetException e) {
            throw (UnsupportedEncodingException) (new UnsupportedEncodingException(
                    enc).initCause(e));
        }
        
        return decode(s, charset);
    }

    private static String decode( final String s, final Charset charset) {

        char str_buf[] = new char[s.length()];
        byte buf[] = new byte[s.length() / 3];
        int buf_len = 0;

        for (int i = 0; i < s.length();) {
            char c = s.charAt(i);
            if (c == '+') {
                str_buf[buf_len] = ' ';
            } else if (c == '%') {

                int len = 0;
                do {
                    if (i + 2 >= s.length()) {
                        throw new IllegalArgumentException( s );
                    }
                    int d1 = Character.digit(s.charAt(i + 1), 16);
                    int d2 = Character.digit(s.charAt(i + 2), 16);
                    if (d1 == -1 || d2 == -1) {
                        throw new IllegalArgumentException( s );
                    }
                    buf[len++] = (byte) ((d1 << 4) + d2);
                    i += 3;
                } while (i < s.length() && s.charAt(i) == '%');

                CharBuffer cb = charset.decode(ByteBuffer.wrap(buf, 0, len));
                len = cb.length();
                System.arraycopy(cb.array(), 0, str_buf, buf_len, len);
                buf_len += len;
                continue;
            } else {
                str_buf[buf_len] = c;
            }
            i++;
            buf_len++;
        }
        return new String(str_buf, 0, buf_len);
    }
}
