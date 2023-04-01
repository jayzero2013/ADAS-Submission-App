package com.jehubasa.adassubmissionlog

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseAuth
import com.jehubasa.adassubmissionlog.databinding.ActivityMainBinding
import com.jehubasa.adassubmissionlog.fragments.AboutFragment
import com.jehubasa.adassubmissionlog.fragments.CheckStatusFragment
import com.jehubasa.adassubmissionlog.fragments.QrGenFragment
import com.jehubasa.adassubmissionlog.fragments.QrScanFragment

class MainActivity : AppCompatActivity(),
    com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private val INTERNET_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {
            replace(R.id.navFragmentContainer, QrScanFragment())
        }

        binding.navBar.setOnItemSelectedListener(this)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.INTERNET
            ) == PackageManager.PERMISSION_DENIED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.INTERNET),
                INTERNET_PERMISSION_CODE
            )

        } else {
            Log.d("ASP", "INTERNET GRANTED ALREADY")
        }

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(getString(R.string.email), getString(R.string.password))
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                } else {
                    // Authentication failed
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == INTERNET_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("ASP", "INTERNET GRANTED ")
            } else {
                Log.d("ASP", "INTERNET DENIED ")
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem) = when (item.itemId) {


        R.id.button_scan -> {
            supportFragmentManager.commit {
                replace(R.id.navFragmentContainer, QrScanFragment())
            }
            true
        }

        R.id.button_qr_gen -> {
            supportFragmentManager.commit {
                replace(R.id.navFragmentContainer, QrGenFragment())
            }
            true
        }

        R.id.button_check_status -> {
            supportFragmentManager.commit {
                replace(R.id.navFragmentContainer, CheckStatusFragment())
            }
            true
        }

        R.id.button_about -> {
            supportFragmentManager.commit {
                replace(R.id.navFragmentContainer, AboutFragment())
            }
            true
        }

        else -> false

    }

}
