// Dependency.java

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

public interface Dependency {
    public long lastUpdated(Set<Dependency> visitedDeps);

    public static class FileDependency implements Dependency {
        public FileDependency( File f ){
            _f = f;
        }

        public long lastUpdated(Set<Dependency> visitedDeps){
            visitedDeps.add(this);
            return _f.lastModified();
        }

        final File _f;
    }
}
