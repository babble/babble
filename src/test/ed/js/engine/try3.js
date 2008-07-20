
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

try {
    print(5);
}
finally {
    print( "f" );
}

try {
    print(5);
}
catch( foo ){
    print( foo );
    asd = 1;
}
finally {
    print( "f" );
}


try {
    print(5);
}
catch( foo ){
    print( foo );
}

print( "----" );

try {
    print(5);
    if ( 5 == 5 )
        throw(7);
    print(1);
}
catch( foo ){
    print( foo );
    asd = 1;
}
finally {
    print( "f" );
}
