package cc.meteormc.yourmiui.ui.fragment

import android.view.View
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.api.data.FeatureInfo
import cc.meteormc.yourmiui.databinding.FragmentCategoryBinding
import cc.meteormc.yourmiui.ui.adapter.FeatureAdapter

class CategoryFragment : BaseFragment<FragmentCategoryBinding>({ inflater, container ->
    FragmentCategoryBinding.inflate(inflater, container, false)
}) {
    private val title: String
        get() = arguments?.getString("title") ?: "Unknown Category"
    private val features: List<FeatureInfo>
        get() = arguments?.getSerializable("features") as List<FeatureInfo>? ?: emptyList()

    override fun onCreate(): View {
        val scopeToolbar = binding.categoryToolbar
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