# Copyright (C) 2008 10gen Inc.
#
# This program is free software: you can redistribute it and/or modify it
# under the terms of the GNU Affero General Public License, version 3, as
# published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
# for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

# Only parses really, really simple WHERE clauses right now. The parser
# returns a hash suitable for use by Mongo.
module XGen

  module SQL

    class Tokenizer

      attr_reader :sql

      def initialize(sql)
        @sql = sql
        @length = sql.length
        @pos = 0
        @extra_tokens = []
      end

      def add_extra_token(tok)
        @extra_tokens.push(tok)
      end

      def skip_whitespace
        while @pos < @length && [" ", "\n", "\r", "\t"].include?(@sql[@pos,1])
          @pos += 1
        end
      end

      def more?
        skip_whitespace
        @pos < @length
      end

      # Returns the next string without its surrounding quotes.
      def next_string(c)
        q = c
        @pos += 1
        t = ''
        while @pos < @length
          c = @sql[@pos, 1]
          case c
          when q
            if @pos + 1 < @length && @sql[@pos + 1, 1] == q # double quote
              t += q
              @pos += 1
            else
              @pos += 1
              return t
            end
          when '\\'
            @pos += 1
            return t if @pos >= @length
            t << @sql[@pos, 1]
          else
            t << c
          end
          @pos += 1
        end
        raise "unterminated string in SQL: #{@sql}"
      end

      def identifier_char?(c)
        c =~ /[\.a-zA-Z0-9]/ ? true : false
      end

      def quote?(c)
        c == '"' || c == "'"
      end

      def next_token
        return @extra_tokens.pop unless @extra_tokens.empty?

        skip_whitespace
        c = @sql[@pos, 1]
        return next_string(c) if quote?(c)

        first_is_identifier_char = identifier_char?(c)
        t = c
        @pos += 1
        while @pos < @length
          c = @sql[@pos, 1]
          break if c == ' '

          this_is_identifier_char = identifier_char?(c)
          break if first_is_identifier_char != this_is_identifier_char && @length > 0
          break if !this_is_identifier_char && quote?(c)

          t << c
          @pos += 1
        end

        case t
        when ''
          nil
        when /^\d+$/
          t.to_i
        else
          t
        end
      end
        
    end

    # Only parses really, really simple WHERE clauses right now. The parser
    # returns a hash suitable for use by Mongo.
    class Parser

      def self.parse_where(sql, remove_table_names=false)
        Parser.new(Tokenizer.new(sql)).parse_where(remove_table_names)
      end

      def initialize(tokenizer)
        @tokenizer = tokenizer
      end

      # We have already read the first '(', read up to the matching one and
      # return an array of values.
      def read_array
        vals = []
        while @tokenizer.more?
          vals.push(@tokenizer.next_token)
          sep = @tokenizer.next_token
          return vals if sep == ')'
          raise "missing ',' in 'in' list of values: #{@tokenizer.sql}" unless sep == ','
        end
        raise "missing ')' at end of 'in' list of values: #{@tokenizer.sql}"
      end

      def regexp_from_string(str)
        if str[0,1] == '%'
          str = str[1..-1]
        else
          str = '^' + str
        end

        if str[-1,1] == '%'
          str = str[0..-2]
        else
          str = str + '$'
        end
        Regexp.new(str, Regexp::IGNORECASE)
      end

      def parse_where(remove_table_names=false)
        filters = {}
        done = false
        while !done && @tokenizer.more?
          name = @tokenizer.next_token
          raise "sql parser can't handle nested stuff yet: #{@tokenizer.sql}" if name == '('
          name.sub!(/.*\./, '') if remove_table_names # Remove "schema.table." from "schema.table.col"

          op = @tokenizer.next_token
          op += (' ' + @tokenizer.next_token) if op.downcase == 'not'
          op = op.downcase

          val = @tokenizer.next_token

          case op
          when "="
            filters[name] = val
          when "<"
            filters[name] = { :$lt => val }
          when "<="
            filters[name] = { :$lte => val }
          when ">"
            filters[name] = { :$gt => val }
          when ">="
            filters[name] = { :$gte  => val }
          when "<>", "!="
            filters[name] = { :$ne => val }
          when "like"
            filters[name] = regexp_from_string(val)
          when "in"
            raise "'in' must be followed by a list of values: #{@tokenizer.sql}" unless val == '('
            filters[name] = { :$in => read_array }
          else
            raise "can't handle sql operator [#{op}] yet: #{@tokenizer.sql}"
          end

          break unless @tokenizer.more?

          tok = @tokenizer.next_token.downcase
          case tok
          when 'and'
            next
          when 'or'
              raise "sql parser can't handle ors yet: #{@tokenizer.sql}"
          when 'order', 'group', 'limit'
            @tokenizer.add_extra_token(tok)
            done = true
          else
            raise "can't handle [#{tok}] yet"
          end
        end
        filters
      end
    end

  end
end
