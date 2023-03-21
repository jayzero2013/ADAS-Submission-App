package com.jehubasa.adassubmissionlog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.jehubasa.adassubmissionlog.databinding.ActivityMainBinding
import com.jehubasa.adassubmissionlog.fragments.CheckStatusFragment
import com.jehubasa.adassubmissionlog.fragments.LogPrintingFragment
import com.jehubasa.adassubmissionlog.fragments.QrGenFragment
import com.jehubasa.adassubmissionlog.fragments.QrScanFragment

class MainActivity : AppCompatActivity(),
    com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {
            replace(R.id.navFragmentContainer, QrScanFragment())
        }

        binding.navBar.setOnItemSelectedListener(this)


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
                replace(R.id.navFragmentContainer, LogPrintingFragment())
            }
            true
        }

        R.id.button_log_print -> {
            supportFragmentManager.commit {
                replace(R.id.navFragmentContainer, CheckStatusFragment())
            }
            true
        }

        else -> false

    }

}
