package cc.meteormc.yourmiui.ui.adapter

import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.core.Option
import cc.meteormc.yourmiui.databinding.ItemOptionBinding
import cc.meteormc.yourmiui.service.FeaturePreference
import cc.meteormc.yourmiui.ui.data.AppInfo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class OptionAdapter(
    options: List<Option>,
    private var prefs: FeaturePreference
) : BaseAdapter<ItemOptionBinding, Option>(
    options.toTypedArray(),
    { inflater, parent -> ItemOptionBinding.inflate(inflater, parent, false) }
) {
    companion object {
        private var appCache: List<AppInfo> = emptyList()
    }

    override fun newHolder(binding: ItemOptionBinding): BaseAdapter<ItemOptionBinding, Option>.BaseViewHolder {
        return ViewHolder(binding, prefs)
    }

    private inner class ViewHolder(
        binding: ItemOptionBinding,
        private var prefs: FeaturePreference
    ) : BaseAdapter<ItemOptionBinding, Option>.BaseViewHolder(binding, binding.root) {
        override fun onBind(item: Option) {
            binding.optionName.setText(item.getNameRes())
            binding.optionSummary.setText(item.getSummaryRes())
            binding.root.setOnClickListener { view ->
                val context = view.context
                val type = Option.Type.getTypeByObject<Any>(item.getType()) ?: return@setOnClickListener
                val untypedValue = prefs.option(item.getPreferenceKey())?.let { preference ->
                    val deserializer = type.deserializer(preference)
                    deserializer
                } ?: item.getDefaultValue()
                @Suppress("UNCHECKED_CAST")
                when (type) {
                    is Option.Type.APP -> TODO("Not yet implemented")
                    is Option.Type.APP_LIST -> onClickAppListOption(
                        context, item, type,
                        untypedValue as? Set<String> ?: return@setOnClickListener
                    )
                    is Option.Type.SINGLE_LIST -> onClickSingleChoiceListOption(
                        context, item, type,
                        untypedValue as? String ?: return@setOnClickListener
                    )
                    is Option.Type.MULTI_LIST -> onClickMultiChoiceListOption(
                        context, item, type,
                        untypedValue as? Set<String> ?: return@setOnClickListener
                    )
                    is Option.Type.SWITCH -> onClickSwitchOption(
                        context, item, type,
                        untypedValue as? Boolean ?: return@setOnClickListener,
                        view
                    )
                    is Option.Type.TEXT -> TODO("Not yet implemented")
                }
            }
        }

        private fun onClickAppListOption(
            context: Context,
            option: Option,
            type: Option.Type.APP_LIST,
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
                        pm.getApplicationIcon(it)
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
                    prefs.option(option.getPreferenceKey(), type.serializer(adapter.selected))
                }.show()
        }

        private fun onClickSingleChoiceListOption(
            context: Context,
            option: Option,
            type: Option.Type.SINGLE_LIST,
            value: String
        ) {
            val options = type.options
            MaterialAlertDialogBuilder(context)
                .setSingleChoiceItems(
                    options.map { context.getString(it.second) }.toTypedArray(),
                    options.indexOfFirst { it.first == value }
                ) { dialog, which ->
                    val selected = options[which].first
                    prefs.option(option.getPreferenceKey(), type.serializer(selected))
                    dialog.dismiss()
                }.show()
        }

        private fun onClickMultiChoiceListOption(
            context: Context,
            option: Option,
            type: Option.Type.MULTI_LIST,
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
                    prefs.option(option.getPreferenceKey(), type.serializer(selected))
                }.setMultiChoiceItems(
                    options.map { context.getString(it.second) }.toTypedArray(),
                    checkedItem
                ) { _, which, isChecked ->
                    checkedItem[which] = isChecked
                }.show()
        }

        private fun onClickSwitchOption(
            context: Context,
            option: Option,
            type: Option.Type.SWITCH,
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

                prefs.option(option.getPreferenceKey(), type.serializer(selected))
                return@setOnMenuItemClickListener true
            }
            popup.show()
        }
    }
}