package cut.the.crap.qreverywhere

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import cut.the.crap.qreverywhere.databinding.ActivityMainBinding
import cut.the.crap.qreverywhere.utils.ui.navSelectorCreate
import cut.the.crap.qreverywhere.utils.ui.selectHistory
import cut.the.crap.qreverywhere.utils.ui.viewBinding

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
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            when {
                destination.id in navSelectorCreate -> {
                    navView.menu.getItem(1).isChecked = true
                }
                selectHistory(destination.id, arguments) -> {
                    navView.menu.getItem(2).isChecked = true
                }
                destination.id == R.id.scanQrFragment -> {
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}