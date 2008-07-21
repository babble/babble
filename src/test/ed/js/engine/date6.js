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

var d = new Date(1204224375193);
print( "initial date: \t\t"+d.toString());

var ms = d.setDate( 1 );
print( "setDate(1): \t\t" + d.toString() + " ("+ms+")");
ms = d.setDate( -1 );
print( "setDate(-1): \t\t" + d.toString() + " ("+ms+")");
ms = d.setDate( 32 );
print( "setDate(32): \t\t" + d.toString() + " ("+ms+")");

ms = d.setFullYear( 1998, 10, 1 );
print( "setFullYear(1998, 10, 1): \t\t" + d.toString() + " ("+ms+")");
ms = d.setFullYear( 1997, 11);
print( "setFullYear(1997, 11): \t\t" + d.toString() + " ("+ms+")");
ms = d.setFullYear( 1996 );
print( "setFullYear(1996): \t\t" + d.toString() + " ("+ms+")");

ms = d.setHours(9, 10, 11, 12);
print( "setHours(9, 10, 11, 12): \t\t" + d.toString() + " ("+ms+")");
ms = d.setHours(8, 7, 6);
print( "setHours(8, 7, 6): \t\t" + d.toString() + " ("+ms+")");
ms = d.setHours(7, 2);
print( "setHours(7, 2): \t\t" + d.toString() + " ("+ms+")");
ms = d.setHours(6);
print( "setHours(6): \t\t" + d.toString() + " ("+ms+")");
ms = d.setHours(-20);
print( "setHours(-20): \t\t" + d.toString() + " ("+ms+")");
ms = d.setHours(30);
print( "setHours(30): \t\t" + d.toString() + " ("+ms+")");

ms = d.setMilliseconds(50);
print( "setMilliseconds(50): \t\t" + d.toString() + " ("+ms+")");
ms = d.setMilliseconds(2000000);
print( "setMilliseconds(2000000): \t\t" + d.toString() + " ("+ms+")");
ms = d.setMilliseconds(-2);
print( "setMilliseconds(-2): \t\t" + d.toString() + " ("+ms+")");

ms = d.setMinutes(20, 19, 18);
print( "setMinutes(20, 19, 18): \t\t" + d.toString() + " ("+ms+")");
ms = d.setMinutes(15, 14);
print( "setMinutes(15, 14): \t\t" + d.toString() + " ("+ms+")");
ms = d.setMinutes(17);
print( "setMinutes(17): \t\t" + d.toString() + " ("+ms+")");
ms = d.setMinutes(-20);
print( "setMinutes(-20): \t\t" + d.toString() + " ("+ms+")");
ms = d.setMinutes(70);
print( "setMinutes(70): \t\t" + d.toString() + " ("+ms+")");

ms = d.setMonth(2, 1);
print( "setMonth(2, 1): \t\t" + d.toString() + " ("+ms+")");
ms = d.setMonth(3);
print( "setMonth(3): \t\t" + d.toString() + " ("+ms+")");
ms = d.setMonth(13);
print( "setMonth(13): \t\t" + d.toString() + " ("+ms+")");
ms = d.setMonth(-3);
print( "setMonth(-3): \t\t" + d.toString() + " ("+ms+")");

ms = d.setSeconds(3, 5);
print( "setSeconds(3, 5): \t\t" + d.toString() + " ("+ms+")");
ms = d.setSeconds(6);
print( "setSeconds(6): \t\t" + d.toString() + " ("+ms+")");
ms = d.setSeconds(61);
print( "setSeconds(61): \t\t" + d.toString() + " ("+ms+")");
ms = d.setSeconds(-1);
print( "setSeconds(-1): \t\t" + d.toString() + " ("+ms+")");

ms = d.setTime(500);
print( "setTime(500): \t\t"+ d.toString() + " ("+ms+")");


print( (d - 100).toFixed(0) );
