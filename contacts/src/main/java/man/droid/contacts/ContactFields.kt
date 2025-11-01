package man.droid.contacts

class ContactFields(
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,

    val mobile: String? = null,
    val work: String? = null,
    val fax: String? = null,
    val otherPhone: String? = null,

    val email1: String? = null,
    val email2: String? = null,
    val otherEmail: String? = null,

    val address1: String? = null,
    val address2: String? = null,
    val city: String? = null,
    val postcode: String? = null,
    val state: String? = null,
    val country: String? = null,
    val otherAddress: String? = null,

    val websites: List<String> = listOf(),

    val companyEmail: String? = null,
    val companyPhone1: String? = null,
    val companyFax: String? = null,
    val companyDesignation: String? = null,
    val companyDepartment: String? = null,
    val companyName: String? = null,

    val companyAddress1: String? = null,
    val companyAddress2: String? = null,
    val companyCity: String? = null,
    val companyPostcode: String? = null,
    val companyState: String? = null,
    val companyCountry: String? = null,
    val companyOtherAddress: String? = null
) {
    val phoneList: List<String>
        get() = listOfNotNull(mobile, work, fax, otherPhone, companyPhone1, companyFax)

    val emailList: List<String>
        get() = listOfNotNull(email1, email2, otherEmail, companyEmail)

    val addressList: List<String>
        get() = listOfNotNull(otherAddress, address1, address2, city, postcode, state, country)

    val addressString: String
        get() {
            var formattedAddressString = ""
            when {
                !address1.isNullOrBlank() && !address2.isNullOrBlank() -> {
                    formattedAddressString += if (address1.lastOrNull() == ',') {
                        "$address1 $address2"
                    } else {
                        "$address1, $address2"
                    }
                }

                !address1.isNullOrBlank() -> {
                    formattedAddressString += "$address1"
                }

                !companyAddress2.isNullOrBlank() -> {
                    formattedAddressString += "$address2"
                }
            }
            if (!otherAddress.isNullOrBlank()) {
                formattedAddressString += " $otherAddress"
            }
            return formattedAddressString
        }

    val companyAddressList: List<String>
        get() = listOfNotNull(
            companyOtherAddress, companyAddress1, companyAddress2, companyCity,
            companyPostcode, companyState, companyCountry
        )

    val companyAddressString: String
        get() {
            var formattedCompanyAddressString = ""
            when {
                !companyAddress1.isNullOrBlank() && !companyAddress2.isNullOrBlank() -> {
                    formattedCompanyAddressString += if (companyAddress1.lastOrNull() == ',') {
                        "$companyAddress1 $companyAddress2"
                    } else {
                        "$companyAddress1, $companyAddress2"
                    }
                }

                !companyAddress1.isNullOrBlank() -> {
                    formattedCompanyAddressString += "$companyAddress1"
                }

                !companyAddress2.isNullOrBlank() -> {
                    formattedCompanyAddressString += "$companyAddress2"
                }
            }
            if (!companyOtherAddress.isNullOrBlank()) {
                formattedCompanyAddressString += " $companyOtherAddress"
            }
            return formattedCompanyAddressString
        }
}