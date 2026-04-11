package cc.meteormc.yourmiui.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T, I>(
    protected val items: Array<I>,
    private val factory: (inflater: LayoutInflater, parent: ViewGroup) -> T
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    protected abstract fun newHolder(binding: T): BaseViewHolder

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = factory(inflater, parent)
        return this.newHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        check(holder is BaseAdapter<T, I>.BaseViewHolder) { "ViewHolder must be of type BaseAdapter.ViewHolder" }
        holder.onBind(items[position])
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        check(holder is BaseAdapter<T, I>.BaseViewHolder) { "ViewHolder must be of type BaseAdapter.ViewHolder" }
        holder.onRecycled()
    }

    override fun getItemCount() = items.size

    protected abstract inner class BaseViewHolder(val binding: T, root: View) : RecyclerView.ViewHolder(root) {
        open fun onBind(item: I) {

        }

        open fun onRecycled() {

        }
    }
}