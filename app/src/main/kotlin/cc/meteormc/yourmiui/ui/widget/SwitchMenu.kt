package cc.meteormc.yourmiui.ui.widget

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import cc.meteormc.yourmiui.R

class SwitchMenu(
    context: Context,
    anchor: View,
    private val initial: Boolean
) : PopupMenu(context, anchor) {
    companion object {
        private const val ENABLE_BOTTON_ID = 0x0
        private const val DISABLE_BOTTON_ID = 0x1
    }

    private var changeListener: (selected: Boolean) -> Unit = { }

    override fun show() {
        val enableBotton = menu.add(0, ENABLE_BOTTON_ID, 0, R.string.switch_menu_enable)
        val disableBotton = menu.add(0, DISABLE_BOTTON_ID, 0, R.string.switch_menu_disable)
        if (initial) {
            enableBotton.isCheckable = true
            enableBotton.isChecked = true
        } else {
            disableBotton.isCheckable = true
            disableBotton.isChecked = true
        }

        setOnMenuItemClickListener {
            when (it.itemId) {
                ENABLE_BOTTON_ID -> {
                    enableBotton.isCheckable = true
                    enableBotton.isChecked = true
                    disableBotton.isCheckable = false
                    changeListener(true)
                }
                DISABLE_BOTTON_ID -> {
                    enableBotton.isCheckable = false
                    disableBotton.isCheckable = true
                    disableBotton.isChecked = true
                    changeListener(false)
                }
            }

            return@setOnMenuItemClickListener true
        }

        super.show()
    }

    fun setChangeListener(listener: (selected: Boolean) -> Unit): SwitchMenu {
        changeListener = listener
        return this
    }
}