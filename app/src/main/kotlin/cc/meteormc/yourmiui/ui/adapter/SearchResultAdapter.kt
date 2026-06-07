package cc.meteormc.yourmiui.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.databinding.ItemSearchResultBinding
import cc.meteormc.yourmiui.helper.ResourceParser.getIcon
import cc.meteormc.yourmiui.ui.data.SearchResultItem

class SearchResultAdapter(
    results: Array<SearchResultItem>,
) : ListAdapter<SearchResultItem, SearchResultAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<SearchResultItem>() {
        override fun areItemsTheSame(
            oldItem: SearchResultItem,
            newItem: SearchResultItem
        ): Boolean = oldItem.feature.key == newItem.feature.key

        override fun areContentsTheSame(
            oldItem: SearchResultItem,
            newItem: SearchResultItem
        ): Boolean = oldItem == newItem

    }
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemSearchResultBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val binding = holder.binding
        val item = getItem(position)
        val category = item.category
        binding.resultIcon.setImageDrawable(category.getIcon())
        binding.resultTitle.text = item.title
        binding.resultPath.text = item.path
        binding.resultPath.isSelected = true
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("category", category.ordinal)
            bundle.putString("focusFeature", item.feature.key)
            it.findNavController().navigate(R.id.action_home_to_category, bundle)
        }
    }

    class ViewHolder(val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root)
}