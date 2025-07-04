import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var outputView: TextView
    private val PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val smsBtn = findViewById<Button>(R.id.btnSms)
        val contactBtn = findViewById<Button>(R.id.btnKontak)
        val emailBtn = findViewById<Button>(R.id.btnEmail)
        outputView = findViewById(R.id.textOutput)

        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.GET_ACCOUNTS
                ),
                PERMISSION_REQUEST_CODE
            )
        }

        smsBtn.setOnClickListener { readSms() }
        contactBtn.setOnClickListener { readContacts() }
        emailBtn.setOnClickListener { readEmails() }
    }

    private fun checkPermissions(): Boolean {
        val sms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        val contact = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        val email = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
        return sms == PackageManager.PERMISSION_GRANTED &&
               contact == PackageManager.PERMISSION_GRANTED &&
               email == PackageManager.PERMISSION_GRANTED
    }

    private fun readSms() {
        val smsUri = Telephony.Sms.Inbox.CONTENT_URI
        val cursor: Cursor? = contentResolver.query(smsUri, null, null, null, null)
        val smsList = StringBuilder()

        if (cursor != null && cursor.moveToFirst()) {
            val bodyIdx = cursor.getColumnIndex("body")
            val addressIdx = cursor.getColumnIndex("address")

            var count = 0
            do {
                val body = cursor.getString(bodyIdx)
                val address = cursor.getString(addressIdx)
                smsList.append("Dari: $address\nPesan: $body\n\n")
                count++
            } while (cursor.moveToNext() && count < 5) // Hanya 5 SMS
            cursor.close()
        }

        outputView.text = smsList.toString()
    }

    private fun readContacts() {
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )
        val contactList = StringBuilder()

        if (cursor != null && cursor.moveToFirst()) {
            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            var count = 0
            do {
                val name = cursor.getString(nameIdx)
                val number = cursor.getString(numberIdx)
                contactList.append("Nama: $name\nNomor: $number\n\n")
                count++
            } while (cursor.moveToNext() && count < 10) // Maksimal 10 kontak
            cursor.close()
        }

        outputView.text = contactList.toString()
    }

    private fun readEmails() {
        val manager = AccountManager.get(this)
        val accounts: Array<Account> = manager.accounts
        val emailList = StringBuilder("Akun Email di Perangkat:\n\n")

        for (acc in accounts) {
            if (acc.type.contains("google")) {
                emailList.append(acc.name).append("\n")
            }
        }

        outputView.text = emailList.toString()
    }
}
