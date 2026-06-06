package cc.meteormc.yourmiui.ui.fragment

import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.databinding.FragmentCategoryBinding
import cc.meteormc.yourmiui.helper.CategoryResProvider.getDiaplayName
import cc.meteormc.yourmiui.store.HostStore
import cc.meteormc.yourmiui.ui.adapter.FeatureAdapter

class CategoryFragment : BaseFragment<FragmentCategoryBinding>({ inflater, container ->
    FragmentCategoryBinding.inflate(inflater, container, false)
}) {
    private val category: Category?
        get() = arguments?.getInt("category", -1)
            ?.takeIf { it >= 0 }
            ?.let { Category.entries[it] }

    override fun onCreate(): View {
        if (category == null) {
            findNavController().navigateUp()
            return binding.root
        }

        val categoryToolbar = binding.categoryToolbar
        categoryToolbar.title = category!!.getDiaplayName()
        categoryToolbar.setNavigationOnClickListener {
            it.findNavController().navigateUp()
        }

        val featureList = binding.featureList
        featureList.layoutManager = LinearLayoutManager(requireContext())
        featureList.adapter = FeatureAdapter(HostStore.features[category] ?: emptyList())

        return binding.root
    }
}