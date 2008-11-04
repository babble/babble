function Clazz() {
    this.prop = "foo";    
}
Clazz.prototype = {
    method: function() {
        return this.prop;
    }
};

var obj = new Clazz();
print( obj["method"]() );