package com.jehubasa.adassubmissionlog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import com.jehubasa.adassubmissionlog.R

class AboutFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true).setDuration(500)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false).setDuration(500)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true).setDuration(500)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false).setDuration(500)

//        exitTransition = MaterialFadeThrough()
//        enterTransition = MaterialFadeThrough()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }
}