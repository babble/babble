import com.sun.javadoc.*;
import com.sun.tools.doclets.formats.html.*;
import java.util.*;
import ed.js.*;
import ed.db.*;
import ed.js.engine.Scope;

public class JavadocToDB {

	public static void getTags(Doc from, JSObjectBase dest) {
        Tag cTags[] = from.tags();
        for(int j=0; j<cTags.length; j++) {
        	dest.set(cTags[j].name().substring(1), cTags[j].text());
        }
	}

	public static JSObjectBase getMethod(MethodDoc m) {
        JSObjectBase tempMethod = new JSObjectBase();
        tempMethod.set("desc", m.commentText());
        tempMethod.set("name", m.name());
        tempMethod.set("alias", m.name());
        tempMethod.set("isStatic", m.isStatic());
        tempMethod.set("isAbstract", m.isAbstract());
        tempMethod.set("isPrivate", m.isPrivate());
        tempMethod.set("isProtected", m.isProtected());
        tempMethod.set("isPublic", m.isPublic());
        tempMethod.set("isa", "FUNCTION");
        tempMethod.set("memberOf", m.containingClass().name());
        tempMethod.set("type", m.returnType().typeName());
        tempMethod.set("exceptions", new JSArray());
        tempMethod.set("see", new JSArray());
        tempMethod.set("requires", new JSArray());

        JSArray examples = new JSArray();

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
                else if(mTags[k].name().equals("@example")) {
                    examples.add(mTags[k].text());
                }
        	else {
        		tempMethod.set(mTags[k].name(), mTags[k].text());
        	}
        }
        if(!tempMethod.containsKey("returns"))
        	tempMethod.set("returns", new JSArray());

        tempMethod.set("example", examples);

        ParamTag params[] = m.paramTags();
        Parameter p2[] = m.parameters();
        JSArray jsParams = new JSArray();
        for(int k=0; k<params.length; k++) {
            JSObjectBase tempParam = new JSObjectBase();
            tempParam.set("title", "param");
            tempParam.set("desc", params[k].parameterComment());
            tempParam.set("type", p2[k].typeName());
            tempParam.set("name", p2[k].name());
            tempParam.set("isOptional", "false");
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
        tempMethod.set("memberof", m.containingClass().name());
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
            tempParam.set("isOptional", "false");
            jsParams.add(tempParam);
        }
        tempMethod.set("_params", jsParams);
        return tempMethod;
	}

    public static JSObjectBase getClasses(ClassDoc c) {
        JSObjectBase temp = new JSObjectBase();

        // get inner classes
        ClassDoc inner[] = c.innerClasses();
        JSArray jsInner = new JSArray();
        for(int j=0; j<inner.length; j++) {
            Tag[] tags = inner[j].tags("expose");
            if(tags.length == 0) continue;

            jsInner.add(getClasses(inner[j]));
        }
        temp.set("innerClasses", jsInner);

        // get interface names
        ClassDoc extend[] = c.interfaces();
        JSArray jsExtends = new JSArray();
        for(int j=0; j<extend.length; j++) {
        	jsExtends.add(extend[j].qualifiedName());
        }
        temp.set("augments", jsExtends);

        // get constructors
        ConstructorDoc cons[] = c.constructors();
        JSArray jsCons = new JSArray();
        for(int j=0; j<cons.length; j++) {
        	jsCons.add(getConstructor(cons[j]));
        }
        temp.set("constructors", jsCons);

        // get properties
        FieldDoc fields[] = c.fields();
        JSArray jsFields = new JSArray();
        for(int j=0; j<fields.length; j++) {
            Tag[] mtags = fields[j].tags("unexpose");
            if(mtags.length > 0) continue;

            JSObjectBase tempField = new JSObjectBase();
            tempField.set("desc", fields[j].commentText());
            tempField.set("name", fields[j].name());
            tempField.set("alias", fields[j].qualifiedName());
            tempField.set("isStatic", fields[j].isStatic());
            tempField.set("isPrivate", fields[j].isPrivate());
            tempField.set("isProtected", fields[j].isProtected());
            tempField.set("isPublic", fields[j].isPublic());
            getTags(fields[j], tempField);
            tempField.set("isa", "FUNCTION");
            tempField.set("memberof", c.name());
            jsFields.add(tempField);
        }
        temp.set("properties", jsFields);

        // get methods
        MethodDoc methods[] = c.methods();
        JSArray jsMethods = new JSArray();
        for (int j = 0; j < methods.length; j++) {
            Tag[] mtags = methods[j].tags("unexpose");
            if(mtags.length > 0) continue;

            jsMethods.add(getMethod(methods[j]));
        }
        temp.set("methods", jsMethods);


        // get examples
        JSArray examples = new JSArray();
        Tag eg[] = c.tags("example");
        for(int j=0; j<eg.length; j++) {
            examples.add(eg[j].text());
        }
        temp.set("example", examples);


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
        getTags(c, temp);

        // get stupid anonymous inner classes
        Tag anon[] = c.tags("anonymous");
        System.out.println("anonymous tags: "+anon.length);
        if(anon.length > 0) {
            JSArray jsAnon = new JSArray();
            for(int j=0; j<anon.length; j++) {
                String aText = anon[j].text();
                int idx;
                System.out.println("text: "+aText);
                JSObjectBase a = new JSObjectBase();
                if((idx=aText.indexOf("name : {")) >= 0 ) {
                    System.out.println("name: "+aText.substring(idx+8, aText.indexOf('}', idx+8)));
                    a.set("name", aText.substring(idx+8, aText.indexOf('}', idx+8)));
                    a.set("alias", aText.substring(idx+8, aText.indexOf('}', idx+8)));
                }
                if((idx=aText.indexOf("param : {")) >= 0 ) {
                    String txt[] = aText.substring(idx+10, aText.indexOf('}', idx+10)).split("||");
                    JSArray params = new JSArray();
                    for(int k=0; k>txt.length; k++) {
                        JSObjectBase p = new JSObjectBase();
                        String pstr[] = txt[k].split("|");
                        if(pstr.length > 0) p.set("name", pstr[0]);
                        if(pstr.length > 1) p.set("type", pstr[1]);
                        if(pstr.length > 2) p.set("desc", pstr[2]);
                        params.add(p);
                    }
                    a.set("params", params);
                }
                if((idx=aText.indexOf("author : {")) >= 0 ) {
                    a.set("author", aText.substring(idx+10, aText.indexOf('}', idx+10)));
                }
                if((idx=aText.indexOf("returns : {")) >= 0) {
                    String txt[] = aText.substring(idx+11, aText.indexOf('}', idx+11)).split("||");
                    JSArray returns = new JSArray();
                    JSObjectBase tempReturn = new JSObjectBase();
                    tempReturn.set("title", "return");
                    if(txt.length > 0) tempReturn.set("type", txt[0]);
                    if(txt.length > 1) tempReturn.set("desc", txt[1]);
                    returns.add(tempReturn);
                    a.set("type", txt[0]);
                    a.set("returns", returns);
                }
                if( (idx=aText.indexOf("desc : {")) >= 0) {
                    System.out.println("desc: "+aText.substring(idx+8, aText.indexOf('}', idx+8)));
                    a.set("desc", aText.substring(idx+8, aText.indexOf('}', idx+8)));
                }
                jsAnon.add(a);
            }
            temp.set("anonymous", jsAnon);
        }

        return temp;
    }

    public static boolean start(RootDoc root) {
        Scope s = Scope.getThreadLocal();
        Object dbo = s.get("db");
        if(! (dbo instanceof DBApiLayer)) throw new RuntimeException("your database is having an identity crisis");

        DBApiLayer db = (DBApiLayer)dbo;
        DBCollection collection = db.getCollection("doc");

        ClassDoc[] classes = root.classes();

        for (int i = 0; i < classes.length; i++) {
            Tag[] tags = classes[i].tags("expose");
            if(tags.length == 0) continue;

            /*            JSObject query = new JSObjectBase();
            query.set("name", classes[i].name());
            Iterator it = collection.find(query);
            while(it.hasNext())
                System.out.println("Conflicting class names: "+it.next());
            */

            JSObjectBase obj = new JSObjectBase();
            JSObjectBase jsClasses = new JSObjectBase();

            jsClasses.set(classes[i].name(), getClasses(classes[i]));
            jsClasses.set("fromJava", true);
            obj.set("symbolSet", jsClasses);

            JSObjectBase topLevel = new JSObjectBase();
            topLevel.set("_index", obj);
            topLevel.set("ts", Calendar.getInstance().getTime().toString());


            collection.save((JSObject)topLevel);
        }

        // Generates standard javadoc html page
        //return com.sun.tools.doclets.formats.html.HtmlDoclet.start(root);
        return true;
    }

}
