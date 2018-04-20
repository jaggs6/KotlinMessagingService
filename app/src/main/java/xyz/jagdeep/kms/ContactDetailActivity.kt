package xyz.jagdeep.kms

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.telephony.SmsManager
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_contact_detail.*
import xyz.jagdeep.kms.dummy.DummyContent

/**
 * An activity representing a single Contact detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [ContactListActivity].
 */
class ContactDetailActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_REQUEST_SEND_SMS: Int = 456

    private fun sendSMS(phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    MY_PERMISSIONS_REQUEST_SEND_SMS)

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            val sms = SmsManager.getDefault()
            sms.sendTextMessage(phoneNumber, null, message, null, null)
        }
    }

    private lateinit var allContacts: ArrayList<DummyContent.ContactItem>

    private fun fetchContacts() {
        allContacts = ArrayList()
        val phones = contentResolver?.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        while (phones!!.moveToNext()) {
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NAME_RAW_CONTACT_ID))
            val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            allContacts.add(DummyContent.ContactItem(id, name, phoneNumber))
        }
        phones.close()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_SEND_SMS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    fab.callOnClick()

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "We really need the SMS permission.", Toast.LENGTH_LONG).show()
                }
                return
            }

        // Add other 'when' lines to check for other
        // permissions this app might request.

            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchContacts()
    }

    private var currentContact: DummyContent.ContactItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_detail)
        setSupportActionBar(detail_toolbar)

        fetchContacts()

        fab.setOnClickListener { view ->
            sendSMS(currentContact?.details!!, "YO")
        }

        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            val fragment = ContactDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ContactDetailFragment.ARG_ITEM_ID,
                            intent.getStringExtra(ContactDetailFragment.ARG_ITEM_ID))
                    val searchVal = intent.getStringExtra(ContactDetailFragment.ARG_ITEM_ID)
                    currentContact = allContacts.find { it.id == searchVal }


                }
            }

            supportFragmentManager.beginTransaction()
                    .add(R.id.contact_detail_container, fragment)
                    .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {
                    // This ID represents the Home or Up button. In the case of this
                    // activity, the Up button is shown. For
                    // more details, see the Navigation pattern on Android Design:
                    //
                    // http://developer.android.com/design/patterns/navigation.html#up-vs-back

                    navigateUpTo(Intent(this, ContactListActivity::class.java))
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
}
