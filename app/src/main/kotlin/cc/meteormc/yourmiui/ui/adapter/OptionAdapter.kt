package cc.meteormc.yourmiui.ui.adapter

import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.common.Option.Type
import cc.meteormc.yourmiui.common.data.AppInfo
import cc.meteormc.yourmiui.databinding.ItemOptionBinding
import cc.meteormc.yourmiui.service.FeaturePreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class OptionAdapter(
    options: List<Option<*>>,
    private var prefs: FeaturePreference
) : BaseAdapter<ItemOptionBinding, Option<*>>(
    options.toTypedArray(),
    { inflater, parent -> ItemOptionBinding.inflate(inflater, parent, false) }
) {
    companion object {
        private var appCache: List<AppInfo> = emptyList()
    }

    override fun newHolder(binding: ItemOptionBinding): BaseAdapter<ItemOptionBinding, Option<*>>.BaseViewHolder {
        return ViewHolder(binding, prefs)
    }

    private inner class ViewHolder(
        binding: ItemOptionBinding,
        private var prefs: FeaturePreference
    ) : BaseAdapter<ItemOptionBinding, Option<*>>.BaseViewHolder(binding, binding.root) {
        override fun onBind(item: Option<*>) {
            binding.optionName.setText(item.nameRes)
            binding.optionSummary.setText(item.summaryRes)
            binding.root.setOnClickListener { view ->
                val context = view.context
                val type = Type.getTypeByObject<Any>(item.type) ?: return@setOnClickListener
                val untypedValue = prefs.option(item.key)?.let { preference ->
                    val deserializer = type.deserializer(preference)
                    deserializer
                } ?: item.defaultValue
                @Suppress("UNCHECKED_CAST")
                when (type) {
                    is Type.App -> TODO("Not yet implemented")
                    is Type.AppList -> onClickAppListOption(
                        context, item, type,
                        untypedValue as? Set<String> ?: return@setOnClickListener
                    )
                    is Type.SingleChoiceList -> onClickSingleChoiceListOption(
                        context, item, type,
                        untypedValue as? String ?: return@setOnClickListener
                    )
                    is Type.MultiChoiceList -> onClickMultiChoiceListOption(
                        context, item, type,
                        untypedValue as? Set<String> ?: return@setOnClickListener
                    )
                    is Type.Switch -> onClickSwitchOption(
                        context, item, type,
                        untypedValue as? Boolean ?: return@setOnClickListener,
                        view
                    )
                    is Type.Text -> TODO("Not yet implemented")
                }
            }
        }

        private fun onClickAppListOption(
            context: Context,
            option: Option<*>,
            type: Type.AppList,
            value: Set<String>
        ) {
            val pm = context.packageManager
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA).toList()
            val adapter = AppAdapter(installedApps.size, value.toMutableSet())

            val searchView = SearchView(context).apply {
                this.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                this.queryHint = context.getString(R.string.dialog_app_picker_hint)
                this.setIconifiedByDefault(false)
                this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        adapter.filter(query)
                        return true
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

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_app_picker_title)
                .setView(container)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.dialog_save) { _, _ ->
                    prefs.option(option.key, type.serializer(adapter.selected))
                }.show()
        }

        private fun onClickSingleChoiceListOption(
            context: Context,
            option: Option<*>,
            type: Type.SingleChoiceList,
            value: String
        ) {
            val options = type.options
            MaterialAlertDialogBuilder(context)
                .setSingleChoiceItems(
                    options.map { context.getString(it.second) }.toTypedArray(),
                    options.indexOfFirst { it.first == value }
                ) { dialog, which ->
                    val selected = options[which].first
                    prefs.option(option.key, type.serializer(selected))
                    dialog.dismiss()
                }.show()
        }

        private fun onClickMultiChoiceListOption(
            context: Context,
            option: Option<*>,
            type: Type.MultiChoiceList,
            value: Set<String>
        ) {
            val options = type.options
            val checkedItem = BooleanArray(options.size) {
                val key = options[it].first
                value.contains(key)
            }

            MaterialAlertDialogBuilder(context)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.dialog_save) { _, _ ->
                    val selected = checkedItem.toList().mapIndexedNotNull { index, isChecked ->
                        if (isChecked) options[index].first else null
                    }.toSet()
                    prefs.option(option.key, type.serializer(selected))
                }.setMultiChoiceItems(
                    options.map { context.getString(it.second) }.toTypedArray(),
                    checkedItem
                ) { _, which, isChecked ->
                    checkedItem[which] = isChecked
                }.show()
        }

        private fun onClickSwitchOption(
            context: Context,
            option: Option<*>,
            type: Type.Switch,
            value: Boolean,
            anchor: View
        ) {
            val popup = PopupMenu(context, anchor)
            val menu = popup.menu

            val enableBotton = menu.add(0, 0, 0, R.string.popup_enable)
            val disableBotton = menu.add(0, 1, 0, R.string.popup_disable)
            if (value) {
                enableBotton.isCheckable = true
                enableBotton.isChecked = true
            } else {
                disableBotton.isCheckable = true
                disableBotton.isChecked = true
            }
            popup.setOnMenuItemClickListener {
                val selected = it.itemId == 0
                if (selected) {
                    enableBotton.isCheckable = true
                    enableBotton.isChecked = true
                    disableBotton.isCheckable = false
                } else {
                    enableBotton.isCheckable = false
                    disableBotton.isCheckable = true
                    disableBotton.isChecked = true
                }

                prefs.option(option.key, type.serializer(selected))
                return@setOnMenuItemClickListener true
            }
            popup.show()
        }
    }
}