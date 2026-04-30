package cc.meteormc.yourmiui.ui.fragment

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.databinding.FragmentManagerBinding
import cc.meteormc.yourmiui.store.HostStore
import cc.meteormc.yourmiui.ui.adapter.ScopeAdapter

class ManagerFragment : BaseFragment<FragmentManagerBinding>({ inflater, container ->
    FragmentManagerBinding.inflate(inflater, container, false)
}) {
    override fun onCreate(): View {
        val emptyView = binding.emptyView
        val scopeList = binding.scopeList
        emptyView.visibility = View.VISIBLE
        scopeList.layoutManager = LinearLayoutManager(requireContext())
        HostStore.scopes.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe
            emptyView.visibility = View.GONE
            scopeList.visibility = View.VISIBLE
            scopeList.adapter = ScopeAdapter(it)
        }

        return binding.root
    }
}