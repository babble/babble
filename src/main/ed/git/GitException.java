// GitException.java

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

package ed.git;

public class GitException extends Exception {
    public GitException( String msg ){
        super( msg );
    }
    
    public GitException( String msg , Throwable t ){
        super( msg , t );
    }
    
    public static class UsernameTaken extends GitException {
        public UsernameTaken( String username ){
            super( "username [" + username + "]  already taken" );
        }
    }
    
}
