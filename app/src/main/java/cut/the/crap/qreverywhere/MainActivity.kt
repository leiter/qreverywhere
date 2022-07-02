package cut.the.crap.qreverywhere

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import cut.the.crap.qreverywhere.databinding.ActivityMainBinding
import cut.the.crap.qreverywhere.qrpreferences.SettingsFragmentDirections
import dagger.hilt.android.AndroidEntryPoint


val navSelectorCreate = arrayListOf(
    R.id.createEmailQrCodeFragment,
    R.id.createOneLinerFragment,
    R.id.createQrCodeFragment
)

val navSelectorHistory = arrayListOf(
    R.id.qrHistoryFragment,
    R.id.detailViewFragment
)
val navSelectorScanQr = arrayListOf(
    R.id.scanQrFragment,
    R.id.settingsFragment
)

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val binding: ActivityMainBinding by viewBinding {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->

            when (destination.id) {
                in navSelectorCreate -> {
                    navView.menu.getItem(1).isChecked = true
                }
                in navSelectorHistory -> {
                    navView.menu.getItem(2).isChecked = true
                }
                R.id.scanQrFragment-> {
                    navView.menu.getItem(0).isChecked = true
                }
            }
        }

        navView.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.scanQrFragment,
                R.id.createQrCodeFragment,
                R.id.qrHistoryFragment,
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
//            R.id.action_about -> {
//                findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_scanQrFragment_to_SettingsFragment)
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}