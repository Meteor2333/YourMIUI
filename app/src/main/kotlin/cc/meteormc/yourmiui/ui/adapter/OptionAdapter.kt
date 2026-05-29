package cc.meteormc.yourmiui.ui.adapter

//todo
//class OptionAdapter(
//    options: List<Option<*>>,
//    private var prefs: FeaturePreferences
//) : BaseAdapter<ItemOptionBinding, Option<*>>(
//    options.toTypedArray(),
//    { inflater, parent -> ItemOptionBinding.inflate(inflater, parent, false) }
//) {
//    override fun newHolder(binding: ItemOptionBinding): BaseAdapter<ItemOptionBinding, Option<*>>.BaseViewHolder {
//        return ViewHolder(binding, prefs)
//    }
//
//    private inner class ViewHolder(
//        binding: ItemOptionBinding,
//        private var prefs: FeaturePreferences
//    ) : BaseAdapter<ItemOptionBinding, Option<*>>.BaseViewHolder(
//        binding,
//        binding.root
//    ) {
//        override fun onBind(item: Option<*>) {
//            binding.optionName.setText(item.nameRes)
//            binding.optionSummary.setText(item.summaryRes)
//            binding.root.setOnClickListener { view ->
//                val context = view.context
//                val type = Type.getTypeByObject<Any>(item.type) ?: return@setOnClickListener
//                val untypedValue = prefs.option(item.key)?.let { preference ->
//                    val deserializer = type.deserializer(preference)
//                    deserializer
//                } ?: item.defaultValue
//                @Suppress("UNCHECKED_CAST")
//                when (type) {
//                    is Type.App -> TODO("Not yet implemented")
//                    is Type.AppList -> onClickAppListOption(
//                        context, item, type,
//                        untypedValue as? Set<String> ?: return@setOnClickListener
//                    )
//                    is Type.SingleChoiceList -> onClickSingleChoiceListOption(
//                        context, item, type,
//                        untypedValue as? String ?: return@setOnClickListener
//                    )
//                    is Type.MultiChoiceList -> onClickMultiChoiceListOption(
//                        context, item, type,
//                        untypedValue as? Set<String> ?: return@setOnClickListener
//                    )
//                    is Type.Switch -> onClickSwitchOption(
//                        context, item, type,
//                        untypedValue as? Boolean ?: return@setOnClickListener,
//                        view
//                    )
//                    is Type.Text -> TODO("Not yet implemented")
//                }
//            }
//        }
//
//        private fun onClickAppListOption(
//            context: Context,
//            option: Option<*>,
//            type: Type.AppList,
//            value: Set<String>
//        ) {
//            AppPicker(context, value)
//                .setSaveListener {
//                    prefs.option(option.key, type.serializer(it))
//                }.show()
//        }
//
//        private fun onClickSingleChoiceListOption(
//            context: Context,
//            option: Option<*>,
//            type: Type.SingleChoiceList,
//            value: String
//        ) {
//            val options = type.options
//            MaterialAlertDialogBuilder(context)
//                .setSingleChoiceItems(
//                    options.map { context.getString(it.second) }.toTypedArray(),
//                    options.indexOfFirst { it.first == value }
//                ) { dialog, which ->
//                    val selected = options[which].first
//                    prefs.option(option.key, type.serializer(selected))
//                    dialog.dismiss()
//                }.show()
//        }
//
//        private fun onClickMultiChoiceListOption(
//            context: Context,
//            option: Option<*>,
//            type: Type.MultiChoiceList,
//            value: Set<String>
//        ) {
//            val options = type.options
//            val checkedItem = BooleanArray(options.size) {
//                val key = options[it].first
//                value.contains(key)
//            }
//
//            MaterialAlertDialogBuilder(context)
//                .setNegativeButton(android.R.string.cancel, null)
//                .setPositiveButton(android.R.string.ok) { _, _ ->
//                    val selected = checkedItem.toList().mapIndexedNotNull { index, isChecked ->
//                        if (isChecked) options[index].first else null
//                    }.toSet()
//                    prefs.option(option.key, type.serializer(selected))
//                }.setMultiChoiceItems(
//                    options.map { context.getString(it.second) }.toTypedArray(),
//                    checkedItem
//                ) { _, which, isChecked ->
//                    checkedItem[which] = isChecked
//                }.show()
//        }
//
//        private fun onClickSwitchOption(
//            context: Context,
//            option: Option<*>,
//            type: Type.Switch,
//            value: Boolean,
//            anchor: View
//        ) {
//            SwitchMenu(context, anchor, value)
//                .setChangeListener {
//                    prefs.option(option.key, type.serializer(it))
//                }.show()
//        }
//    }
//}