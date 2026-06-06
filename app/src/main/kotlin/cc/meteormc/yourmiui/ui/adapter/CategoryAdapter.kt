package cc.meteormc.yourmiui.ui.adapter

import android.os.Bundle
import androidx.navigation.findNavController
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.databinding.ItemCategoryBinding
import cc.meteormc.yourmiui.helper.CategoryResProvider.getDiaplayName
import cc.meteormc.yourmiui.helper.CategoryResProvider.getIcon

class CategoryAdapter(
    scopes: List<Category>
) : BaseAdapter<ItemCategoryBinding, Category>(
    scopes.toList().toTypedArray(),
    { inflater, parent -> ItemCategoryBinding.inflate(inflater, parent, false) }
) {
    override fun newHolder(binding: ItemCategoryBinding): BaseAdapter<ItemCategoryBinding, Category>.BaseViewHolder {
        return ViewHolder(binding)
    }

    private inner class ViewHolder(
        binding: ItemCategoryBinding
    ) : BaseAdapter<ItemCategoryBinding, Category>.BaseViewHolder(
        binding,
        binding.root
    ) {
        override fun onBind(item: Category) {
            val icon = item.getIcon()
            val displayName = item.getDiaplayName()

            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("category", item.ordinal)
                it.findNavController().navigate(R.id.action_home_to_category, bundle)
            }

            binding.categoryName.text = displayName
            binding.categoryIcon.setImageDrawable(icon)
        }
    }
}