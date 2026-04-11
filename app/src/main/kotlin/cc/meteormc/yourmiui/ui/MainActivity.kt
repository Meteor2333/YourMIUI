package cc.meteormc.yourmiui.ui

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.databinding.ActivityMainBinding
import cc.meteormc.yourmiui.helper.MIUIVersion
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkSystem()) return
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val controller = (fragment as? NavHostFragment)?.navController ?: return
        binding.nav.setupWithNavController(controller)
    }

    private fun checkSystem(): Boolean {
        val check = when (MIUIVersion.currentVersion) {
            MIUIVersion.UNKNOWN -> R.string.check_unknown
            MIUIVersion.UNSUPPORTED -> R.string.check_unsupported
            else -> return true
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.check_title)
            .setMessage(check)
            .setCancelable(false)
            .setPositiveButton(R.string.check_exit) { _, _ ->
                finishAffinity()
                exitProcess(0)
            }
            .show()
        return false
    }
}