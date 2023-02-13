package cut.the.crap.qreverywhere.utils

import android.content.*
import android.net.Uri
import android.os.RemoteException
import android.provider.Contacts.People
import android.provider.ContactsContract
import android.provider.ContactsContract.*
import android.provider.ContactsContract.CommonDataKinds.*
import android.provider.ContactsContract.Contacts.Data


class ContactManager {

    fun addContact(context: Context, name: String, phone: String) : Uri?{
        val values = ContentValues()
        values.put(People.NUMBER, phone)
        values.put(People.TYPE, Phone.TYPE_CUSTOM)
        values.put(People.LABEL, name)
        values.put(People.NAME, name)
        val dataUri: Uri? = context.contentResolver.insert(People.CONTENT_URI, values)
        val  updateUri = dataUri?.let { Uri.withAppendedPath(it, People.Phones.CONTENT_DIRECTORY) }
        values.clear()
        values.put(People.Phones.TYPE, People.TYPE_MOBILE)
        values.put(People.NUMBER, phone)
        return updateUri?.let { context.contentResolver.insert(updateUri, values) }
    }

    fun insertContact(context: Context ) {

        val ops = arrayListOf<ContentProviderOperation>()
        val rawContactInsertIndex = ops.size

        //prepare
        ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
            .withValue(RawContacts.ACCOUNT_TYPE, null)
            .withValue(RawContacts.ACCOUNT_NAME, null).build());


        //Phone Number
        ops.add(ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                rawContactInsertIndex)
            .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
            .withValue(Phone.NUMBER, "9X-XXXXXXXXX")
            .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
            .withValue(Phone.TYPE, "2").build());

        //Display name/Contact name
        ops.add(ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(
                Data.RAW_CONTACT_ID,
                rawContactInsertIndex)
            .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
            .withValue(StructuredName.DISPLAY_NAME, "Mike Sullivan")
            .build());


        //Email details
        ops.add(ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                rawContactInsertIndex)
            .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
            .withValue(Email.DATA, "abc@aho.com")
            .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
            .withValue(Email.TYPE, "2").build());


        //Postal Address
        ops.add(ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                rawContactInsertIndex)
            .withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE )
            .withValue(StructuredPostal.POBOX, "Postbox")

            .withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE )
            .withValue(StructuredPostal.STREET, "street")

            .withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE )
            .withValue(StructuredPostal.CITY, "city")

            .withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE )
            .withValue(StructuredPostal.REGION, "region")

            .withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE )
            .withValue(StructuredPostal.POSTCODE, "postcode")

            .withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE )
            .withValue(StructuredPostal.COUNTRY, "country")

            .withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE )
            .withValue(StructuredPostal.TYPE, "3")


            .build());


        //Organization details
        ops.add(ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
            .withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE )
            .withValue(Organization.COMPANY, "Devindia")
            .withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE )
            .withValue(Organization.TITLE, "Developer")
            .withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE )
            .withValue(Organization.TYPE, "0")

            .build());

        //Instant messenger details
        ops.add(ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(
                Data.RAW_CONTACT_ID,
                rawContactInsertIndex)
            .withValue(Data.MIMETYPE, Im.CONTENT_ITEM_TYPE)
            .withValue(Im.DATA, "ImName")
            .withValue(Data.MIMETYPE, Im.CONTENT_ITEM_TYPE )
            .withValue(Im.DATA5, "3")  // Skype, yahoo,
            .build());


        try {
            val res: Array<ContentProviderResult> = context.contentResolver.applyBatch(
                AUTHORITY, ops
            )
        } catch (e: RemoteException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: OperationApplicationException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }


    }





}