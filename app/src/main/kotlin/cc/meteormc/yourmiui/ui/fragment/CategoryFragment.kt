package cc.meteormc.yourmiui.ui.fragment

import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.databinding.FragmentCategoryBinding
import cc.meteormc.yourmiui.store.HostStore
import cc.meteormc.yourmiui.ui.adapter.FeatureAdapter

class CategoryFragment : BaseFragment<FragmentCategoryBinding>({ inflater, container ->
    FragmentCategoryBinding.inflate(inflater, container, false)
}) {
    private val title: String?
        get() = arguments?.getString("title")
    private val category: Category?
        get() = arguments?.getInt("category", -1)
            ?.takeIf { it >= 0 }
            ?.let { Category.entries[it] }

    override fun onCreate(): View {
        val features = HostStore.features.value
        if (title == null || category == null || features == null) {
            findNavController().navigateUp()
            return binding.root
        }

        val categoryToolbar = binding.categoryToolbar
        categoryToolbar.title = title
        categoryToolbar.setNavigationOnClickListener {
            it.findNavController().navigateUp()
        }

        val featureList = binding.featureList
        featureList.layoutManager = LinearLayoutManager(requireContext())
        featureList.adapter = FeatureAdapter(features[category] ?: emptyList())

        return binding.root
    }
}