// ImportHelper.java

package org.python.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

public class ImportHelper {
    // Java class protection mechanisms suck, but at least they're
    // easy to defeat
    public static PyObject loadFromSource(PySystemState sys, String name, String modName, String entry){
        return imp.loadFromSource(sys, name, modName, entry);
    }

    private static final String IMPORT_LOG = "import";

    // copy-pasted from imp.java's "loadFromSource", seasoned to taste
    public static PyObject loadFromDirectory(PySystemState sys, String name, String modName, String entry){
        String sourceName = "__init__.py";
        String compiledName = "__init__$py.class";
        String directoryName = sys.getPath(entry);
        // displayDirName is for identification purposes (e.g.
        // __file__): when null it forces java.io.File to be a
        // relative path (e.g. foo/bar.py instead of /tmp/foo/bar.py)
        String displayDirName = entry.equals("") ? null : entry.toString();

        // First check for packages
        File dir = new File(directoryName);
        File sourceFile = new File(dir, sourceName);
        File compiledFile = new File(dir, compiledName);

        boolean pkg = dir.isDirectory() && (sourceFile.isFile()
                                            || compiledFile.isFile());
        if (!pkg) {
            Py.writeDebug(IMPORT_LOG, "trying source " + dir.getPath());
            sourceName = name + ".py";
            compiledName = name + "$py.class";
            sourceFile = new File(directoryName, sourceName);
            compiledFile = new File(directoryName, compiledName);
        } else {
            PyModule m = imp.addModule(modName);
            PyObject filename = new PyString(dir.getPath());
            m.__dict__.__setitem__("__path__", new PyList(
                    new PyObject[] { filename }));
            m.__dict__.__setitem__("__file__", filename);
        }

        if (sourceFile.isFile() && imp.caseok(sourceFile, sourceName)) {
            String filename = new File(dir, sourceName).getPath();
            if(compiledFile.isFile() && imp.caseok(compiledFile, compiledName)) {
                Py.writeDebug(IMPORT_LOG, "trying precompiled "
                        + compiledFile.getPath());
                long pyTime = sourceFile.lastModified();
                long classTime = compiledFile.lastModified();
                if(classTime >= pyTime) {
                    // XXX: filename should use compiledName here (not
                    // sourceName), but this currently breaks source
                    // code printed out in tracebacks
                    PyObject ret = imp.createFromPyClass(modName, makeStream(compiledFile),
                                                     true, filename);
                    if(ret != null) {
                        return ret;
                    }
                }
            }
            return imp.createFromSource(modName, makeStream(sourceFile), filename,
                                    compiledFile.getPath());
        }
        // If no source, try loading precompiled
        Py.writeDebug(IMPORT_LOG, "trying precompiled with no source "
                + compiledFile.getPath());
        if(compiledFile.isFile() && imp.caseok(compiledFile, compiledName)) {
            String filename = new File(displayDirName, compiledName).getPath();
            return imp.createFromPyClass(modName, makeStream(compiledFile), true, filename);
        }
        return null;
    }

    public static String getBuiltin(String name){
        return PySystemState.getBuiltin(name);
    }

    // Support functions I had to copy
    private static InputStream makeStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (IOException ioe) {
            throw Py.IOError(ioe);
        }
    }

}
