class Address < XGen::Mongo::Base

  fields :street, :city, :state, :postal_code

  def to_s
    "#{street}\n#{city}, #{state} #{postal_code}"
  end

end
