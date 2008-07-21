// These are tests for the $A function, which I implemented but then decided
// wasn't very useful to us
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


// These unit tests are from Prototype -- check test/unit/base.html

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

var Empty = Class.create();
assert('object', typeof new Empty);

var tom = new Cat("Tom");
assert(Cat == tom.constructor);
assert(Animal == tom.constructor.superclass);
assert("Tom" == tom.name);
assert("Tom: meow" == tom.say('meow'));
assert('Tom: Yuk! I only eat mice.' == tom.eat(new Animal));

assert('Tom: Yum!' == tom.eat(new Mouse));

// augment the constructor and test
var Dodo = Class.create(Animal, {
    initialize: function($super, name) {
        $super(name);
        this.extinct = true;
    },

    say: function($super, message) {
        return $super(message) + " honk honk";
    }
});

var gonzo = new Dodo('Gonzo');
assert('Gonzo' == gonzo.name);
assert(gonzo.extinct);
assert("Gonzo: hello honk honk" == gonzo.say("hello"));


var tom   = new Cat('Tom');
var jerry = new Mouse('Jerry');

Animal.addMethods({
    sleep: function() {
        return this.say('ZZZ');
    }
});

Mouse.addMethods({
    sleep: function($super) {
        return $super() + " ... no, can't sleep! Gotta steal cheese!";
    },
    escape: function(cat) {
        return this.say('(from a mousehole) Take that, ' + cat.name + '!');
    }
});

assertEqual = function(one, two, message){
    if (one != two) throw message;
}

assertUndefined = function(thing, message){
    if(! message) message = "not undefined";
    if(thing == null)
        ; // pass
    else
        throw message;
}

assertEqual('Tom: ZZZ', tom.sleep(), "added instance method not available to subclass");
assertEqual("Jerry: ZZZ ... no, can't sleep! Gotta steal cheese!", jerry.sleep());
assertEqual("Jerry: (from a mousehole) Take that, Tom!", jerry.escape(tom));
// insure that a method has not propagated *up* the prototype chain:
assertUndefined(tom.escape);
assertUndefined(new Animal().escape);

Animal.addMethods({
    sleep: function() {
        return this.say('zZzZ');
    }
});

assertEqual("Jerry: zZzZ ... no, can't sleep! Gotta steal cheese!", jerry.sleep());
