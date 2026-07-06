package cc.meteormc.yourmiui.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.content.withStyledAttributes
import androidx.core.widget.addTextChangedListener
import cc.meteormc.yourmiui.R
import com.google.android.material.internal.ToolbarUtils
import com.google.android.material.shape.MaterialShapeDrawable

class SearchBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.searchBarStyle
) : Toolbar(context, attrs, defStyleAttr) {
    private val editText: AppCompatEditText
    private val imm by lazy {
        context.getSystemService(InputMethodManager::class.java)
    }

    val text
        get() = editText.text

    init {
        minimumHeight = resources.getDimensionPixelSize(R.dimen.search_bar_height)
        setNavigationIcon(R.drawable.ic_search_24dp)
        ToolbarUtils.getNavigationIconButton(this)?.let {
            it.background = null
            it.isClickable = false
            it.isFocusable = false
        }
        context.withStyledAttributes(
            attrs,
            R.styleable.SearchBar,
            defStyleAttr,
            R.style.Theme_YourMIUI_SearchBar
        ) {
            val background = MaterialShapeDrawable(
                context,
                attrs,
                defStyleAttr,
                R.style.Theme_YourMIUI_SearchBar
            )
            background.fillColor = ColorStateList.valueOf(getColor(R.styleable.SearchBar_android_background, 0))
            this@SearchBar.background = background
        }

        LayoutInflater.from(context).inflate(R.layout.view_search_bar, this)
        editText = findViewById(R.id.search_edit)
    }

    fun setOnEditorActionListener(listener: TextView.OnEditorActionListener) {
        editText.setOnEditorActionListener(listener)
    }

    fun addTextChangedListener(
        beforeTextChanged: (text: CharSequence?, start: Int, count: Int, after: Int) -> Unit = { _, _, _, _ -> },
        onTextChanged: (text: CharSequence?, start: Int, before: Int, count: Int) -> Unit = { _, _, _, _ -> },
        afterTextChanged: (text: Editable?) -> Unit = {},
    ) {
        editText.addTextChangedListener(beforeTextChanged, onTextChanged, afterTextChanged)
    }

    fun removeTextChangedListener(watcher: TextWatcher) {
        editText.removeTextChangedListener(watcher)
    }

    fun showKeyboard() {
        editText.post {
            @Suppress("DEPRECATION")
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun hideKeyboard() {
        editText.post {
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }
}