
function Foo(){
    
}

Foo.prototype.a = function(){
    return 5;
}

assert.eq( 5 , javaStatic( "ed.js.JS" , "eval" , new Foo() , "a" ) );
