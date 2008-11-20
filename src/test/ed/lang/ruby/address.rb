require 'xgen/mongo'            # not needed in Rails code

class Address < XGen::Mongo::Subobject

  fields :street, :city, :state, :postal_code
  belongs_to :student

  def to_s
    "#{street}\n#{city}, #{state} #{postal_code}"
  end

end
