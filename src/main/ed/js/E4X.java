// E4X.java

package ed.js;

import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import ed.js.func.*;
import ed.js.engine.*;
import ed.util.*;

public class E4X {
    
    public static final JSFunctionCalls1 CONS = new JSFunctionCalls1(){
	    public JSObject newOne(){
		return new ENode();
	    }
	    
	    public Object call( Scope scope , Object str , Object [] args){	
		ENode e = (ENode)scope.getThis();
		e.init( str.toString() );
		return e;
	    }
	};

    static class ENode extends JSObjectBase {
        private ENode(){}
        
        private ENode( Node n ){
            _node = n;
        }
        
        void init( String s ){
            _raw = s;
            try {
                _document = XMLUtil.parse( s );
                _node = _document.getDocumentElement();
            }
            catch ( Exception e ){
                throw new RuntimeException( "can't parse : " + e );
            }
        }
        
        public Object get( Object n ){
            if ( n == null )
                return null;
            
            if ( n instanceof String || n instanceof JSString ){
                String s = n.toString();
                
                if ( s.startsWith( "@" ) ){
                    return ((Element)_node).getAttribute( s.substring(1) );
                }
                
                List<Node> lst = new ArrayList<Node>();
                NodeList children = _node.getChildNodes();
                for ( int i=0; i<children.getLength(); i++ ){
                    Node c = children.item(i);
                    
                    if ( c.getNodeName().equals( s ) ){
                        lst.add( c );
                    }
                }
                
                if ( lst.size() == 0 )
                    return null;
            
                if ( lst.size() == 1 )
                    return new ENode( lst.get(0) );
                
                return new EList( lst );
            }
            
            throw new RuntimeException( "can't handle : " + n.getClass() );
        }


        
        public String toString(){
            if ( _raw != null )
                return _raw;
            
            if ( _node == null )
                return null;
            
            if ( _node.getChildNodes() != null &&
                 _node.getChildNodes().getLength() == 1 &&
                 _node.getChildNodes().item(0) instanceof CharacterData )
                return XMLUtil.toString( _node.getChildNodes().item(0) );
            
            return XMLUtil.toString( _node );
        }

        private String _raw;
        private Document _document;
        
        private Node _node;
    }
    
    static class EList extends JSObjectBase {
        private EList( List<Node> lst ){
            _lst = lst;
        }
        
        public Object get( Object n ){
            if ( n == null )
                return null;

            if ( n instanceof Number )
                return child( ((Number)n).intValue() );
            
            if ( n instanceof JSString || n instanceof String ){
                String s = n.toString();
            
                if ( s.equals( "length" ) || 
                     s.equals( "toString" ) ||
                     s.equals( "child" ) )
                    return null;
            }
            
            throw new RuntimeException( "can't handle [" + n + "] from a list" );
        }
        
        public ENode child( int num ){
            return new ENode( _lst.get( num ) );
        }

        public int length(){
            if ( _lst == null )
                return -1;
            return _lst.size();
        }
        
        public String toString(){
            StringBuilder buf = new StringBuilder();
            for ( Node n : _lst )
                XMLUtil.append( n , buf , 0 );
            return buf.toString();
        }

        final private List<Node> _lst;
    }
}
