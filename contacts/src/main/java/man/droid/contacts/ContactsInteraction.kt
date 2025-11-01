package man.droid.contacts

import android.Manifest
import android.app.Dialog
import android.content.ContentProviderOperation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import cut.the.crap.qreverywhere.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL

fun saveContactOnDevice(
    context: Context,
    contacts: List<ContactFields>
): Result<List<ContactFields>> {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        return Result.failure(Throwable(message = "WRITE_CONTACTS Permission not granted"))
    }

    val contentResolver = context.contentResolver
    val contentProviderOperation = ArrayList<ContentProviderOperation>()

    contacts.forEach {
        performSaveContact(context, it, contentProviderOperation)
    }

    try {
        contentResolver.applyBatch(ContactsContract.AUTHORITY, contentProviderOperation)
    } catch (exception: Exception) {
        return Result.failure(Throwable(message = exception.localizedMessage))
    } finally {
        return Result.success(contacts)
    }
}

fun performSaveContact(
    context: Context,
    fields: ContactFields,
    contentProviderOperation: ArrayList<ContentProviderOperation>
) {
    val rawContactInsertIndex = contentProviderOperation.size

    /**
     * Content Resolver
     */

    contentProviderOperation.add(
        ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build()
    )

    /**
     * Name
     */

    val nameOperation = ContentProviderOperation
        .newInsert(ContactsContract.Data.CONTENT_URI)
        .withValueBackReference(
            ContactsContract.Contacts.Data.RAW_CONTACT_ID,
            rawContactInsertIndex
        )
        .withValue(
            ContactsContract.Contacts.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
        ).withValue(
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            fields.firstName
        ).withValue(
            ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
            fields.middleName
        ).withValue(
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            fields.lastName
        )

    contentProviderOperation.add(nameOperation.build())

    /**
     * Phone Number
     */

    fields.phoneList.forEach { phoneNumber ->
        val phoneNumberOperation = ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(
                ContactsContract.Data.RAW_CONTACT_ID,
                rawContactInsertIndex
            ).withValue(
                ContactsContract.Contacts.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
            )

        phoneNumberOperation.withValue(
            ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber
        )

        when (phoneNumber) {
            fields.mobile -> {
                phoneNumberOperation.withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
            }

            fields.work -> {
                phoneNumberOperation.withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                )
            }

            fields.fax -> {
                phoneNumberOperation.withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME
                )
            }

            fields.otherPhone -> {
                phoneNumberOperation.withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_OTHER
                )
            }

            fields.companyPhone1 -> {
                phoneNumberOperation.withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN
                )
            }

            fields.companyFax -> {
                phoneNumberOperation.withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK
                )
            }
        }
        contentProviderOperation.add(phoneNumberOperation.build())
    }

    /**
     * Email
     */

    fields.emailList.forEach { email ->
        val emailOperation = ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(
                ContactsContract.Data.RAW_CONTACT_ID,
                rawContactInsertIndex
            ).withValue(
                ContactsContract.Contacts.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
            )

        emailOperation.withValue(ContactsContract.CommonDataKinds.Email.DATA, email)

        when (email) {
            fields.email1 -> emailOperation.withValue(
                ContactsContract.CommonDataKinds.Email.TYPE,
                ContactsContract.CommonDataKinds.Email.TYPE_HOME
            )

            fields.email2 -> emailOperation.withValue(
                ContactsContract.CommonDataKinds.Email.TYPE,
                ContactsContract.CommonDataKinds.Email.TYPE_HOME
            )

            fields.otherEmail -> emailOperation.withValue(
                ContactsContract.CommonDataKinds.Email.TYPE,
                ContactsContract.CommonDataKinds.Email.TYPE_OTHER
            )

            fields.companyEmail -> {
                emailOperation.withValue(
                    ContactsContract.CommonDataKinds.Email.TYPE,
                    ContactsContract.CommonDataKinds.Email.TYPE_WORK
                )
            }
        }

        contentProviderOperation.add(emailOperation.build())
    }

    /**
     * Address - Personal
     */

    if (!fields.addressList.isNullOrEmpty()) {
        val addressOperation = ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(
                ContactsContract.Data.RAW_CONTACT_ID,
                rawContactInsertIndex
            ).withValue(
                ContactsContract.Contacts.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
            ).withValue(
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME
            ).withValue(
                ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                fields.addressString
            ).withValue(
                ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                fields.city
            ).withValue(
                ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
                fields.postcode
            ).withValue(
                ContactsContract.CommonDataKinds.StructuredPostal.REGION,
                fields.state
            ).withValue(
                ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
                fields.country
            )

        contentProviderOperation.add(addressOperation.build())
    }

    /**
     * Address - Company
     */

    if (!fields.companyAddressList.isNullOrEmpty()) {
        val companyAddressOperation = ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(
                ContactsContract.Data.RAW_CONTACT_ID,
                rawContactInsertIndex
            ).withValue(
                ContactsContract.Contacts.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
            ).withValue(
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK
            ).withValue(
                ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                fields.companyAddressString
            ).withValue(
                ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                fields.companyCity
            ).withValue(
                ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
                fields.companyPostcode
            ).withValue(
                ContactsContract.CommonDataKinds.StructuredPostal.REGION,
                fields.companyState
            ).withValue(
                ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
                fields.companyCountry
            )

        contentProviderOperation.add(companyAddressOperation.build())
    }

    /**
     * Website
     */

    fields.websites.forEach { website ->
        val websiteOperation = ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(
                ContactsContract.Data.RAW_CONTACT_ID,
                rawContactInsertIndex
            ).withValue(
                ContactsContract.Contacts.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
            ).withValue(
                ContactsContract.CommonDataKinds.Website.DATA,
                website
            )
        contentProviderOperation.add(websiteOperation.build())
    }

    /**
     * Company
     */

    val organizationOperation = ContentProviderOperation
        .newInsert(ContactsContract.Data.CONTENT_URI)
        .withValueBackReference(
            ContactsContract.Data.RAW_CONTACT_ID,
            rawContactInsertIndex
        ).withValue(
            ContactsContract.Contacts.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
        ).withValue(
            ContactsContract.CommonDataKinds.Organization.COMPANY, fields.companyName
        ).withValue(
            ContactsContract.CommonDataKinds.Organization.TITLE, fields.companyDesignation
        )

    contentProviderOperation.add(organizationOperation.build())

    /**
     * Note
     */

    var noteString = ""

//    if (!fields.companyDepartment.isNullOrBlank()) {
//        noteString += "${
//            context.getString(
//                R.string.department_note,
//                fields.companyDepartment
//            )
//        }\n"
//    }

    val noteOperation = ContentProviderOperation
        .newInsert(ContactsContract.Data.CONTENT_URI)
        .withValueBackReference(
            ContactsContract.Data.RAW_CONTACT_ID,
            rawContactInsertIndex
        ).withValue(
            ContactsContract.Contacts.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
        ).withValue(
            ContactsContract.CommonDataKinds.Note.NOTE,
            noteString
        )

    contentProviderOperation.add(noteOperation.build())
}

fun findContact(context: Context) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CONTACTS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        //showToastyMessage("Permission not Granted")
        return
    }
    val cursor = context.contentResolver.query(
        ContactsContract.Data.CONTENT_URI,
        null,
        ContactsContract.CommonDataKinds.Website.DATA + "=?",
        arrayOf("https://www.helloworld.com/32066907-b648-4a77-8301-b3c437ea3b7f"),
        null,
        null
    ) ?: return

    while (cursor.moveToNext()) {
//        val rawContactId = cursor.getString(
//            cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID)
//        )
//        val displayName = cursor.getString(
//            cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
//        )
        //Timber.i("ID: ${rawContactId}, Display Name: $displayName")
    }
}

fun updateContact(context: Context, rawContactId: String) {
    val contentProviderOperation = ArrayList<ContentProviderOperation>()

    val updateWhere = ContactsContract.Data.RAW_CONTACT_ID + "=? AND " +
        ContactsContract.Contacts.Data.MIMETYPE + "=" +
        "'${ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE}'" + " AND " +
        ContactsContract.CommonDataKinds.Phone.TYPE + "=?"

    val updateParams =
        arrayOf(rawContactId, "${ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE}")

    contentProviderOperation.add(
        ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
            .withSelection(updateWhere, updateParams)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, "+60123456789")
            .build()
    )

    try {
        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, contentProviderOperation)
        //showToastyMessage("Contact Update Successfully")
    } catch (exception: Exception) {
        //showToastyMessage(exception.message.toString())
    }
}

fun deleteAndSaveContact(
    context: Context,
    rawContactId: String,
    fields: ContactFields
) {
    val contentProviderOperation = ArrayList<ContentProviderOperation>()
    val deleteWhere = ContactsContract.Data.RAW_CONTACT_ID + "=?"
    val deleteParams = arrayOf(rawContactId)

    contentProviderOperation.add(
        ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
            .withSelection(deleteWhere, deleteParams)
            .build()
    )

    performSaveContact(context, fields, contentProviderOperation)

    try {
        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, contentProviderOperation)

    } catch (exception: Exception) {
        //showToastyMessage(exception.message.toString())
    }
}

private fun downloadVcfFile() {
    CoroutineScope(Dispatchers.IO).launch {
        val url = "https://srv-store5.gofile.io/download/JXLVFW/vcard.vcf"
        val path =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/contacts.vcf"

        URL(url).openStream().use { input ->
            FileOutputStream(File(path)).use { output ->
                input.copyTo(output)

                val file = File(path)
                file.createNewFile()
                //onMain { saveVcfFile(file) }
            }
        }
    }
}

private fun saveVcfFile(context: Context, savedVCard: File) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            savedVCard
        )
        intent.setDataAndType(uri, context.contentResolver.getType(uri))
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    } catch (exception: Exception) {
//        Dialog(context)
//            .message(text = exception.message.toString())
//            .show()
    }
}