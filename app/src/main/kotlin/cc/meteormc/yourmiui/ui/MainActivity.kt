package cc.meteormc.yourmiui.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val controller = (fragment as? NavHostFragment)?.navController ?: return
        binding.nav.setupWithNavController(controller)
    }
}