def valid_email?(email)
  email.size < 100 && email =~ /.@.+\../ && email.count('@') == 1
end
