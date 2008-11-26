package ed.net.httpserver;

import org.testng.annotations.Test;
import static  org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

public class PostDataInMemoryTest {

    @Test
    public void testFillin() throws Exception {

        String s = "Woogie!";

        PostDataInMemory pdim = new PostDataInMemory(s.length(), false, "text/html");

        byte buff[] = s.getBytes();

        for (byte b : buff) {
            pdim.put(b);
        }

        ByteBuffer bb = ByteBuffer.allocate(s.length());

        pdim.fillIn(bb, 0, s.length());

        String x = new String(bb.array(), 0, s.length());

        assertTrue(x.equals(s));
    }

    @Test
    public void testString() throws Exception {

        String s = "Woogie!";

        PostDataInMemory pdim = new PostDataInMemory(s.length(), false, "text/html");

        byte buff[] = s.getBytes();

        for (byte b : buff) {
            pdim.put(b);
        }

        String x = pdim.string(0, s.length());

        assertTrue(x.equals(s));

        ByteBuffer bb = ByteBuffer.allocate(s.length());

        pdim.fillIn(bb, 0, s.length());

        x = new String(bb.array(), 0, s.length());

        assertTrue(x.equals(s));

        final byte barr[] = new byte[s.length()];

        pdim.write( new OutputStream() {

                public void write( byte[] arr, int start, int length) {
                    System.arraycopy(arr, start, barr, 0, length);
                }

                public void write(int b) throws IOException {
                    throw new IOException("Shouldn't be called");
            }
        }, 0, s.length());


        assertTrue(s.equals(new String(barr)));
    }


    @Test
    public void testStreamWrite() throws Exception {

        String s = "Woogie!";

        PostDataInMemory pdim = new PostDataInMemory(s.length(), false, "text/html");

        byte buff[] = s.getBytes();

        for (byte b : buff) {
            pdim.put(b);
        }

        final byte barr[] = new byte[s.length()];

        pdim.write( new OutputStream() {

                public void write( byte[] arr, int start, int length) {
                    System.arraycopy(arr, start, barr, 0, length);
                }

                public void write(int b) throws IOException {
                    throw new IOException("Shouldn't be called");
            }
        }, 0, s.length());


        assertTrue(s.equals(new String(barr)));
    }

    @Test
    public void testWriteTo() throws Exception {

        String s = "Woogie!";

        PostDataInMemory pdim = new PostDataInMemory(s.length(), false, "text/html");

        byte buff[] = s.getBytes();

        for (byte b : buff) {
            pdim.put(b);
        }

        File f = null;

        try {
            f = File.createTempFile(this.getClass().getName(), ".dat");

            pdim.writeTo(f);

            FileInputStream fis = new FileInputStream(f);

            final byte barr[] = new byte[s.length() * 2];

            int len = fis.read(barr);

            assert(len == buff.length);

            for (int i = 0; i < buff.length; i++) {
                assertTrue(buff[i] == barr[i]);
            }
        }
        finally {
            try {
                if (f != null) {
                    f.delete();
                }
            }
            catch(Exception e) {
                // feh...
            }
        }
    }


    @Test
    public void testWrite() {

        PostDataInMemory pdim = new PostDataInMemory(1, false, "text/html");

        assertTrue(pdim._data[0] == 0);
        pdim.put((byte) 10);
        assertTrue(pdim._data[0] == 10);
        assertTrue(pdim.get(0) == 10);
        assertTrue(pdim.position() == 1);

        try {
            pdim.put((byte) 11);
            fail();
        }
        catch(Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertTrue(e.getMessage().startsWith("Error: attempt to write past end of buffer."));
        }
    }

    @Test
    public void testRead() {

        PostDataInMemory pdim = new PostDataInMemory(1, false, "text/html");

        assertTrue(pdim._data[0] == 0);
        pdim.put((byte) 10);
        assertTrue(pdim._data[0] == 10);
        assertTrue(pdim.get(0) == 10);
        assertTrue(pdim.position() == 1);

        try {
            pdim.get(1);
            fail();
        }
        catch(Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertTrue(e.getMessage().startsWith("Error: attempt to read past end of data."));
        }
    }

    @Test
    public void testMisc() {

        assertTrue(PostDataInMemory.MAX == PostDataInMemory.getMax());
    }

    @Test
    public void testInitialState() {

        PostDataInMemory pdim = new PostDataInMemory(129, false, "text/html");
        assertTrue(pdim.position() == 0);
        assertTrue(pdim._data.length == 129);
    }

    @Test
    public void testSize() {

        try {
            new PostDataInMemory(PostDataInMemory.getMax() + 1, false, "text/html");
            fail();
        }
        catch (RuntimeException e) {
            // ok
        }

        try {
            new PostDataInMemory(0, false, "text/html");
        }
        catch (RuntimeException e) {
            fail();
        }

        try {
            new PostDataInMemory(-1, false, "text/html");
            fail();
        }
        catch (RuntimeException e) {
            // ok
        }
    }
}
