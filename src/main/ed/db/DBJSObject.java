// DBJSObject.java

package ed.db;

import java.nio.*;
import java.util.*;

import ed.js.*;

import static ed.db.Bytes.*;
import static ed.MyAsserts.*;

public class DBJSObject implements JSObject {

    static boolean DEBUG = Boolean.getBoolean( "DEBUG.DBJSO" );

    DBJSObject( ByteBuffer buf ){
        this( buf , 0 );
        assertEquals( _end , _buf.limit() );
    }
    
    DBJSObject( ByteBuffer buf , int offset ){
        _buf = buf;
        _offset = offset;
        _end = _buf.getInt( _offset );
    }

    public Object get( Object n ){
        Element e = findElement( n.toString() );
        if ( e == null )
            return null;
        return e.getObject();
    }

    public JSFunction getFunction( String name ){
        return JSObjectBase.getFunction( this , name );
    }

    public Object getInt( int n ){
        return get( String.valueOf( n ) );
    }

    public Object setInt( int n , Object v ){
        throw new RuntimeException( "read only" );
    }

    public Object set( Object n , Object v ){
        throw new RuntimeException( "read only" );
    }

    public Object removeField( Object n ){
        throw new RuntimeException( "read only" );
    }

    public boolean containsKey( String s ){
        return findElement( s ) != null;
    }

    public Collection<String> keySet(){
        return keySet( true );
    }

    public Collection<String> keySet( boolean includePrototype ){    
        Set<String> keys = new HashSet<String>();
        
        ElementIter i = new ElementIter();
        while ( i.hasNext() ){
            Element e = i.next();
	    if ( e.eoo() )
		break;
            keys.add( e.fieldName() );
        }
        
        return keys;
    }

    public JSFunction getConstructor(){
        return null;
    }
    public JSObject getSuper(){
        return null;
    }

    String _readCStr( final int start ){
	return _readCStr( start , null );
    }
    
    String _readCStr( final int start , final int[] end ){
        int pos = 0;
        while ( _buf.get( pos + start ) > 0 ){
            _cStrBuf[pos] = _buf.get( pos + start );
            pos++;
        }
	if ( end != null && end.length > 0 )
	    end[0] = start + pos;
        return new String( _cStrBuf , 0 , pos );
    }

    String _readJavaString( final int start ){
	int size = _buf.getInt( start ) - 1;
	
	byte[] b = new byte[size];

	int old = _buf.position();
	_buf.position( start + 4 );
	_buf.get( b , 0 , b.length );
	_buf.position( old );
	
	try {
	    return new String( b , "UTF-8" );
	}
	catch ( java.io.UnsupportedEncodingException uee ){
	    return new String( b );
	}
    }

    /**
     * includes 0 at end
     */
    int _cStrLength( final int start ){
	int end = start;
	while ( _buf.get( end ) > 0 )
	    end++;
	return 1 + ( end - start );
    }

    Element findElement( String name ){
        ElementIter i = new ElementIter();
        while ( i.hasNext() ){
            Element e = i.next();
            if ( e.fieldName().equals( name ) )
                return e;
        }
        return null;
    }

    public String toString(){
        return "Object";
    }
    
    class Element {
        Element( final int start ){
            _start = start;
            _type = _buf.get( _start );
            _name = eoo() ? "" : _readCStr( _start + 1 );
            
	    if ( DEBUG ) System.out.println( "name [" + _name + "] type [" + _type + "]" );
	    
            int size = 1 + _name.length() + 1; // 1 for the start byte, end for end of string
            _dataStart = _start + size;

            switch ( _type ){
            case MAXKEY:
            case EOO:
            case UNDEFINED:
            case NULL:
                break;
            case BOOLEAN:
                size += 1;
                break;
            case DATE:
            case NUMBER:
                size += 8;
                break;
            case OID:
                size += 12;
                break;
            case REF:
                size += 12;
            case SYMBOL:
            case CODE:
            case STRING:
                size += 4 + _buf.getInt( _dataStart );
                break;
            case ARRAY:
            case OBJECT:
		size += _buf.getInt( _dataStart );
		break;
            case BINARY:
                size += 4 + _buf.getInt( _dataStart ) + 1;
                break;
            case REGEX:
		int first = _cStrLength( _dataStart );
		int second = _cStrLength( _dataStart + first );
		size += first + second;
		break;
            default:
                throw new RuntimeException( "can't handle type " + _type );
            }
            _size = size;
        }

        String fieldName(){
            return _name;
        }

        boolean eoo(){
            return _type == EOO || _type == MAXKEY;
        }
	
        int size(){
            return _size;
        }
	
        Object getObject(){
            switch ( _type ){
            case NUMBER:
                return _buf.getDouble( _dataStart );
	    case OID:
		return new ObjectId( _buf.getLong( _dataStart ) , _buf.getInt( _dataStart + 8 ) );
	    case SYMBOL:
	    case CODE:
		return ed.js.engine.Convert.makeAnon( _readJavaString( _dataStart ) );
	    case STRING:
		return _readJavaString( _dataStart );
	    case DATE:
		return new JSDate( _buf.getLong( _dataStart ) );
	    case REGEX:
		int[] endPos = new int[1];
		String first = _readCStr( _dataStart , endPos );
		return new JSRegex( first , _readCStr( 1 + endPos[0] ) );
	    case BINARY:
		throw new RuntimeException( "can't inspect binary in db" );
	    case BOOLEAN:
		return _buf.get( _dataStart ) > 0;
	    case ARRAY:
                // TODO: need to be smarter here
	    case OBJECT:
		return new DBJSObject( _buf , _dataStart );
	    case NULL:
            case EOO:
	    case MAXKEY:
	    case UNDEFINED:
                return null;
            }
            throw new RuntimeException( "can't decode type " + _type );
        }

        final int _start;
        final byte _type;
        final String _name;
        final int _dataStart;
        final int _size;
    }
    
    class ElementIter {
        
        ElementIter(){
            _pos = _offset + 4;
        }
        
        boolean hasNext(){
            return ! _done && _pos < _buf.limit();
        }
        
        Element next(){
            Element e = new Element( _pos );
            _done = e.eoo();
                
            _pos += e.size();
            return e;
        }
        
        int _pos;
        boolean _done = false;
    }

    final ByteBuffer _buf;
    final int _offset;
    final int _end;
    final byte[] _cStrBuf = new byte[1024];
}
