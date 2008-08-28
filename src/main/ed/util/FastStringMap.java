// FastStringMap.java

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

import java.util.*;

import ed.js.*;

public final class FastStringMap implements Map<String,Object> {

    public FastStringMap(){
        this( 16 );
    }
    
    public FastStringMap( int initSize ){
        _data = new MyEntry[ initSize ];
    }


    // -
        
    public Object put( final String key, final Object value ){
        return put( key.hashCode() , key , value );
    }

    public Object put( final int hash , final String key, final Object value){
        final MyEntry e = _getEntry( hash , key , true );
        final Object old = e._value;
        e._value = value;
        if ( e._deleted ){
            e._deleted = false;
            _size++;
        }
        return old;
    }

    public Object get( final Object keyObj ){
        return get( keyObj.hashCode() , keyObj.toString() );
    }
    
    public Object get( final String key ){
        return get( key.hashCode() , key );
    }

    public Object get( final int hash , final String key ){
        final MyEntry e = _getEntry( hash , key , false , _data , null );
        
        if ( e == null || e._deleted )
            return null;
        
        return e.getValue();
    }

    public Object remove( final Object keyObj ){
        return remove( keyObj.hashCode() , keyObj.toString() );
    }

    public Object remove( final String key ){
        return remove( key.hashCode() , key );
    }
    
    public Object remove( final int hash , final String key ){
        final MyEntry e = _getEntry( hash , key , false );
        if ( e == null || e._deleted )
            return null;
        
        _size--;
        e._deleted = true;
        final Object old = e._value;
        e._value = null;
        return old;
    }
    
    public boolean containsKey( final Object key ){
        return containsKey( key.hashCode() , key.toString() );
    }

    public boolean containsKey( final String key ){
        return containsKey( key.hashCode() , key );
    }
    
    public boolean containsKey( final int hash , final String key ){
        final MyEntry e = _getEntry( hash , key , false , _data , null );
        return e != null && ! e._deleted;
    };

    public Set<String> keySet(){
        return keySet( false );
    }
    
    public Set<String> keySet( boolean myCopy ){
        Set<String> s = new HashSet<String>( _data.length );
        for ( int i=0; i<_data.length; i++ ){
            MyEntry e = _data[i];
            if ( e == null || e._deleted )
                continue;
            s.add( e._key );
        }
        return s;
    }

    // -
    
    static final class MyEntry implements Map.Entry<String,Object>{
        MyEntry( int hash , String key  ){
            _hash = hash;
            _key = key;
        }

        public boolean equals( int hash , String key){
            return _hash == hash && _key.equals( key );
        }

        public boolean equals(Object o){
            return this == o;
        }

        public String getKey(){
            return _key;
        }
        
        public Object getValue(){
            return _value;
        }

        public int hashCode(){
            return _hash;
        }
        
        public Object setValue( Object value){
            final Object old = _value;
            _value = value;
            return old;
        }

        public String toString(){
            return _key + ":" + _value;
        }

        final int _hash;
        final String _key;

        private Object _value;
        private boolean _deleted = false;
    }

    public void debug(){
        StringBuilder buf = new StringBuilder();
        debug( buf );
        System.out.println( buf );
    }
    
    public void debug( StringBuilder a ){
        a.append( "[ " );
        for ( int i=0; i<_data.length; i++ ){
            if ( i > 0 )
                a.append( " , " );
            if ( _data[i] == null || _data[i]._deleted )
                continue;
            a.append( "\"" ).append( _data[i]._key ).append( "\" " );
        }
        a.append( "]" );
    }
    
    public void clear(){
        for ( int i=0; i<_data.length; i++ )
            _data[i] = null;
    } 

    public Set<Map.Entry<String,Object>> entrySet(){
        // TODO: make a lot faster
        Set<Map.Entry<String,Object>> set = new HashSet<Map.Entry<String,Object>>( _data.length );
        for ( int i=0; i<_data.length; i++ ){
            if ( _data[i] == null || _data[i]._deleted )
                continue;
            set.add( _data[i] );
        }
        return set;
    }
    
    private MyEntry _getEntry( final int hash , final String key , final boolean create ){
        if ( key == null )
            throw new NullPointerException( "key is null " );        

        while( true ){
            final MyEntry e = _getEntry( hash , key , create , _data , null );
            if ( e != null || ! create )
                return e;

            grow();
        }
    }

    int _indexFor( int h , int max ){
        
        
        if ( _slow ){
            if ( h < 0 )
                h = h * -1;
            return h % max;
        }
            
        h += ~(h << 9);
        h ^=  (h >>> 14);
        h +=  (h << 4);
        h ^=  (h >>> 10);
        
        h = h & ( max - 1 );

        return h;
    }
    
    private final MyEntry _getEntry( final int hash , final String key , final boolean create , final MyEntry[] data , final MyEntry toInsert ){
        int cur = _indexFor( hash , data.length );
        
        for ( int z=0; z<_maxChainLength; z++ ){
            
            if ( data[cur] == null ){
                
                if ( ! create )
                    return null;

                if ( toInsert == null ){
                    _size++;
                    data[cur] = new MyEntry( hash , key );
                    return data[cur];
                }
                
                data[cur] = toInsert;
                return data[cur];
            }
            
            if ( data[cur].equals( hash , key ) )
                return data[cur];

            cur++;
            if ( cur == data.length )
                cur = 0;
        }

        return null;
    }

    private void grow(){
        grow( (int)(_data.length * 1.5) );
    }
    
    private void grow( int size ){
        
        _maxChainLength++;
        
        tries:
        for ( int z=0; z<20; z++ ){
            
            if ( size > 1000 )
                _slow = true;

            MyEntry[] newData = new MyEntry[ size ];
            for ( int i=0; i<_data.length; i++ ){
                MyEntry e = _data[i];
                if ( e == null || e._deleted )
                    continue;
                MyEntry n = _getEntry( e._hash , e._key , true , newData , e );
                if ( n == null ){

                    if ( z % 2 == 0 )
                        size *= 1.5;
                    else
                        _maxChainLength ++;
                    
                    continue tries;
                }
                if ( n != e ) throw new RuntimeException( "something broke" );
            }
            _data = newData;
            return;
        }
        
        throw new RuntimeException( "grow failed" );
        
    }

    public long approxSize( IdentitySet seen ){
        if ( seen == null )
            seen = new IdentitySet();
        else if ( seen.contains( this ) )
            return 0;
        
        seen.contains( this );

        long size = 48 + ( 8 * _data.length );
        for ( int i=0; i<_data.length; i++ ){

            MyEntry e = _data[i];
            if ( e == null )
                continue;
            
            size += 32;
            size += JSObjectSize.size( e._key , seen );
            size += JSObjectSize.size( e._value , seen );
        }

        return size;
    }

    public int size(){
        return _size;
    } 

    public boolean isEmpty(){
        return _size == 0;
    }

    private int _size = 0;
    private MyEntry[] _data;
    private int _maxChainLength = 1;
    private boolean _slow = false;

    // -----------------

    public boolean containsValue(Object value){
        throw new UnsupportedOperationException();
    }
        
    public boolean equals(Object o){
        throw new UnsupportedOperationException();
    }

    public int hashCode(){
        throw new UnsupportedOperationException();
    }
    
    public void putAll( Map<? extends String,? extends Object> other ){

        if ( other == null || other.size() == 0 )
            return;
        
        // TODO: we can do some cool fast stuff here later

        for ( String s : other.keySet() ){
            put( s , other.get( s ) );
        }
    }

    public Collection<Object> values(){
        throw new UnsupportedOperationException();
    }
    
}
