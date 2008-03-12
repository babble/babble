// These are tests for the $A function, which I implemented but then decided
// wasn't very useful to us
/*
a = $A([1, 2, 3]);
assert(a[0] == 1);
assert(a[1] == 2);
assert(a[2] == 3);

o = {0: 1, 4: 5, length: 5};
a = $A(o);

assert(a[0] == 1);
assert(a[4] == 5);
assert(a[2] == null);
*/

// Function.prototype.bind
f = function(a, b, c){
    return [this, a, b, c];
};

newf = f.bind(0, 1, 2);
g = newf(3);
assert(g[0] == 0);
assert(g[1] == 1);
assert(g[2] == 2);
assert(g[3] == 3);

// Make sure we accept empty arguments in either position
f2 = function(){
    return [this].concat(arguments);
};

newf2 = f2.bind(1);
g = newf2();
assert(g.length == 1);
assert(g[0] == 1);

g = newf2(2, 4);
assert(g.length == 3);
assert(g[0] == 1);
assert(g[1] == 2);
assert(g[2] == 4);


C = function(){
    this.x = 1;
};

C.prototype.getX = function(){
    return this.x;
};

C.prototype.getX = C.prototype.getX.wrap(
    function(proceed, factor){
        factor = (factor == null)? 1 : factor;
        return proceed()*factor;
    });

c1 = new C();
assert(c1.getX() == 1);
assert(c1.getX(5) == 5);
c1.x = 6;

assert(c1.getX() == 6);
assert(c1.getX(2) == 12);

a = function( a , b ){};
assert( a.argumentNames().length == 2 );
assert( a.argumentNames()[0] == "a" );
assert( a.argumentNames()[1] == "b" );

function b( a , b ){};
assert( b.argumentNames().length == 2 );
assert( b.argumentNames()[0] == "a" );
assert( b.argumentNames()[1] == "b" );


a = function(){};
a.prototype.m1 = function(i){
    return i * 2;
};

f = function(){};
f.superclass = a;

f.addMethods = Class.Methods.addMethods;

f.addMethods({
    m1: function($super, i){
        return $super(i+4);
    }
});

assert(f.m1(5) == 18);


var Animal = Class.create({
    initialize: function(name) {
        this.name = name;
    },
    name: "",
    eat: function() {
        return this.say("Yum!");
    },
    say: function(message) {
        return this.name + ": " + message;
    }
});

// subclass that augments a method
var Cat = Class.create(Animal, {
    eat: function($super, food) {
        if (food instanceof Mouse) return $super();
        else return this.say("Yuk! I only eat mice.");
    }
});

// empty subclass
var Mouse = Class.create(Animal, {});

assert(isFunction(Animal));
assert(Cat.superclass == Animal);
assert(Mouse.superclass == Animal);

var pet = new Animal("Nibbles");
assert(pet.name == "Nibbles");
assert(pet.say("Hi!") == "Nibbles: Hi!");
assert(Animal == pet.constructor);
assert(pet.superclass == null);

var tom = new Cat("Tom");
assert(Cat == tom.constructor);
assert(Animal == tom.constructor.superclass);
assert("Tom" == tom.name);
assert("Tom: meow" == tom.say('meow'));
//assert('Tom: Yuk! I only eat mice.' == tom.eat(new Animal));

assert('Tom: Yum!' == tom.eat(new Mouse));

print((new Animal()) instanceof Mouse);

print((new Mouse()) instanceof Mouse);
