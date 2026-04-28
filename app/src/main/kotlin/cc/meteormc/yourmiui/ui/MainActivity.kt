package cc.meteormc.yourmiui.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.databinding.ActivityMainBinding
import cc.meteormc.yourmiui.helper.SysVersion
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkSystem()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val controller = (fragment as? NavHostFragment)?.navController ?: return
        binding.nav.setupWithNavController(controller)
    }

    private fun checkSystem() {
        val check = when (val current = SysVersion.getCurrent()) {
            SysVersion.HYPEROS -> getString(R.string.syscheck_hyperos)
            SysVersion.OTHER -> getString(R.string.syscheck_unknown_system)
            SysVersion.UNSUPPORTED -> getString(R.string.syscheck_unsupported_version, current.code)
            else -> return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.syscheck_title)
            .setMessage(check)
            .setCancelable(false)
            .setPositiveButton(R.string.syscheck_exit) { _, _ -> finishAffinity() }
            .setNegativeButton(R.string.syscheck_ignore, null)
            .show()
    }
}