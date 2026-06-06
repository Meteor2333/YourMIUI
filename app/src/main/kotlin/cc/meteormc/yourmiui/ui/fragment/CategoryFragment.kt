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
    private var focusFeature: String?
        get() = arguments?.getString("focusFeature", null)
        set(value) { arguments?.putString("focusFeature", value) }

    override fun onCreateView(): View {
        if (category == null) {
            findNavController().navigateUp()
            return binding.root
        }

        val categoryToolbar = binding.categoryToolbar
        categoryToolbar.title = category!!.getDiaplayName()
        categoryToolbar.setNavigationOnClickListener {
            it.findNavController().navigateUp()
        }

        val features = HostStore.features[category] ?: emptyList()
        val featureList = binding.featureList
        val adapter = FeatureAdapter(features)
        featureList.layoutManager = LinearLayoutManager(requireContext())
        featureList.adapter = adapter
        if (focusFeature != null) featureList.post {
            val position = features.indexOfFirst { it.key == focusFeature }
            focusFeature = null

            if (position < 0) return@post
            featureList.smoothScrollToPosition(position)
            featureList.findViewHolderForAdapterPosition(position)?.let {
                adapter.highlightFeature(it)
            }
        }

        return binding.root
    }
}