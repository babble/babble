// TextSimpleConfig.java

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

import ed.io.*;

/**
   ---
<type> <name>
    <key> : <value>

<type> <name>
    <value>
    <value>

<type> <name> <value>

   ---
   
 */
public class TextSimpleConfig implements SimpleConfig {

    public static TextSimpleConfig readString( String s ){
        try {
            return read( new LineReader( new StringReader( s ) ) );
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "should be impossible" , ioe );
        }
    }

    public static TextSimpleConfig read( File f )
        throws IOException {
        return read( new FileInputStream( f ) );
    }

    public static TextSimpleConfig read( InputStream in )
        throws IOException {
        return read( new LineReader( in ) );
    }

    public static TextSimpleConfig read( LineReader in )
        throws IOException {
        
        TextSimpleConfig sc = new TextSimpleConfig();
        
        String type = null;
        String name = null;
        

        for ( String line : in ){

            if ( line.trim().length() == 0 )
                continue;

            if ( Character.isWhitespace( line.charAt( 0 ) ) ){
                // have an internal line
                
                line = line.trim();
                int idx = line.indexOf( ": " );
                if ( idx < 0 )
                    idx = line.indexOf( " " );
                
                if ( idx < 0 ){ // value
                    sc.addValue( type , name , line );
                }
                else { // entry
                    sc.addEntry( type , name , line.substring( 0 , idx ).trim() , line.substring( idx + 1 ).trim() );
                }

                continue;
            }

            String pcs[] = line.trim().split( " +" );
            if ( pcs.length < 2 || pcs.length > 3 )
                throw new IllegalArgumentException( "invalid base line [" + line + "]" );
            
            type = pcs[0].trim();
            name = pcs[1].trim();

            if ( pcs.length == 3 )
                sc.addValue( type , name , pcs[2] );
        }
        
        return sc;
    }

    public static String outputToString( SimpleConfig config ){
        StringBuilder buf = new StringBuilder();
        try {
            outputToString( config, buf );
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "should be impossible" , ioe );
        }
        return buf.toString();
    }
    
    public static void outputToString( SimpleConfig config , Appendable out )
        throws IOException {

        for ( String type : config.getTypes() ){
            for ( String name : config.getNames( type ) ){
                
                out.append( type ).append( " " ).append( name );

                if ( config.isMap( type , name ) ){
                    Map<String,String> m = config.getMap( type , name );
                    
                    out.append( "\n" );
                    for ( String key : m.keySet( ))
                        out.append( "    " ).append( key ).append( " : " ).append( m.get( key ) ).append( "\n" );
                }
                else if ( config.isValue( type , name ) ){
                    List<String> values = config.getValues( type , name );
                    if ( values.size() == 1 )
                        out.append( " " ).append( values.get( 0 ) ).append( "\n" );
                    else {
                        out.append( "\n" );
                        for ( String value : values )
                            out.append( "    " ).append( value ).append( "\n" );
                    }
                }
                
                out.append( "\n" );
            }
        }
        
    }

    /**
     * create a new empty SimpleConfig
     */
    public TextSimpleConfig(){

    }

    public void addEntry( String type , String name , String key , String value ){
        getType( type , true ).addEntry( name , key , value );
    }

    public void addValue( String type , String name , String value ){
        getType( type , true ).addValue( name , value );
    }

    public Map<String,String> getMap( String type , String name ){
        Type t = getType( type , false );
        if ( t == null )
            return EMPTY_MAP;
        return t.getMap( name );
    }

    public List<String> getValues( String type , String name ){
        Type t = getType( type , false );
        if ( t == null )
            return EMPTY_LIST;
        return t.getValues( name );
    }

    public List<String> getTypes(){
        List<String> l = new ArrayList<String>( _types.keySet() );
        Collections.sort( l );
        return l;
    }
    
    public List<String> getNames( String type ){
        Type t = getType( type , false );
        if ( t == null )
            return EMPTY_LIST;

        List<String> l = new ArrayList<String>( t.keySet() );
        Collections.sort( l );
        return l;
    }

    public boolean isMap( String type , String name ){
        Type t = getType( type , false );
        if ( t == null )
            return false;
        return t.isMap( name );
    }
    
    public boolean isValue( String type , String name ){
        Type t = getType( type , false );
        if ( t == null )
            return false;
        return t.isValue( name );
    }

    public String outputToString(){
        return outputToString( this );
    }
    
    public String toString(){
        return outputToString( this );
    }

    // ---

    Type getType( String type , boolean create ){
        Type t = _types.get( type );
        if ( t != null || ! create )
            return t;
        
        t = new Type( type );
        _types.put( type , t );
        return t;
    }
    
    class Type extends TreeMap<String,Object>{

        Type( String type ){
            _type = type;
        }

        Map<String,String> getMap( String name ){
            Object foo = get( name );
            if ( foo == null )
                return EMPTY_MAP;

            if ( foo instanceof Map )
                return (Map<String,String>)foo;

            throw new RuntimeException( _type + "." + name + " is not a map" );
        }

        List<String> getValues( String name ){
            Object foo = get( name );
            if ( foo == null )
                return EMPTY_LIST;

            if ( foo instanceof List )
                return (List<String>)foo;

            throw new RuntimeException( _type + "." + name + " is not a list" );
        }

        boolean isMap( String name ){
            return get( name ) instanceof Map;
        }

        boolean isValue( String name ){
            return get( name ) instanceof List;
        }

        void addEntry( String name , String key , String value ){
            Object foo = get( name );
            if ( foo != null && ! ( foo instanceof Map ) )
                throw new IllegalArgumentException( _type + "." +  name + " not in map mode" );

            if ( foo == null ){
                foo = new TreeMap<String,String>();
                put( name , foo );
            }

            ((Map)foo).put( key , value );
        }


        void addValue( String name , String value ){
            Object foo = get( name );
            if ( foo != null && ! ( foo instanceof List ) )
                throw new IllegalArgumentException( _type + "." + name + " not in value mode" );

            if ( foo == null ){
                foo = new ArrayList<String>();
                put( name , foo );
            }

            ((List)foo).add( value );
        }

        final String _type;
    }
    
    private Map<String,Type> _types = new TreeMap<String,Type>();
    
    private static final List<String> EMPTY_LIST = Collections.unmodifiableList( new LinkedList<String>() );
    private static final Map<String,String> EMPTY_MAP = Collections.unmodifiableMap( new TreeMap<String,String>() );
    
}
