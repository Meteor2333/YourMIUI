package cc.meteormc.yourmiui.ui.fragment

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.databinding.FragmentManagerBinding
import cc.meteormc.yourmiui.store.HostStore
import cc.meteormc.yourmiui.ui.adapter.CategoryAdapter

class ManagerFragment : BaseFragment<FragmentManagerBinding>({ inflater, container ->
    FragmentManagerBinding.inflate(inflater, container, false)
}) {
    override fun onCreate(): View {
        val scopeList = binding.scopeList
        scopeList.layoutManager = LinearLayoutManager(requireContext())
        HostStore.features.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe
            scopeList.visibility = View.VISIBLE
            scopeList.adapter = CategoryAdapter(it.keys.toList())
        }

        return binding.root
    }
}