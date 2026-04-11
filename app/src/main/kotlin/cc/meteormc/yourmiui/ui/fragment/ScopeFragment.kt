package cc.meteormc.yourmiui.ui.fragment

import android.view.View
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.databinding.FragmentScopeBinding
import cc.meteormc.yourmiui.ui.adapter.FeatureAdapter
import cc.meteormc.yourmiui.ui.data.FeatureNavConfig

class ScopeFragment : BaseFragment<FragmentScopeBinding>({ inflater, container ->
    FragmentScopeBinding.inflate(inflater, container, false)
}) {
    override fun onCreate(): View {
        val title = arguments?.getString("title") ?: "Unknown Scope"
        val features = if (android.os.Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelableArrayList("features", FeatureNavConfig::class.java)
        } else {
            @Suppress("DEPRECATION", "UNCHECKED_CAST")
            arguments?.getParcelableArrayList<FeatureNavConfig>("features") as? List<FeatureNavConfig>
        } ?: emptyList()

        val scopeToolbar = binding.scopeToolbar
        scopeToolbar.title = title
        scopeToolbar.setNavigationOnClickListener {
            it.findNavController().navigateUp()
        }

        val featureList = binding.featureList
        featureList.adapter = FeatureAdapter(features)
        featureList.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }
}