package com.mycompany.thesis1

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    //private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        val firebaseAuth = FirebaseAuth.getInstance()
        navGraph.startDestination =
            if (firebaseAuth.currentUser != null)
                R.id.mainFragment
            else
                R.id.authFragment2
        navController.graph = navGraph
        //nav_view.setupWithNavController(navController)
//        appBarConfiguration = AppBarConfiguration(setOf(
//            R.id.homeFragment,
//            R.id.authFragment2
//        ), drawer_layout)

        //setupActionBarWithNavController(navController, appBarConfiguration)

    }

//    override fun openDrawer() {
//        drawer_layout.open()
//        nav_view.setNavigationItemSelectedListener {
//            when(it.itemId) {
//                R.id.menuItemGroup -> {
//                    Log.d("myTag", "openDrawer: groups clicked")}
//            }
//            false
//        }
//    }
//
//    override fun setDrawerHeader(userName: String, userId: String) {
//        drawer_layout.apply {
//            tvUserName.text = userName
//            tvUserEmail.text = userId
//        }
//    }

//    override fun lockDrawer() {
//        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
//    }
//
//    override fun unlockDrawer() {
//        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
//    }

//    override fun removeDrawer() {
//        appBarConfiguration = AppBarConfiguration(setOf(
//            R.id.homeFragment,
//            R.id.searchFragment,
//            R.id.authFragment
//        ))
//        setSupportActionBar(toolbar)
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
//    }
//
//    override fun addDrawer() {
//        appBarConfiguration = AppBarConfiguration(setOf(
//            R.id.homeFragment,
//            R.id.searchFragment,
//            R.id.authFragment
//        ), drawer_layout)
//        setSupportActionBar(toolbar)
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
//    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }
}