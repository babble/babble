// TypeInference.java

package ed.js.engine;

import org.mozilla.javascript.*;

import java.util.*;

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
            _vars.put( fn.getParamOrVarName( i ) , new Info( fn.getParamOrVarName( i ) ) );
        
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
                
                if ( cur.getString().equals( "arguments" ) || cur.getString().equals( "processArgs" ) )
                    _usesArguemnts = true;
                if ( cur.getString().equals( "scope" ) )
                    _usesScope = true;
            }
            else if ( cur.getType() == Token.INC ||
                      cur.getType() == Token.DEC ){
                
                if ( cur.getFirstChild().getType() == Token.GETVAR || 
                     cur.getFirstChild().getType() == Token.NAME ){
                    
                    _vars.incOrDec( cur.getFirstChild().getString() );
                    
                }
                
            }
            else if ( cur.getType() == Token.SETVAR ){
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

        Info( String name ){
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

    private boolean _played = false;

    final boolean _hasLambda;
    private boolean _usesArguemnts = false;
    private boolean _usesScope = false;
    
}
