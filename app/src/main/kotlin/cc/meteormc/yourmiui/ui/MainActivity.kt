package cc.meteormc.yourmiui.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.databinding.ActivityMainBinding
import cc.meteormc.yourmiui.store.HostStore

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val controller = (fragment as NavHostFragment).navController
        binding.nav.setupWithNavController(controller)

        HostStore.init()
    }
}