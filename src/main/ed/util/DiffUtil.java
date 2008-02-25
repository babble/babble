// DiffUtil.java

package ed.util;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import bmsi.util.*;


public class DiffUtil {

    /**
     * @return diff to get back to a from b
     */
    public static String computeDiff( String a , String b ){

	if ( a == null )
	    a = "";
	if ( b == null )
	    b = "";
	
	String aSplit[] = a.split("\n");
	String bSplit[] = b.split("\n");
	
	Diff d = new Diff( aSplit , bSplit );
	DiffPrint.Base p = new DiffPrint.NormalPrint( aSplit , bSplit );
	StringWriter out = new StringWriter();
	p.setOutput(out);
	p.print_script( d.diff_2(false) );
	return out.toString();
    }

    public static String applyScript( String base , String script ){
        List<Command> commands = parseScript( script );
        List<String> lines = stringToLines( base );

        for ( int i=commands.size()-1; i>=0 ; i-- ){
            Command c = commands.get(i);
            c.applyBackwards( lines );
            
        }

        StringBuilder buf = new StringBuilder( base.length() );
        for ( String l : lines )
            buf.append( l );
        return buf.toString().trim();
    }


    static List<Command> parseScript( String script ){

        List<String> scriptLines = stringToLines( script );

        List<Command> commands = new ArrayList();
        Command current = null;
        for ( int i=0; i<scriptLines.size(); i++ ){
            String line = scriptLines.get(i);

            Matcher m = command.matcher( line );
            if ( m.find() ){
                current = new Command( m );
                commands.add( current );
            }
            else {
                if ( current == null ){
                    throw new RuntimeException("this should be command:" + line );
                }
                current.addLine( line );
            }
        }
        return commands;
    }

    final static Pattern EOL = Pattern.compile("([\n])");
    static List stringToLines( String s ){
        List<String> l = new ArrayList<String>();
        Matcher m = EOL.matcher( s );
        
        int prev = 0;
        while ( m.find() ){
            String temp = s.substring( prev , m.end()  );
            l.add( temp );
            prev = m.end();
        }

        if ( prev < s.length() ){
            String temp = s.substring( prev );
            l.add( temp );
        }

        return l;
    }


    static final Pattern command = Pattern.compile("^(\\d+),?(\\d*)([adc])(\\d+),?(\\d*)$");

    static class Command {
        public Command( Matcher m ){
            _f1_StartRange = Integer.parseInt( m.group(1) );
            _command = m.group(3).charAt(0);
            _f2_StartRange = Integer.parseInt( m.group(4) );
            if ( m.group(5) != null ){
                _f2_EndRange = StringParseUtil.parseInt( m.group(5) , -1 );
            }
            if ( m.group(2) != null ){
                _f1_EndRange = StringParseUtil.parseInt( m.group(2) , -1 );
            }
        }

        public void addLine( String line ){
            if ( line.startsWith("> ") )
                _newLines.add( line.substring(2) );
            if ( line.startsWith("< ") )
                _oldLines.add( line.substring(2) );
        }

        public void applyBackwards( List lines ){
            //System.err.println("running: " + this.toString() );
            switch ( _command ){
            case 'c':
                if (lines.size() < _f2_StartRange)     // Should never happen... but it does.
                    {
                        for (int nMissingLines = lines.size(); nMissingLines <= _f2_StartRange; nMissingLines++)
                            {
                                lines.add( nMissingLines, "Missing Line\r\n" );
                            }
                    }

                lines.set( _f2_StartRange - 1 , _oldLines.get(0) );
                if ( _f2_EndRange > 0 ){
                    //System.err.println("a");
                    for ( int i=0; i < ( _f2_EndRange - _f2_StartRange ) ; i++ )
                        if (_f2_StartRange < lines.size())
                            lines.remove( _f2_StartRange );
                }
                if ( _f1_EndRange > 0 ){
                    //System.err.print("b");
                    for ( int i=1; i<_oldLines.size(); i++ )
                        lines.add( _f2_StartRange - 1 + i , _oldLines.get(i) );
                }
                return;
            case 'a':
                for ( int i=0; i<_newLines.size() ; i++ ){
                    //System.err.println("asd:" + lines.size());
                    if (_f2_StartRange-1 >= lines.size())              // Should never happen.
                        return;
                    lines.remove( _f2_StartRange - 1);
                }
                return;
            case 'd':
                for ( int i=0; i<_oldLines.size(); i++ ){
                    if (lines.size() > _f2_StartRange + i)
                        lines.add( _f2_StartRange + i , _oldLines.get(i) );
                    else
                        lines.add( _oldLines.get(i) );  // Not sure about this, but it fixes crash.
                }
                return;
            default:
                throw new RuntimeException("duunno");
            }
        }

        public String toString(){
            StringBuffer buf = new StringBuffer();
            buf.append( _f1_StartRange );
            if ( _f1_EndRange > 0 )
                buf.append(",").append(_f1_EndRange);
            buf.append( _command ).append( _f2_StartRange );
            if ( _f2_EndRange > 0 )
                buf.append( "," ).append( _f2_EndRange );
            buf.append("\n");
            for ( int i=0; i<_oldLines.size(); i++){
                buf.append("< ").append( explain( (String)_oldLines.get(i) ) ).append("\n");
            }
            if ( _oldLines.size() > 0 && _newLines.size() > 0 )
                buf.append("----\n");
            for ( int i=0; i<_newLines.size(); i++){
                buf.append("> ").append( explain( (String)_newLines.get(i) ) ).append("\n");
            }
            return buf.toString();
        }

        //
        // Better to String.
        public String toBetterString(){
            StringBuffer buf = new StringBuffer();
            buf.append( _f1_StartRange );
            if ( _f1_EndRange > 0 )
                buf.append(",").append(_f1_EndRange);
            buf.append( _command ).append( _f2_StartRange );
            if ( _f2_EndRange > 0 )
                buf.append( "," ).append( _f2_EndRange );
            buf.append("\n");

            if (_command == 'c')
                {
                    buf.append( "<b>Change Command</b>\n" );

                    buf.append( "\tStarting at line :" + _f1_StartRange + "\n" );
                    buf.append( "\tGoing to line    :" + (_f2_EndRange > 0 ? _f2_EndRange : _f1_StartRange) + "\n\n" );

                    buf.append( "Old Lines:\n" );
                    for ( int i=0; i<_oldLines.size(); i++){
                        buf.append( explain( (String)_oldLines.get(i) ) ).append("\n");
                    }
                    buf.append("----\n");

                    buf.append( "Changed Lines:\n" );
                    for ( int i=0; i<_newLines.size(); i++){
                        buf.append( explain( (String)_newLines.get(i) ) ).append("\n");
                    }
                }
            else if (_command == 'a')
                {
                    buf.append( "<b>Add Command</b>\n" );
                    buf.append( "\tStarting after line :" + _f1_StartRange + "\n" );
                    buf.append( "\tGoing through line  :" + (_f2_EndRange > 0 ? _f2_EndRange : _f1_StartRange) + "\n\n" );

                    buf.append( "Added Lines:\n" );
                    for ( int i=0; i<_newLines.size(); i++){
                        buf.append( explain( (String)_newLines.get(i) ) ).append("\n");
                    }
                }
            else if (_command == 'd')
                {
                    buf.append( "<b>Delete Command</b>\n" );
                    buf.append( "\tStarting at line   :" + (_f1_StartRange) + "\n" );
                    buf.append( "\tGoing through line :" + (_f1_EndRange > 0 ? _f1_EndRange : _f1_StartRange) + "\n\n" );

                    for ( int i=0; i<_oldLines.size(); i++){
                        buf.append( explain( (String)_oldLines.get(i) ) ).append("\n");
                    }
                }

            String result = buf.toString();
            result = result.replaceAll("EON", "<font color=\"#0066FF\">\\\\n</font>");
            result = result.replaceAll("EOR", "<font color=\"#0066FF\">\\\\r</font>");

            return( result );
        }

        char _command;

        int _f1_StartRange;
        int _f1_EndRange;

        int _f2_StartRange;
        int _f2_EndRange = -1;

        List _newLines = new ArrayList();
        List _oldLines = new ArrayList();

    }

    static String explain( String s ){
        return s.replaceAll("\r","EOR").replaceAll("\n","EON" );
    }
}
