// Dependency.java

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
