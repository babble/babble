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

package ed.lang.ruby;

import java.io.IOException;
import java.io.OutputStream;

import ed.net.httpserver.JxpWriter;

public class RubyJxpOutputStream extends OutputStream {

    private final JxpWriter _writer;

    public RubyJxpOutputStream(JxpWriter writer) {
	_writer = writer;
    }

    public void write(int b) throws IOException {
	_writer.print(b);
    }

    public void write(byte[] b) throws IOException {
	_writer.print(new String(b));
    }

    public void write(byte[] b, int off, int len) throws IOException {
	byte[] subseq = new byte[len];
	System.arraycopy(b, off, subseq, 0, len);
	_writer.print(new String(subseq));
    }

    public void flush() throws IOException {
	_writer.flush();
    }

    public void close() throws IOException {
	_writer.reset();
    }
}
