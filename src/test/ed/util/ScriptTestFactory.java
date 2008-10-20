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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import ed.js.JSFunction;
import ed.js.engine.Scope;


/**
 * Factory to create tests for each JS test we have.  
 * Will be invoked by TestNG framework and make a test for each .js file
 */
public class ScriptTestFactory {
    
    public static List<String> excludeList = new ArrayList<String>();
    public static String baseDir = "";

    static void addExcludeFiles( File f ) throws IOException {
        if( f.isDirectory() ) {
            for( File aFile : f.listFiles() ) {
                addExcludeFiles( aFile );
            }
        }
        else {
            excludeList.add( f.getCanonicalPath() );
        }
    }

    static {
        try {
            String root= System.getProperty("TESTNG:CODE_ROOT", "/data");
            baseDir = (new File( root + "/qa/modules/mozilla/" )).getCanonicalPath();
            Scanner sc = new Scanner( new File( baseDir + File.separator + "exclude" ) );
            while( sc.hasNext() ) {
                String s = sc.next();
                if( s.startsWith( "#" ) ) {
                    sc.nextLine();
                    continue;
                }
                addExcludeFiles( new File( baseDir + File.separator + s ) );
            }
        }
        catch( Exception e ) {
            // catch IO and FileNotFound exceptions
            // this is fine, exclude list will be empty
        }
    }

    
    /**
     *  Creates an array of ScriptTestInstance objects, each representing
     *  one script file in whatever directory we're targeted at
     */
    @Parameters({ "test-class", "dir-name", "file-ending", "inc-regex", "ex-regex", "recursive"})
    @Factory
    public Object[] createTestInstances(String testClassName, String dirName, String fileEnding, 
                                        @Optional("") String inclusionRegex,
                                        @Optional("") String exclusionRegex,
                                        @Optional("false") String recursive) throws Exception {


        List<ScriptTestInstance> list = new ArrayList<ScriptTestInstance>();

        if (testClassName == null || testClassName.length() == 0) { 
            list.add(new FrameworkMisconfigTestInstance("test class name unspecified"));
            return list.toArray();
        }        
        
        /*
         *   ensure that the specified class exists and is useful
         */
        try { 
            ScriptTestInstance i = (ScriptTestInstance) Class.forName(testClassName).newInstance();
        }
        catch(ClassCastException cce) {
            list.add(new FrameworkMisconfigTestInstance("specified test class not a ScriptTestInstance?" + cce));
            return list.toArray();
        }
        
        if (dirName == null || dirName.length() == 0) { 
            list.add(new FrameworkMisconfigTestInstance("test directory unspecified"));
            return list.toArray();
       }

        if (fileEnding == null || 
            (fileEnding.length() == 0 && inclusionRegex.length() == 0)) { 
            list.add(new FrameworkMisconfigTestInstance("no inclusion criteria given"));
            return list.toArray();
        }
        
        if (!dirName.startsWith("/")) {
        	String s= System.getProperty("TESTNG:CODE_ROOT", "/data/");

        	if (!s.endsWith("/")) { 
        		s = s + "/";
        	}
        	dirName = s + dirName;
        }
                
        File dir = new File(dirName);
        
        if (!dir.exists()) {
            list.add(new FrameworkMisconfigTestInstance("test directory doesn't exist : " + dir));
            return list.toArray();
        }

        Pattern inPattern = null;
        Pattern exPattern = null;
        
        if (inclusionRegex != null && inclusionRegex.length() > 0) {
            inPattern = Pattern.compile(inclusionRegex);
        }

        if (exclusionRegex != null && exclusionRegex.length() > 0) {
            exPattern = Pattern.compile(exclusionRegex);
        }

        if( recursive.equals( "true" ) ) {
            addFilesRecursively( dir, list, testClassName, fileEnding, inPattern, exPattern );
        }
        else {
            for (File f : dir.listFiles()) {
                addIfValid( f, list, testClassName, fileEnding, inPattern, exPattern );
            }
        }
        return list.toArray();
    }

    public void addFilesRecursively( File dir, List<ScriptTestInstance> list, 
                                     String testClassName, String fileEnding, Pattern inPattern, Pattern exPattern ) 
        throws Exception {

        for ( File f : dir.listFiles() ){
	    if ( f.isDirectory() ) {
                addFilesRecursively( f, list, testClassName, fileEnding, inPattern, exPattern );
            }
            else {
                addIfValid( f, list, testClassName, fileEnding, inPattern, exPattern );
            }
	}
    }

    public void addIfValid( File f, List<ScriptTestInstance> list, 
                            String testClassName, String fileEnding, Pattern inPattern, Pattern exPattern ) 
        throws Exception {

        boolean include = true;
        
        if (f.toString().endsWith(fileEnding)) {
            
            if (inPattern != null) {
                include = inPattern.matcher(f.toString()).matches();
            }
            
            if (exPattern != null) {
                
                if (exPattern.matcher(f.toString()).matches()) {
                    System.out.println("JSTestFactory : regexp exclusion of " + f.toString());
                    include = false;
                }
            }
            
            if( excludeList.contains( f.getCanonicalPath() ) ) {
                include = false;
            }

            if (include) {
                ScriptTestInstance testInstance = (ScriptTestInstance) Class.forName(testClassName).newInstance();
                testInstance.setTestScriptFile(f);
                list.add(testInstance);
            }            
        }
    }
    
    /**
     * TestNG doesn't seem to grok that calling a test factory and getting an 
     * Exception or Error thrown means that something isn't quite right (I'm 
     * sure there's a way to do this, but rather than depend on some incantation
     * of testng config, lets just return a test that will fail w/ hopefully a 
     * useful message
     */
    public class FrameworkMisconfigTestInstance implements ScriptTestInstance { 
        
        private String msg;
        
        FrameworkMisconfigTestInstance(String m) { 
            msg = "FRAMEWORK CONFIG ERROR  : " + m;
        }
        
        @Test
        public void test() throws Exception {
            throw new Exception(msg);
        }

        public JSFunction convert() throws Exception {
            // don't care - stub
            return null;
        }

        public void preTest(Scope s) throws Exception {
            // don't care - stub
        }

        public void setTestScriptFile(File f) {
            // don't care - stub
        }

        public void validateOutput(Scope s) throws Exception {
            // don't care - stub
        }
    }
}
