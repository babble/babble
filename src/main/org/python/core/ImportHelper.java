package org.python.core;

public class ImportHelper {
    static public PyObject loadFromSource( PySystemState state, String name, String modName, String path ){
        // Java class protection mechanisms suck, but at least they're easy to defeat
        return imp.loadFromSource( state , name , modName , path );
    }
}