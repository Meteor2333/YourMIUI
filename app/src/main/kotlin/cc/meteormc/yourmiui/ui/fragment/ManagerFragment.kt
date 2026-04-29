package cc.meteormc.yourmiui.ui.fragment

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.YourMIUI
import cc.meteormc.yourmiui.databinding.FragmentManagerBinding
import cc.meteormc.yourmiui.ui.adapter.ScopeAdapter

class ManagerFragment : BaseFragment<FragmentManagerBinding>({ inflater, container ->
    FragmentManagerBinding.inflate(inflater, container, false)
}) {
    override fun onCreate(): View {
        val scopes = YourMIUI.get().scopes
        if (scopes.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            return binding.root
        }

        val scopeList = binding.scopeList
        scopeList.visibility = View.VISIBLE
        scopeList.adapter = ScopeAdapter(scopes)
        scopeList.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }
}