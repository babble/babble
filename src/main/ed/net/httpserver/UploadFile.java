// UploadFile.java

package ed.net.httpserver;

public class UploadFile {
    UploadFile( String filename , String contentType , PostData pd , int start , int end ){
        _filename = filename;
        _contentType = contentType;
        _data = pd;
        _start = start;
        _end = end;
    }

    public String string(){
        return _data.string( _start , _end - _start );
    }

    public String toString(){
        return "{UploadFile.  _filename:" + _filename + " _contentType:" + _contentType + " _start:" + _start + " _end:" + _end + "}";
    }

    final String _filename;
    final String _contentType;
    final PostData _data;
    final int _start;
    final int _end;
        
}
