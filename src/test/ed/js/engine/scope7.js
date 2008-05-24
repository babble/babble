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
