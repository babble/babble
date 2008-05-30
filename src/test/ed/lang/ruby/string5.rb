TOKEN_LENGTH = 4

characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890'
puts characters.length
puts characters[5]
puts characters[5..5]
puts characters[5..6]

# def random_token
#   characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890'
#   temp_token = ''
#   TOKEN_LENGTH.times do
#     pos = 5#rand(characters.length)
#     puts pos
#     puts characters[5..5]
#     temp_token += characters[pos]
#   end
#   temp_token
# end

# puts random_token
