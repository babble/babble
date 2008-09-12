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

import com.sun.javadoc.*;
import com.sun.tools.doclets.formats.html.*;
import java.util.*;
import ed.js.*;
import ed.db.*;
import ed.js.engine.Scope;
import java.util.regex.*;
import ed.doc.Generate;

public class JavadocToDB {

    public static void getTags(Doc from, JSObjectBase dest) {
        Tag cTags[] = from.tags();
        for(int j=0; j<cTags.length; j++) {
            if(!(cTags[j].name().equals("@return") ||
                 cTags[j].name().equals("@example") ||
                 cTags[j].name().equals("@param") ||
                 cTags[j].name().equals("@anonymous"))) {
                dest.set(cTags[j].name().substring(1), cTags[j].text());
            }
        }
    }

    public static JSObjectBase getMethod(MethodDoc m) {
        JSObjectBase tempMethod = new JSObjectBase();
        tempMethod.set("desc", m.commentText());
        tempMethod.set("name", m.name());
        tempMethod.set("alias", m.name());
        tempMethod.set("isStatic", m.isStatic());
        tempMethod.set("isAbstract", m.isAbstract());
        tempMethod.set("isPublic", m.isPublic());
        tempMethod.set("isa", "FUNCTION");
        tempMethod.set("memberOf", m.containingClass().name());
        tempMethod.set("type", m.returnType().typeName());
        tempMethod.set("exceptions", new JSArray());
        tempMethod.set("see", new JSArray());
        tempMethod.set("requires", new JSArray());


        Tag mTags[] = m.tags();
        for(int k=0; k<mTags.length; k++) {
            if(mTags[k].name().equals("@return")) {
                JSArray returns = new JSArray();
                JSObjectBase tempReturn = new JSObjectBase();
                tempReturn.set("title", "return");
                tempReturn.set("desc", mTags[k].text());
                tempReturn.set("type", m.returnType().typeName());
                returns.add(tempReturn);
                tempMethod.set("returns", returns);
            }
            else if (mTags[k].name().equals("@jsget")) {
                tempMethod.set("desc", "Getter for the class "+m.containingClass().name());
            }
            else if (mTags[k].name().equals("@jsset")) {
                tempMethod.set("desc", "Setter for the class "+m.containingClass().name());
            }
            else if (mTags[k].name().equals("@jskeyset")) {
                tempMethod.set("desc", "Returns the keyset of the class "+m.containingClass().name());
            }
            else {
                tempMethod.set(mTags[k].name(), mTags[k].text());
            }
        }
        if(!tempMethod.containsKey("returns")) {
            JSArray returns = new JSArray();
            JSObjectBase tempReturn = new JSObjectBase();
            tempReturn.set("title", "return");
            tempReturn.set("type", m.returnType().typeName());
            returns.add(tempReturn);
            tempMethod.set("returns", returns);
        }

        tempMethod.set("example", getTagArray( m, "example" ) );

        ParamTag params[] = m.paramTags();
        Parameter p2[] = m.parameters();
        JSArray jsParams = new JSArray();
        for(int k=0; k<Math.min(params.length, p2.length); k++) {
            JSObjectBase tempParam = new JSObjectBase();
            tempParam.set("title", "param");
            tempParam.set("desc", params[k].parameterComment());
            tempParam.set("type", p2[k].typeName());
            tempParam.set("name", p2[k].name());
            jsParams.add(tempParam);
        }
        tempMethod.set("_params", jsParams);
        tempMethod.set("params", jsParams);
        return tempMethod;
    }

    public static JSObjectBase getConstructor(ConstructorDoc m) {
        JSObjectBase tempMethod = new JSObjectBase();
        tempMethod.set("desc", m.commentText());
        tempMethod.set("name", m.name());
        tempMethod.set("alias", m.qualifiedName());
        tempMethod.set("isStatic", m.isStatic());
        tempMethod.set("isPrivate", m.isPrivate());
        tempMethod.set("isProtected", m.isProtected());
        tempMethod.set("isPublic", m.isPublic());
        tempMethod.set("isa", "CONSTRUCTOR");
        tempMethod.set("memberOf", m.containingClass().name());
        getTags(m, tempMethod);

        ParamTag params[] = m.paramTags();
        Parameter p2[] = m.parameters();
        JSArray jsParams = new JSArray();
        for(int k=0; k<params.length; k++) {
            JSObjectBase tempParam = new JSObjectBase();
            tempParam.set("title", "param");
            tempParam.set("desc", params[k].parameterComment());
            tempParam.set("type", p2[k].typeName());
            tempParam.set("name", p2[k].name());
            jsParams.add(tempParam);
        }
        tempMethod.set("_params", jsParams);
        tempMethod.set("params", jsParams);
        return tempMethod;
    }

    public static JSObjectBase getField( FieldDoc field ) {
        JSObjectBase tempField = new JSObjectBase();
        tempField.set("desc", field.commentText());
        tempField.set("name", field.name());
        tempField.set("alias", field.qualifiedName());
        tempField.set("type", (field.type()).typeName());
        tempField.set("isStatic", field.isStatic());
        tempField.set("isPublic", field.isPublic());

        getTags(field, tempField);

        tempField.set("isa", "FUNCTION");
        tempField.set("memberOf", field.containingClass().name());
        return tempField;
    }

    public static boolean dontProcess( ProgramElementDoc elem ) {
        if( elem.tags( "unexpose" ).length > 0 ||
            elem.isPrivate() ||
            elem.isProtected() ||
            elem.isPackagePrivate() )
            return true;

        return false;
    }

    public static JSArray getJSArray( ProgramElementDoc[] elem ) {
        JSArray js = new JSArray();
        for(int j=0; j < elem.length; j++) {
            if( dontProcess( elem[j] ) ) 
                continue;

            if( elem[j] instanceof ClassDoc )
                js.add( getClasses( (ClassDoc)elem[j] ) );
            else if( elem[j] instanceof ConstructorDoc )
                js.add( getConstructor( (ConstructorDoc)elem[j] ) );
            else if( elem[j] instanceof MethodDoc )
                js.add( getMethod( (MethodDoc)elem[j] ) );
            else if( elem[j] instanceof FieldDoc )
                js.add( getField( (FieldDoc)elem[j] ) );
        }
        return js;
    }

    public static JSArray getTagArray( ProgramElementDoc elem, String tagname ) {
        JSArray js = new JSArray();
        Tag tags[] = elem.tags( tagname );
        for(int j=0; j< tags.length; j++) {
            js.add( tags[j].text() );
        }
        return js;
    }

    public static JSObjectBase getClasses(ClassDoc c) {
        JSObjectBase temp = new JSObjectBase();

        // get interface names
        ClassDoc extend[] = c.interfaces();
        JSArray jsExtends = new JSArray();
        for(int j=0; j<extend.length; j++) {
            jsExtends.add( extend[j].qualifiedName() );
        }
        temp.set("augments", jsExtends);

        // get inner classes
        temp.set( "innerClasses", getJSArray( c.innerClasses() ) );

        // get constructors
        temp.set("constructors", getJSArray( c.constructors() ) );

        // get properties
        temp.set("properties", getJSArray( c.fields() ) );

        // get methods
        temp.set("methods", getJSArray( c.methods() ) );

        // get examples
        temp.set( "example", getTagArray( c, "example" ) );


        temp.set("classDesc", c.commentText());
        temp.set("desc", c.commentText());
        temp.set("alias", c.name());
        temp.set("name", c.qualifiedName());
        temp.set("isStatic", c.isStatic());
        temp.set("isAbstract", c.isAbstract());
        temp.set("isPrivate", c.isPrivate());
        temp.set("isProtected", c.isProtected());
        temp.set("isPublic", c.isPublic());
        temp.set("isExternalizable", c.isExternalizable());
        temp.set("isSerializable", c.isSerializable());
        temp.set("isInterface", c.isInterface());
        temp.set("isa", "CONSTRUCTOR");
        temp.set("memberof", c.name());
        temp.set("comment", new JSObjectBase());
        temp.set("inheritsFrom", new JSArray());
        temp.set("params", new JSArray());
        temp.set("exceptions", new JSArray());
        temp.set("returns", new JSArray());
        temp.set("requires", new JSArray());
        temp.set("see", new JSArray());

        // pick up any other tags
        getTags(c, temp);

        // get stupid anonymous inner classes
        Tag anon[] = c.tags("anonymous");
        if( anon.length > 0 ) {
            JSArray jsAnon = new JSArray();
            for(int j=0; j<anon.length; j++) {
                JSObjectBase a = new JSObjectBase();
                String aText = anon[j].text();

                Pattern tagger = Pattern.compile("([A-Za-z]+) : \\{([^\\}]+)\\}");
                Matcher m = tagger.matcher(aText);
                JSArray params = new JSArray();
                JSArray returns = new JSArray();
                while(m.find()) {
                    if(m.group(1).equals("param")) {
                        Pattern subtagger = Pattern.compile("([A-Za-z]+) : \\(([^\\)]+)\\)");
                        Matcher subm = subtagger.matcher(m.group(2));
                        JSObjectBase pgroup = new JSObjectBase();
                        pgroup.set("title", "param");
                        while(subm.find()) {
                            pgroup.set(subm.group(1), subm.group(2));
                        }
                        params.add(pgroup);
                    }
                    else if(m.group(1).equals("return")) {
                        Pattern subtagger = Pattern.compile("([A-Za-z]+) : \\(([^\\)]+)\\)");
                        Matcher subm = subtagger.matcher(m.group(2));
                        JSObjectBase pgroup = new JSObjectBase();
                        pgroup.set("title", "return");
                        while(subm.find()) {
                            pgroup.set(subm.group(1), subm.group(2));
                            if(subm.group(1).equals("type")) {
                                a.set("type", subm.group(2));
                            }
                        }
                        returns.add(pgroup);
                        a.set("returns", returns);
                    }
                    else if(m.group(1).equals("name")) {
                        a.set("alias", m.group(2));
                        a.set("name", m.group(2));
                    }
                    else {
                        a.set(m.group(1), m.group(2));
                    }
                }
                a.set("_params", params);
                a.set("params", params);
                jsAnon.add(a);
            }
            temp.set("anonymous", jsAnon);
        }

        return temp;
    }

    public static void resolveConflicts(ClassDoc newClass, JSObjectBase javadocObj, DBCollection db) {
        // is there a conflict with a js class?
        JSObject query = new JSObjectBase();
        query.set("name", newClass.name());
        Iterator it = db.find(query);
        if(it == null) return;
        //        System.out.println("it != null: "+newClass.name());

        // drill down to get the relevant obj
        JSObject jsdocObj = (JSObjectBase)it.next();

        // for later, when we need to delete this obj
        JSObject dquery = new JSObjectBase();
        dquery.set("_id", (ed.db.ObjectId)jsdocObj.get("_id"));

        jsdocObj = (JSObject)jsdocObj.get("_index");
        jsdocObj = (JSObject)jsdocObj.get("symbolSet");
        jsdocObj = (JSObject)jsdocObj.get(newClass.name());
        mergeClasses(javadocObj, jsdocObj);

        // remove the repeated class
        db.remove(dquery);
    }

    public static void attachClass(ClassDoc newClass, JSObjectBase javadocObj, DBCollection db, String jsClassName) {
        // is there a conflict with a js class?
        JSObject query = new JSObjectBase();
        query.set("name", jsClassName);
        Iterator it = db.find(query);
        System.out.println("jsClassName: "+jsClassName);
        if(it == null) {
            System.out.println("Error: tried to attach "+newClass.name()+" to non-existant JavaScript class "+jsClassName);
            return;
        }

        JSObject jsdoc = (JSObject)it.next();
        JSObject coreobj = (JSObject)jsdoc.get("_index");
        coreobj = (JSObject)coreobj.get("symbolSet");
        coreobj = (JSObject)coreobj.get(jsClassName);
        mergeClasses(coreobj, javadocObj);

        // save to db
        db.save(jsdoc);
    }

    public static JSObject mergeClasses(JSObject master, JSObject child) {
        // merge constructors
        JSArray javadocCons = (JSArray)master.get("constructors");
        if(javadocCons == null) javadocCons = new JSArray();
        JSArray jsCons = (JSArray)child.get("constructors");
        if(jsCons != null) {
            Iterator p = jsCons.iterator();
            while(p.hasNext())
                javadocCons.add(p.next());
        }

        // merge methods
        JSArray javadocMethod = (JSArray)master.get("methods");
        Iterator p = ((JSArray)child.get("methods")).iterator();
        while(p.hasNext())
            javadocMethod.add(p.next());

        // merge props
        JSArray javadocProp = (JSArray)master.get("properties");
        p = ((JSArray)child.get("properties")).iterator();
        while(p.hasNext())
            javadocProp.add(p.next());

        // add src file, if it exists
        if(child.get("srcFile") != null) {
            master.set("srcFile", (child.get("srcFile")).toString());
        }
        return master;
    }


    public static boolean start(RootDoc root) {
        Scope s = Scope.getThreadLocal();
        Object dbo = s.get( "db" );
        if(! (dbo instanceof DBApiLayer)) throw new RuntimeException("your database isn't a database");

        DBApiLayer db = (DBApiLayer)dbo;
        DBCollection collection = db.getCollection("doc");

        ClassDoc[] classes = root.classes();

        for (int i = 0; i < classes.length; i++) {
            if( ( Generate.classes.size() > 0 && 
                  !Generate.classes.contains( classes[i].name() ) ) || 
                classes[i].tags( "expose" ).length == 0 )
                continue;

            JSObjectBase obj = new JSObjectBase();
            JSObjectBase jsClasses = new JSObjectBase();

            JSObjectBase javaClass = getClasses(classes[i]);
            Tag[] attacher = classes[i].tags("attachto");
            if(attacher.length > 0) {
                attachClass(classes[i], javaClass, collection, attacher[0].text());
                continue;
            }
            else {
                resolveConflicts(classes[i], javaClass, collection);
            }
            jsClasses.set(classes[i].name(), javaClass);
            obj.set("symbolSet", jsClasses);


            JSObjectBase topLevel = new JSObjectBase();
            topLevel.set("_index", obj);
            topLevel.set("ts", Calendar.getInstance().getTime().toString());
            topLevel.set("alias", classes[i].name());
            topLevel.set("name", classes[i].qualifiedName());

            int summarylen = classes[i].commentText().indexOf(". ")+1;
            if(summarylen == 0) summarylen = classes[i].commentText().indexOf(".\n")+1;
            if(summarylen == 0) summarylen = classes[i].commentText().length();
            topLevel.set("desc", classes[i].commentText().substring(0, summarylen));

            collection.save((JSObject)topLevel);
        }

        // Generates standard javadoc html page
        //return com.sun.tools.doclets.formats.html.HtmlDoclet.start(root);
        return true;
    }

}
