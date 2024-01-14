package com.example.nfcdemo

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.nfcdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        val errorDetected = "No NFC Tag Detected"
        val writeSuccess = "Text Written Successfully!"
        val writeError = "Error during writing, Try again!"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var writingTagFilter: IntentFilter
    private var writeModel = false
    private lateinit var myTag: Tag


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activeButton.setOnClickListener {
            try {
                if(myTag==null){
                    Toast.makeText(this, errorDetected, Toast.LENGTH_SHORT).show()
                }else{

                    if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
                        // Write to the discovered NFC tag
                        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                        writeNFC(binding.editMessage.text.toString(),tag)
                        Toast.makeText(this, writeSuccess, Toast.LENGTH_SHORT).show()
                    }


                }

            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        nfcAdapter=NfcAdapter.getDefaultAdapter(this)
        if(nfcAdapter==null){
            Toast.makeText(this, "this device not support NFC", Toast.LENGTH_SHORT).show()
            finish()
        }
        onNewIntent(intent)
        pendingIntent = PendingIntent.getActivity(this,0, Intent(this,javaClass)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE)
        val tagDetected=IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT)
    }


 /*   fun readFromIntent (intent:Intent) {
        val action = intent.action
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action
            || NfcAdapter.ACTION_TECH_DISCOVERED == action
            || NfcAdapter.ACTION_NDEF_DISCOVERED == action
        ) {
            val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            val msgs: Array<NdefMessage>
            if (rawMsgs != null) {
                msgs = NdefMessage(rawMsgs.size)

            }
        }
    }*/

    override fun onPause() {
        super.onPause()
        // Disable NFC foreground dispatch
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onResume() {
        super.onResume()

        // Enable NFC foreground dispatch
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.let {
            val pendingIntent = PendingIntent.getActivity(
                this, 0, Intent(this, javaClass)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
            )
            it.enableForegroundDispatch(this, pendingIntent, null, null)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMessages != null) {
                val messages = Array(rawMessages.size) {
                    rawMessages[it] as NdefMessage }
                processNFCData(messages)
            }
        }


    }

    private fun processNFCData(messages: Array<NdefMessage>) {
        for (message in messages) {
            val records = message.records
            for (record in records) {
                // Process the NDEF record data
                val payload = record.payload
                val text = String(payload)
                binding.nfcContents.text = text
            }
        }
    }

    private fun writeNFC(editText: String, tag: Tag?) {
        val message = NdefMessage(
            arrayOf(NdefRecord.createTextRecord("en", editText))
        )

        val ndef = Ndef.get(tag)
        ndef?.let {
            it.connect()
            it.writeNdefMessage(message)
            it.close()
            Toast.makeText(this, writeSuccess, Toast.LENGTH_SHORT).show()
            // Handle successful write
        } ?: run {
            Toast.makeText(this, "The tag does not support NDEF", Toast.LENGTH_SHORT).show()
            // The tag does not support NDEF
            // Handle accordingly
        }
    }

}