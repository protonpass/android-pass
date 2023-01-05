package me.proton.pass.autofill.e2e

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class E2EActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Text(text = "E2E activity")
        }
    }
}
