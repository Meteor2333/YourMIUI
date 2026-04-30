package cc.meteormc.yourmiui.ui.fragment

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.databinding.FragmentManagerBinding
import cc.meteormc.yourmiui.store.DataField
import cc.meteormc.yourmiui.store.HostStore
import cc.meteormc.yourmiui.ui.adapter.ScopeAdapter

class ManagerFragment : BaseFragment<FragmentManagerBinding>({ inflater, container ->
    FragmentManagerBinding.inflate(inflater, container, false)
}) {
    override fun onCreate(): View {
        val emptyView = binding.emptyView
        emptyView.visibility = View.VISIBLE
        HostStore.scopes.observe(DataField.ObserveType.INIT) {
            val scopes = HostStore.scopes.value
            if (scopes.isEmpty()) return@observe
            emptyView.visibility = View.GONE
            val scopeList = binding.scopeList
            scopeList.visibility = View.VISIBLE
            scopeList.adapter = ScopeAdapter(scopes)
            scopeList.layoutManager = LinearLayoutManager(requireContext())
        }

        return binding.root
    }
}