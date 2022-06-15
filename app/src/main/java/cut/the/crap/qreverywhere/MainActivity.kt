package cut.the.crap.qreverywhere

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import cut.the.crap.qreverywhere.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        with(binding) {


            fab.setOnClickListener { view ->

                readBarcode()

//                val intent = Intent("com.google.zxing.client.android.SCAN")
//                intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
//                startActivityForResult(intent,0)
//
//                val intentIntegrator = IntentIntegrator(this@MainActivity)
//                intentIntegrator.setPrompt("Scan a barcode or QR Code")
//                intentIntegrator.setOrientationLocked(true)
//                intentIntegrator.initiateScan()
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
            }

        }


    }


    private val barcodeScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->

        if( result.resultCode == Activity.RESULT_OK ){

            val barcodeRawValue = result.data?.getStringExtra(
                BarCodeScannerActivity.RESULT_BARCODE_RAW_VALUE
            ) ?: ""

        }
    }

    private fun readBarcode(){
        val intent = Intent(this, BarCodeScannerActivity::class.java)
        barcodeScannerLauncher.launch(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}