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

myVar = "global"

function Clazz() {
    if(myVar != "global") {
        print("ERROR: failed to look up global variable [expected: global, actual: "+myVar+"]");
    } else {
        print("OK");
    }

    this.myVar = "instance";

    if(this.myVar != "instance") {
        print("ERROR: failed to set or lookup instance variable [expected: instance, actual: "+this.myVar+"]");
    } else {
        print("OK");
    }

    if(myVar != "global") {
        print("ERROR: global variable disappeared! [expected: global, actual: "+myVar+"]");
    } else {
        print("OK");
    }

    myVar = "global2"
}


new Clazz();


//heres the interesting part: even though the constructor failed to GET the value of the global variable,
//it was actually able to set it
if(myVar != "global2") {
    print("ERROR: the constructor failed to set the global variable [expected: global2, actual: "+myVar+"]");
} else {
    print("OK");
}
