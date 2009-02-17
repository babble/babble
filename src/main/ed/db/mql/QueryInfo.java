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

package ed.db.mql;

import java.util.List;
import java.util.ArrayList;

/**
 *  Class to collect info about a query from teh AST.  Currently only generates JS strings, but
 *  will be extended to do other languages, as well as direct operations on the java object model
 */
public class QueryInfo {

    private int _type;
    private String _dbName = "db";
    private int _limit;
    private int _skip;
    private String _collection_name="NOCOLL";
    private List<Field> _returnFieldList = new ArrayList<Field>();
    private List<Field> _setFieldList = new ArrayList<Field>();
    private String _whereClause = "";
    private List<Field> _orderByClauses = new ArrayList<Field>();
    private boolean _count;

    public QueryInfo(String dbName){
        _dbName = dbName;
    }

    public QueryInfo() {
        this("db");
    }

    public String getDBVarName() {
        return _dbName;
    }
    
    public void addSetField(Field f) {
        _setFieldList.add(f);
    }
    
    public void setType(int i) {
        _type = i;
    }

    public int getType() {
        return _type;
    }
    
    public void setWhereClause(String s) {
        _whereClause = s;
    }

    public void addOrderBy(Field f) {
        _orderByClauses.add(f);
    }

    public void setCount(boolean count) {
        _count = count;
    }

    public int getLimit() {
        return _limit;
    }

    public void setLimit(int limit) {
        _limit = limit;
    }

    public int getSkip() {
        return _skip;
    }

    public void setSkip(int skip) {
        _skip = skip;
    }

    public String getCollection_name() {
        return _collection_name;
    }

    public void setCollection_name(String collection_name) {
        _collection_name = collection_name;
    }

    public List<Field> getReturnFieldList() {
        return _returnFieldList;
    }

    public void addReturnField(Field f) {
        _returnFieldList.add(f);
    }

    public String toString() {

        switch(_type) {
            case MQLTreeConstants.JJTSELECT :
                return generateSelectString();

            case MQLTreeConstants.JJTUPDATE :
                return generateUpdateString();
                
            case MQLTreeConstants.JJTDELETE :
                return generateDeleteString();

            default:
                return "UNKNOWN";
        }
    }

    String generateDeleteString() {
        String s =  "db."+ _collection_name + ".remove(";

        if (_whereClause.length() > 0)  {

            StringBuilder wc = new StringBuilder("{ $where : function() { return ");

            wc.append(_whereClause);

            wc.append(";}}");

            s = s + wc.toString();
        }
        else {
            s = s + "{}";
        }

        s = s + ")";

        return s;
    }


    /*
     *  need to use db.eval() as db....update() does surprising things
     */
    String generateUpdateString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getDBVarName());
        sb.append(".eval(  function() { ");

        // do the find()

        sb.append(" var cur = ");
        sb.append(this.generateSelectString());
        sb.append(";");

        // now iterate and create our updater function

        sb.append(" cur.forEach( function() {");

        for (Field f : _setFieldList) {

            // fixme

            if (f._nameType == MQLTreeConstants.JJTPATH) {
                sb.append("arguments[0].");
            }

            sb.append(f._name);
            sb.append("=");

            if (f._valueType == MQLTreeConstants.JJTPATH) {
                sb.append("arguments[0].");
            }

            sb.append(f._value);
            sb.append("; ");
        }

        sb.append(getDBVarName());
        sb.append(".");        
        sb.append(_collection_name);
        sb.append(".save(arguments[0]); ");

        sb.append("})");

        // close the dbeval function
        
        sb.append("})");

        return sb.toString();
    }

    /**
     *  First run at this - tried to use update, but update won't update more than one object, and it replaces the objec
     *  rather than updates it...
     * @return string containing js form of query
     */
    String generateUpdateString1() {

        StringBuilder sb = new StringBuilder(getDBVarName());

        sb.append(".");
        sb.append(_collection_name);
        sb.append(".update(");

        if (_whereClause.length() > 0)  {

            StringBuilder wc = new StringBuilder("{ $where : function() { return ");

            wc.append(_whereClause);

            wc.append(";}}");

            sb.append(wc.toString());
        }
        else {
            sb.append("{}");
        }

        if (_setFieldList.size() > 0) {

            StringBuilder rf = new StringBuilder("{");

            int count = 0;
            for (Field f : _setFieldList) {
                if (count++ != 0) {
                    rf.append(", ");
                }
                rf.append(f._name);
                rf.append(":");
                rf.append(f._value);
            }
            rf.append("}");

            sb.append(", ");
            sb.append(rf.toString());
        }
        else {
           sb.append(", {}");
        }
        sb.append(", {upsert:false})");

        return sb.toString();
    }

    String generateSelectString() {

        StringBuilder sb = new StringBuilder(getDBVarName());
        sb.append(".");
        sb.append(_collection_name);
        sb.append(".find(");

        if (_whereClause.length() > 0)  {

            StringBuilder wc = new StringBuilder("{ $where : function() { return ");

            wc.append(_whereClause);

            wc.append(";}}");

            sb.append(wc.toString());
        }
        else {
            sb.append("{}");
        }

        if (_returnFieldList.size() > 0) {

            StringBuilder rf = new StringBuilder("{");

            int count = 0;
            for (Field f : _returnFieldList) {
                if (count++ != 0) {
                    rf.append(", ");
                }
                rf.append(f._name);
                rf.append(":true");
            }
            rf.append("}");

            sb.append(", ");
            sb.append(rf.toString());
        }

        sb.append(")");


        if (_orderByClauses.size() > 0) {
            StringBuilder rf = new StringBuilder(".sort({");

            int count = 0;
            for (Field f : _orderByClauses) {
               
                if (count++ != 0) {
                    rf.append(", ");
                }
                rf.append(f._name);
                rf.append(":");
                rf.append(f._value);
            }
            rf.append("})");

            sb.append(rf.toString());
        }

        if (_limit > 0) {
            sb.append(".limit(");
            sb.append(_limit);
            sb.append(")");
        }

        if (_skip > 0) {
            sb.append(".skip(");
            sb.append(_skip);
            sb.append(")");
        }

        if (_count) {
            sb.append(".count()");
        }

        return sb.toString();
    }

    public static  class Field {
        public String _name;
        int _nameType;
        public String _value;
        int _valueType;

        public Field(String s, int t) {
            _name = s;
            _nameType = t;
        }

        public Field(String s, int a,  String v, int b) {
            _name = s;
            _nameType = a;
            _value = v;
            _valueType = b;
        }
    }
}
