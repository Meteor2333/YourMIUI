package cc.meteormc.yourmiui.ui.adapter

import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.findNavController
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.util.putObject
import cc.meteormc.yourmiui.databinding.ItemScopeBinding
import cc.meteormc.yourmiui.ui.data.AppInfo

class ScopeAdapter(scopes: Map<Scope, List<AppInfo>>) : BaseAdapter<ItemScopeBinding, Pair<Scope, List<AppInfo>>>(
    scopes.toList().toTypedArray(),
    { inflater, parent -> ItemScopeBinding.inflate(inflater, parent, false) }
) {
    override fun newHolder(binding: ItemScopeBinding): BaseAdapter<ItemScopeBinding, Pair<Scope, List<AppInfo>>>.BaseViewHolder {
        return ViewHolder(binding)
    }

    private inner class ViewHolder(binding: ItemScopeBinding) : BaseAdapter<ItemScopeBinding, Pair<Scope, List<AppInfo>>>.BaseViewHolder(binding, binding.root) {
        private var currentIndex = 0
        private val runnable = Runnable { this.next() }
        private val handler = Handler(Looper.getMainLooper())
        private lateinit var icons: Array<Drawable>

        override fun onBind(item: Pair<Scope, List<AppInfo>>) {
            val scope = item.first
            val infos = item.second

            val context = binding.root.context
            val first = infos.firstOrNull() ?: return
            // 优先使用设置的名称 若没有 则选取已安装的第一项软件名称作为作用域名称
            val name = scope.getNameRes()?.let { context.getString(it) } ?: first.label
            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("name", name)
                bundle.putBoolean("restartable", scope.isRestartable())
                bundle.putObject("apps", infos)
                bundle.putObject("features", scope.getFeatures())
                it.findNavController().navigate(R.id.action_to_scope, bundle)
            }

            binding.scopeName.text = name

            // 如果不支持多软件 直接设置为第一项软件图标
            if (infos.size == 1) {
                binding.scopeIcon.setImageBitmap(first.icon)
                return
            }

            // 否则循环播放多软件图标
            this.currentIndex = 0
            this.icons = infos.map { it.icon.toDrawable(context.resources) }.toTypedArray()
            handler.removeCallbacks(runnable)
            handler.post(runnable)
        }

        override fun onRecycled() {
            handler.removeCallbacks(runnable)
        }

        private fun next() {
            if (!::icons.isInitialized) return
            val oldDrawable = icons[currentIndex]
            currentIndex = (currentIndex + 1) % icons.size
            val newDrawable = icons[currentIndex]

            val transition = TransitionDrawable(arrayOf(oldDrawable, newDrawable))
            transition.startTransition(500)
            binding.scopeIcon.setImageDrawable(transition)

            handler.postDelayed(runnable, 5000)
        }
    }
}