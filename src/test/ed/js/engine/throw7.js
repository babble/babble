
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

function go(z){
    try {
        throw z;
    }
    catch ( e if z == 6 ){
        print( "six" );
    }
    catch ( e if z == 7 ){
        print( "seven" );
    }
    finally {
        print( "ff" );
    }
}

go( 6 );
go( 7 );
try {
    go( 8 );
}
catch ( e ){
    print( e );
}


print(
    function( z ){
        try {
            throw z;
        }
        catch( e if e == 5 ){
            print("a");
            return "asd";
        }
        finally {
            print( "basdlkajsd")
            
        }
    }( 5 ) 
);
