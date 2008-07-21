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

import java.io.File;
import ed.js.engine.Scope;
import ed.js.JSFunction;


/**
 *  Interface for script test classes
 *
 */
public interface ScriptTestInstance {


    /**
     *   Sets the script file that is to be run
     */
    public void setTestScriptFile(File f);
    
    
    /**
     *  Called before script is run.
     */
    public void preTest(Scope s) throws Exception;

    /**
     *  Called after script is run.  
     */
    public void validateOutput(Scope s) throws Exception;
    
    /**
     *   called to get the function from the script type
     */
    
    public JSFunction convert() throws Exception;
}
