// GitHost.java

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

public interface GitHost {

    public GitIdentity createAccount( String username , String email , String password )
        throws GitException;
    
    public void forkRepository( GitIdentity who , String whatToFork )
        throws GitException;

    public void renameRepository( GitIdentity who , String from , String to )
        throws GitException;
    
}
