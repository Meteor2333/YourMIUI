package cc.meteormc.yourmiui.ui.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import cc.meteormc.yourmiui.databinding.ItemAppBinding
import cc.meteormc.yourmiui.ui.data.AppInfo

@SuppressLint("NotifyDataSetChanged")
class AppAdapter(size: Int, val selected: MutableSet<String>) : BaseAdapter<ItemAppBinding, AppInfo?>(
    arrayOfNulls(size),
    { inflater, parent -> ItemAppBinding.inflate(inflater, parent, false) }
) {
    private var index = 0
    lateinit var listView: RecyclerView
    private val apps = mutableListOf<AppInfo>()
    private val comparator = compareBy<AppInfo> { !selected.contains(it.packageName) }
        .thenBy { it.label.lowercase() }
        .thenBy { it.packageName }

    override fun getItemCount() = index

    fun filter(query: String?) {
        items.fill(null)

        index = 0
        for (app in apps) {
            if (isMatch(app, query)) {
                items[index++] = app
            }
        }

        notifyDataSetChanged()
    }

    fun submit(app: AppInfo, query: String?) {
        fun findInsertIndex(app: AppInfo): Int {
            for (i in 0 until index) {
                val current = items[i] ?: continue
                if (comparator.compare(app, current) < 0) {
                    return i
                }
            }
            return index
        }

        val insertIndex = findInsertIndex(app)
        apps.add(insertIndex, app)
        if (isMatch(app, query)) {
            for (i in index downTo insertIndex + 1) {
                items[i] = items[i - 1]
            }
            items[insertIndex] = app
            notifyItemInserted(insertIndex)
            index++
        }

        if (insertIndex <= 0 && ::listView.isInitialized) {
            listView.post {
                listView.smoothScrollBy(0, -listView.computeVerticalScrollOffset())
            }
        }
    }

    fun submitAll(apps: List<AppInfo>) {
        items.fill(null)
        this.apps.clear()

        index = 0
        apps.sortedWith(comparator).forEachIndexed { _, app ->
            items[index++] = app
            this.apps.add(app)
        }

        notifyDataSetChanged()
    }

    private fun isMatch(app: AppInfo, query: String?): Boolean {
        if (query.isNullOrBlank()) return true
        return app.label.contains(query, true) || app.packageName.contains(query, true)
    }

    override fun newHolder(binding: ItemAppBinding): BaseAdapter<ItemAppBinding, AppInfo?>.BaseViewHolder {
        return object : BaseViewHolder(binding, binding.root) {
            override fun onBind(item: AppInfo?) {
                if (item == null) return
                binding.appIcon.setImageDrawable(item.icon)
                binding.appName.text = item.label

                val checkbox = binding.checkbox
                checkbox.setOnCheckedChangeListener(null)
                checkbox.isChecked = selected.contains(item.packageName)

                fun toggle(isChecked: Boolean) {
                    if (isChecked) selected.add(item.packageName)
                    else selected.remove(item.packageName)
                    notifyItemChanged(layoutPosition)
                }

                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    toggle(isChecked)
                }

                itemView.setOnClickListener {
                    toggle(!checkbox.isChecked)
                }
            }
        }
    }
}