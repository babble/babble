
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
