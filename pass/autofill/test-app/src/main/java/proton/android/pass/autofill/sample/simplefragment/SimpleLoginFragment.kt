/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill.sample.simplefragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import androidx.fragment.app.Fragment
import proton.android.pass.autofill.sample.LoginResultFragment
import proton.android.pass.autofill.sample.R
import proton.android.pass.autofill.sample.databinding.FragmentSimpleLoginBinding

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
