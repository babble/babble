
=begin
    Copyright (C) 2008 10gen Inc.
  
    This program is free software: you can redistribute it and/or  modify
    it under the terms of the GNU Affero General Public License, version 3,
    as published by the Free Software Foundation.
  
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.
  
    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
=end

def pluralize( n , t )
  return "#{ n } #{t}#{ n <= 1 ? "" : "s" }"
end

def foo( distance_in_minutes , distance_in_seconds )
  case distance_in_minutes
  when 0..1           then time = (distance_in_seconds < 60) ? "#{pluralize(distance_in_seconds, 'second')} ago" : '1 minute ago'
  when 2..59          then time = "#{distance_in_minutes} minutes ago"
  when 60..90         then time = "1 hour ago"
  when 90..1440       then time = "#{(distance_in_minutes.to_f / 60.0).round} hours ago"
  when 1440..2160     then time = '1 day ago' # 1 day to 1.5 days
  when 2160..2880     then time = "#{(distance_in_minutes.to_f / 1440.0).round} days ago" # 1.5 days to 2 days
  else time = "a long time";
  end

  return time
end

puts foo( 0 , 20 )
puts foo( 3 , 40 ) 
puts foo( 100 , 40 )
puts foo( 1000 , 40 )
