package me.proton.core.pass.autofill.sample.simplefragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import androidx.fragment.app.Fragment
import me.proton.core.pass.autofill.sample.LoginResultFragment
import me.proton.core.pass.autofill.sample.R
import me.proton.core.pass.autofill.sample.databinding.FragmentSimpleLoginBinding

class SimpleLoginFragment : Fragment() {

    private var binding: FragmentSimpleLoginBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSimpleLoginBinding.inflate(inflater, container, false)
        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.loginButton?.setOnClickListener {
            val autofillManager = requireContext().getSystemService(AutofillManager::class.java)
            requireFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, LoginResultFragment())
                // Needed to trigger save, it's either this or finishing the current activity
                .runOnCommit { autofillManager.commit() }
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
