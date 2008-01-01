
this.a = 17;

function foo() {
    this.a = 3;

    this.b = function() {
        print(this.a);
        var c = function() {
            print( this.a );
        }
        c();
    }

}

var x = new foo();

x.b();
print( this.a );

function zzz(){
    this.a = 123;
}
zzz();
print( this.a );
