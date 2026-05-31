package cc.meteormc.yourmiui.ui.adapter

import android.view.View
import cc.meteormc.yourmiui.api.OptionType
import cc.meteormc.yourmiui.api.data.OptionInfo
import cc.meteormc.yourmiui.common.prefs.SharedPreferences
import cc.meteormc.yourmiui.databinding.ItemOptionBinding
import cc.meteormc.yourmiui.helper.ResourceParser
import cc.meteormc.yourmiui.ui.widget.AppPicker
import cc.meteormc.yourmiui.ui.widget.SwitchMenu
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class OptionAdapter(
    options: List<OptionInfo>,
    private var prefs: SharedPreferences.Feature
) : BaseAdapter<ItemOptionBinding, OptionInfo>(
    options.toTypedArray(),
    { inflater, parent -> ItemOptionBinding.inflate(inflater, parent, false) }
) {
    override fun newHolder(binding: ItemOptionBinding): BaseAdapter<ItemOptionBinding, OptionInfo>.BaseViewHolder {
        return ViewHolder(binding)
    }

    private inner class ViewHolder(
        binding: ItemOptionBinding
    ) : BaseAdapter<ItemOptionBinding, OptionInfo>.BaseViewHolder(
        binding,
        binding.root
    ) {
        override fun onBind(item: OptionInfo) {
            val context = itemView.context
            binding.optionName.setText(ResourceParser.parseResName(context, item.name))
            binding.optionDescription.setText(ResourceParser.parseResName(context, item.description))
            binding.root.setOnClickListener { onClickOption(it, item) }
        }

        private fun onClickOption(view: View, option: OptionInfo) {
            val key = option.key
            val context = view.context
            when (val type = option.type) {
                is OptionType.App -> {
                    val value = prefs.getOption(key, type) ?: type.defaultPackages
                    AppPicker(context, value, type.multiSelect).setSaveListener {
                        prefs.setOption(key, it, type)
                    }.show()
                }
                is OptionType.List -> {
                    val value = prefs.getOption(key, type) ?: type.defaultOptions
                    val dialog = MaterialAlertDialogBuilder(context)

                    val options = type.options.toList()
                    val displayOptions = options.map {
                        val parsedId = ResourceParser.parseResName(context, it.second)
                        context.getString(parsedId)
                    }.toTypedArray()
                    if (type.multiSelect) {
                        val checkedItem = BooleanArray(options.size) {
                            val key = options[it].first
                            value.contains(key)
                        }

                        dialog.setMultiChoiceItems(displayOptions, checkedItem) { _, which, isChecked ->
                            checkedItem[which] = isChecked
                        }.setPositiveButton(android.R.string.ok) { _, _ ->
                            val selected = checkedItem.toList().mapIndexedNotNull { index, isChecked ->
                                if (isChecked) options[index].first else null
                            }.toSet()
                            prefs.setOption(key, selected, type)
                        }.setNegativeButton(android.R.string.cancel, null)
                    } else {
                        val first = value.firstOrNull()
                        val index = options.indexOfFirst { it.first == first }
                        dialog.setSingleChoiceItems(displayOptions, index) { dialog, which ->
                            val selected = options[which].first
                            prefs.setOption(option.key, setOf(selected), type)
                            dialog.dismiss()
                        }
                    }

                    dialog.show()
                }
                is OptionType.Switch -> {
                    val value = prefs.getOption(key, type) ?: type.defaultValue
                    SwitchMenu(context, view, value).setChangeListener {
                        prefs.setOption(option.key, it, type)
                    }.show()
                }
                is OptionType.Text -> {
                    TODO("Not yet implemented")
                }
            }
        }
    }
}