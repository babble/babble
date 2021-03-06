// TypeInference.java

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

package ed.js.engine;

import java.util.*;

import ed.ext.org.mozilla.javascript.*;
import ed.util.*;

public class FunctionInfo implements Iterable<String> {

    static FunctionInfo create( FunctionNode fn ){
        FunctionInfo fi = new FunctionInfo( fn );
        fi._play();
        //System.out.println( fi );
        return fi;
    }

    private FunctionInfo( FunctionNode fn ){
        _fn = fn;
        _vars = new VarSet();

        for ( int i=0; i<fn.getParamAndVarCount(); i++ )
            _vars.put( fn.getParamOrVarName( i ) , new Info( _vars , fn.getParamOrVarName( i ) ) );
        
        for ( int i=0; i<fn.getParamCount(); i++ )
            _vars.get( fn.getParamOrVarName( i ) )._param = true;
        
        _hasLambda = fn.getFunctionCount() > 0;
    }

    boolean usesArguemnts(){
        _play();
        return _usesArguemnts;
    }

    boolean usesScope(){
        _play();
        return _usesScope;
    }

    Info getInfo( String s ){
        return _vars.get( s );
    }

    boolean canUseLocal( String s ){
        if ( _hasLambda || _usesScope )
            return false;
        
        Info i = getInfo( s );
        if ( i == null )
            return false;
        
        return i.canUseLocal();
    }

    boolean isNumber( Node n ){
        if ( n.getType() == Token.NUMBER )
            return true;
        
        if ( n.getType() == Token.ADD )
            return 
                isNumber( n.getFirstChild() ) && 
                isNumber( n.getFirstChild().getNext() );

        if ( n.getType() == Token.GETVAR || n.getType() == Token.NAME )
            return isNumber( n.getString() );
        
        return false;
    }

    boolean isNumber( String s ){
        Info i = getInfo( s );
        if ( i == null )
            return false;
        return i.isNumber();
    }
    
    public Iterator<String> iterator(){
        return _vars.keySet().iterator();
    }

    private void _play(){
        if ( _played )
            return;
        
        for ( Iterator<Node> ni = NodeUtil.childIterator( _fn ); ni.hasNext(); ){
            Node cur = ni.next();
            
            if ( cur.getType() == Token.NAME ||
                 cur.getType() == Token.GETVAR ){
                
                String name = cur.getString();

                if ( name.equals( "arguments" ) || name.equals( "processArgs" ) )
                    _usesArguemnts = true;
                if ( name.equals( "scope" ) )
                    _usesScope = true;

                if ( ! _vars.containsKey( name ) )
                    _globals.add( name );
                
            }
            else if ( cur.getType() == Token.INC ||
                      cur.getType() == Token.DEC ){
                
                if ( cur.getFirstChild().getType() == Token.GETVAR || 
                     cur.getFirstChild().getType() == Token.NAME ){
		    
                    _vars.incOrDec( cur.getFirstChild().getString() );
                    
                }
                
            }
	    else if ( cur.getType() == Token.FOR && Convert.countChildren( cur ) == 3 ){
		_vars.unknownEvidence( cur.getFirstChild().getString() );
	    }
            else if ( cur.getType() == Token.SETVAR || cur.getType() == Token.SETNAME ){
                _vars.settingTo( cur.getFirstChild().getString() , cur.getFirstChild().getNext() );
            }
        }
        _played = true;
    }


    static class VarSet extends TreeMap<String,Info> {

        boolean isNumber( String name ){
            Info i = get( name );
            if ( i == null )
                return false;
            return i.isNumber();
        }
        
        void numberEvidence( String name ){
            Info i = get( name );
            if ( i == null )
                return;
            i.numberEvidence();
        }

        void incOrDec( String name ){
            Info i = get( name );
            if ( i == null )
                return;
            i.incOrDec();
        }

        void unknownEvidence( String name ){
            Info i = get( name );
            if ( i == null )
                return;
            i.unknownEvidence();
        }

        void settingTo( String name , Node n ){
            Info i = get( name );
            if ( i == null )
                return;
            i.settingTo( n );
        }

        public String toString(){
            StringBuilder buf = new StringBuilder( "VarSet\n" );
            for ( String s : keySet() ){
                buf.append( "\t" ).append( get( s ) ).append( "\n" );
            }
            return buf.toString();
        }
    }

    static class Info {

        Info( VarSet vars , String name ){
            _vars = vars;
            _name = name;
        }

        void incOrDec(){
            numberEvidence();
            _incOrDec = true;
        }
        
        void numberEvidence(){
            _numberEvidence = true;
        }

        void unknownEvidence(){
            _unknownEvidence = true;
        }

        boolean isNumber(){
            return isNumber( new IdentitySet() );
        }

        boolean isNumber( IdentitySet seen ){
            for ( Info i : _depends ){
                if ( seen.contains( i ) )
                    continue;
                seen.add( i );
                if ( ! i.isNumber( seen ) ){
                    return false;
                }
            }
            
            return 
                ! _param && 
                _numberEvidence &&
                ! _unknownEvidence;
        }

        void settingTo( Node n ){
            if ( n.getType() == Token.NUMBER ){
                numberEvidence();
                return;
            }

            if ( n.getType() == Token.ADD ){
                settingTo( n.getFirstChild() );
                settingTo( n.getFirstChild().getNext() );
                return;
            }
            
            if ( n.getType() == Token.GETVAR || n.getType() == Token.NAME ){
                String s = n.getString();
                if ( s.equals( _name ) )
                    return;
                
                if ( _vars.containsKey( s ) ){
                    _depends.add( _vars.get( s ) );
                    return;
                }
                
                // TODO: look elsewhere
                unknownEvidence();
                return;
            }

            // TODO: more
            unknownEvidence();
        }
        
        boolean canUseLocal(){
            if ( isNumber() )
                return true;
            return ! _incOrDec;
        }

        public String toString(){
            return _name + " isNumber:" + isNumber() + " incOrDec:" + _incOrDec;
        }

        final String _name;
        final VarSet _vars;
        
        final List<Info> _depends = new LinkedList<Info>();

        boolean _param = false;
        boolean _incOrDec = false;
        boolean _numberEvidence = false;
        boolean _unknownEvidence = false;
    }

    static void _warn( String s ){
        System.out.println( "TypeInference.warn : " + s );
    }

    public String toString(){
        return 
            "FunctionInfo: " + _fn + "\n" +
            "\t _hasLambda: " + _hasLambda + "\n" +
            _vars;
        
    }

    final FunctionNode _fn;
    final VarSet _vars;
    final Set<String> _globals = new HashSet<String>();
    
    private boolean _played = false;

    final boolean _hasLambda;
    private boolean _usesArguemnts = false;
    private boolean _usesScope = false;
    
}
