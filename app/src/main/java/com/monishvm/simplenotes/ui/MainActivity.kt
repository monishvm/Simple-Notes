package com.monishvm.simplenotes.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.monishvm.simplenotes.R
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.view.*
import java.security.Permission
import java.security.Permissions

class MainActivity : DaggerAppCompatActivity() {

    lateinit var navController: NavController
    private lateinit var fab: com.google.android.material.floatingactionbutton.FloatingActionButton

    // Method #1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1
            )

        setSupportActionBar(findViewById(R.id.toolbar))

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val config = AppBarConfiguration(navController.graph)
        findViewById<Toolbar>(R.id.toolbar).setupWithNavController(navController, config)

        fab = findViewById(R.id.fab)

        //Floating action button will redirect to Add New Notes Fragment #AddFragment
        fab.setOnClickListener {
            onFloatingClicked()
        }
    }

    // Method #2
    private fun onFloatingClicked() {
        navController.navigate(R.id.action_listFragment_to_addFragment)
        hideFloatingButton()
    }

    fun hideFloatingButton() {
        fab.hide()
    }

    // Method #3
    fun showFloatingButton() {
        fab.show()
        fab.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

}