// Dependency.java

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
