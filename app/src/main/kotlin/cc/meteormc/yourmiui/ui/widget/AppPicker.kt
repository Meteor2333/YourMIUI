package cc.meteormc.yourmiui.ui.widget

import android.content.Context
import android.content.pm.PackageManager
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.common.data.AppInfo
import cc.meteormc.yourmiui.databinding.ItemAppBinding
import cc.meteormc.yourmiui.ui.adapter.BaseAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class AppPicker(
    private val context: Context,
    private val selected: Collection<String>
) : MaterialAlertDialogBuilder(context) {
    companion object {
        private var appCache: List<AppInfo> = emptyList()
    }

    private var saveListener: (selected: Set<String>) -> Unit = { }
    
    override fun create(): AlertDialog {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA).toList()
        val adapter = AppAdapter(installedApps.size, selected.toMutableSet())
        
        val searchView = SearchView(context).apply {
            this.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.queryHint = context.getString(R.string.app_picker_hint)
            this.setIconifiedByDefault(false)
            this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    adapter.filter(query)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter(newText)
                    return true
                }
            })
        }

        val appList = RecyclerView(context).apply {
            this.adapter = adapter
            this.layoutManager = LinearLayoutManager(context)
            this.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            adapter.listView = this
        }

        val container = LinearLayout(context).apply {
            this.orientation = LinearLayout.VERTICAL
            this.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        container.addView(searchView)
        container.addView(appList)

        fun loadAppInfos(): Flow<AppInfo> = flow {
            installedApps.forEach {
                val info = AppInfo(
                    it.packageName,
                    pm.getApplicationLabel(it).toString(),
                    pm.getApplicationIcon(it).toBitmap()
                )
                emit(info)
            }
        }

        if (appCache.isNotEmpty()) {
            adapter.submitAll(appCache)
        } else {
            (context as LifecycleOwner).lifecycleScope.launch {
                val cache = mutableListOf<AppInfo>()
                loadAppInfos()
                    .flowOn(Dispatchers.IO)
                    .onCompletion {
                        appCache = cache.toList()
                    }
                    .collect {
                        cache.add(it)
                        adapter.submit(it, searchView.query?.toString())
                    }
            }
        }

        setView(container)
        setTitle(R.string.app_picker_title)
        setNegativeButton(R.string.dialog_cancel, null)
        setPositiveButton(R.string.dialog_save) { _, _ -> saveListener(adapter.selected) }
        return super.create()
    }

    fun setSaveListener(listener: (selected: Set<String>) -> Unit): AppPicker {
        saveListener = listener
        return this
    }

    private class AppAdapter(size: Int, val selected: MutableSet<String>) : BaseAdapter<ItemAppBinding, AppInfo?>(
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
                    binding.appIcon.setImageBitmap(item.icon)
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
}