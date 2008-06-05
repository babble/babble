// Dependency.java

package ed.util;

import java.io.*;
import java.util.*;

public interface Dependency {
    public long lastUpdated();

    public static class FileDependency implements Dependency {
        public FileDependency( File f ){
            _f = f;
        }

        public long lastUpdated(){
            return _f.lastModified();
        }

        final File _f;
    }
}
