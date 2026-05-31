package cc.meteormc.yourmiui.ui.adapter

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.findNavController
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.databinding.ItemScopeBinding

class CategoryAdapter(
    scopes: List<Category>
) : BaseAdapter<ItemScopeBinding, Category>(
    scopes.toList().toTypedArray(),
    { inflater, parent -> ItemScopeBinding.inflate(inflater, parent, false) }
) {
    override fun newHolder(binding: ItemScopeBinding): BaseAdapter<ItemScopeBinding, Category>.BaseViewHolder {
        return ViewHolder(binding)
    }

    private inner class ViewHolder(
        binding: ItemScopeBinding
    ) : BaseAdapter<ItemScopeBinding, Category>.BaseViewHolder(
        binding,
        binding.root
    ) {
        override fun onBind(item: Category) {
            // todo: 这里的获取方式比较耗时，后面会迁移到App打开时加载应用缓存
            val name = getCategoryName(item)
            val icon = getCategoryIcon(item)

            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("title", name)
                bundle.putInt("category", item.ordinal)
                it.findNavController().navigate(R.id.action_manager_to_scope, bundle)
            }

            binding.scopeName.text = name
            binding.scopeIcon.setImageDrawable(icon)
        }

        private fun getCategoryName(category: Category): String {
            fun provideByPkg(packageName: String): String {
                val pm = itemView.context.packageManager
                val info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                return pm.getApplicationLabel(info).toString()
            }

            fun provideByRes(@StringRes resId: Int): String {
                return itemView.context.getString(resId)
            }

            return when (category) {
                Category.SYSTEM -> provideByRes(R.string.category_system)
                Category.UI -> provideByRes(R.string.category_ui)
                Category.DESKTOP -> provideByRes(R.string.category_desktop)
                Category.MARTET -> provideByPkg("com.xiaomi.market")
                Category.MIUI_PLUS -> provideByRes(R.string.category_miuiplus)
                Category.MMS -> provideByPkg("com.android.mms")
                Category.NFC -> provideByRes(R.string.category_nfc)
                Category.NOTIFICATION -> provideByRes(R.string.category_notification)
                Category.PACKAGE_INSTALLER -> provideByRes(R.string.category_packageinstaller)
                Category.PORTAL -> provideByPkg("com.miui.contentextension")
                Category.SECURITY_CENTER -> provideByPkg("com.miui.securitycenter")
                Category.SETTINGS -> provideByPkg("com.android.settings")
                Category.WALLPAPER -> provideByPkg("com.miui.miwallpaper")
            }
        }

        private fun getCategoryIcon(category: Category): Drawable {
            fun provideByPkg(packageName: String): Drawable {
                val pm = itemView.context.packageManager
                return pm.getApplicationIcon(packageName)
            }

            fun provideByRes(@DrawableRes resId: Int): Drawable {
                return AppCompatResources.getDrawable(itemView.context, resId)!!
            }

            return when (category) {
                Category.SYSTEM -> provideByPkg("android")
                Category.UI -> provideByPkg("com.android.systemui")
                Category.DESKTOP -> provideByPkg("com.miui.home")
                Category.MARTET -> provideByPkg("com.xiaomi.market")
                Category.MIUI_PLUS -> provideByPkg("com.xiaomi.mirror")
                Category.MMS -> provideByPkg("com.android.mms")
                Category.NFC -> provideByPkg("com.android.nfc")
                Category.NOTIFICATION -> provideByPkg("com.miui.notification")
                Category.PACKAGE_INSTALLER -> provideByPkg("com.miui.packageinstaller")
                Category.PORTAL -> provideByPkg("com.miui.contentextension")
                Category.SECURITY_CENTER -> provideByPkg("com.miui.securitycenter")
                Category.SETTINGS -> provideByPkg("com.android.settings")
                Category.WALLPAPER -> provideByPkg("com.miui.miwallpaper")
            }
        }
    }
}