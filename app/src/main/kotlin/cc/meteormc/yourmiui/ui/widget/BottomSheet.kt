package cc.meteormc.yourmiui.ui.widget

import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.graphics.ColorUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textview.MaterialTextView

class BottomSheet(
    private val context: Context,
    private val title: String?,
    private val content: String?,
    private val negativeButtonText: String?,
    private val negativeButtonListener: DialogInterface.OnClickListener?,
    private val positiveButtonText: String?,
    private val positiveButtonListener: DialogInterface.OnClickListener?
) : BottomSheetDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = LinearLayout(context).apply {
            setPadding(18.px2dp(), 36.px2dp(), 18.px2dp(), 36.px2dp())
            gravity = Gravity.CENTER_HORIZONTAL
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        if (title != null) {
            val view = MaterialTextView(context).apply {
                gravity = Gravity.CENTER
                textSize = 18f
                text = title
            }
            container.addView(view)
        }

        if (content != null) {
            val view = MaterialTextView(context).apply {
                gravity = Gravity.CENTER
                textSize = 16f
                text = content

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.topMargin = 16.px2dp()
                params.bottomMargin = 24.px2dp()
                layoutParams = params
            }
            container.addView(view)
        }

        val buttons = LinearLayout(context).apply {
            setPadding(12.px2dp(), 0, 12.px2dp(), 0)
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        if (negativeButtonText != null) {
            val button = MaterialButton(context).apply {
                text = negativeButtonText
                setOnClickListener {
                    dismiss()
                    negativeButtonListener?.onClick(this@BottomSheet, BUTTON_NEGATIVE)
                }
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )

                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(MaterialColors.getColor(this, android.R.attr.colorPrimary))
                strokeWidth = 1.px2dp()
                strokeColor = ColorStateList.valueOf(
                    ColorUtils.setAlphaComponent(
                        MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface),
                        50
                    )
                )
            }
            buttons.addView(button)
        }

        if (negativeButtonText != null && positiveButtonText != null) {
            val spacer = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(16.px2dp(), 0)
            }
            buttons.addView(spacer)
        }

        if (positiveButtonText != null) {
            val button = MaterialButton(context).apply {
                text = positiveButtonText
                setOnClickListener {
                    dismiss()
                    positiveButtonListener?.onClick(this@BottomSheet, BUTTON_POSITIVE)
                }
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            buttons.addView(button)
        }

        container.addView(buttons)

        setContentView(container)
    }

    override fun show() {
        super.show()

        window?.decorView!!.post {
            drawRounded()
        }
    }

    private fun drawRounded() {
        val sheetField = BottomSheetDialog::class.java.getDeclaredField("bottomSheet")
        sheetField.isAccessible = true
        val sheet = sheetField.get(this) as FrameLayout
        val model = ShapeAppearanceModel.builder(
            context, 0,
            com.google.android.material.R.style.ShapeAppearanceOverlay_MaterialAlertDialog_Material3
        ).build()
        val background = MaterialShapeDrawable(model)
        background.fillColor = ColorStateList.valueOf(
            MaterialColors.getColor(
                sheet,
                com.google.android.material.R.attr.colorSurfaceContainerHigh
            )
        )
        sheet.background = InsetDrawable(background, 16.px2dp())
    }

    private fun Int.px2dp() = (this * context.resources.displayMetrics.density).toInt()

    @Suppress("unused")
    class Builder(val context: Context) {
        private var title: String? = null
        private var content: String? = null
        private var negativeButtonText: String? = null
        private var negativeButtonListener: DialogInterface.OnClickListener? = null
        private var positiveButtonText: String? = null
        private var positiveButtonListener: DialogInterface.OnClickListener? =null

        fun setTitle(@StringRes titleId: Int): Builder {
            this.title = context.getString(titleId)
            return this
        }

        fun setTitle(@StringRes titleId: Int, vararg formatArgs: Any?): Builder {
            this.title = context.getString(titleId, *formatArgs)
            return this
        }

        fun setTitle(title: CharSequence?): Builder {
            this.title = title?.toString()
            return this
        }

        fun setContent(@StringRes contentId: Int): Builder {
            this.content = context.getString(contentId)
            return this
        }

        fun setContent(@StringRes contentId: Int, vararg formatArgs: Any?): Builder {
            this.content = context.getString(contentId, *formatArgs)
            return this
        }

        fun setContent(content: CharSequence?): Builder {
            this.content = content?.toString()
            return this
        }

        fun setNegativeButton(@StringRes textId: Int, listener: DialogInterface.OnClickListener?): Builder {
            this.negativeButtonText = context.getString(textId)
            this.negativeButtonListener = listener
            return this
        }

        fun setNegativeButton(text: CharSequence?, listener: DialogInterface.OnClickListener?): Builder {
            this.negativeButtonText = text?.toString()
            this.negativeButtonListener = listener
            return this
        }

        fun setPositiveButton(@StringRes textId: Int, listener: DialogInterface.OnClickListener?): Builder {
            this.positiveButtonText = context.getString(textId)
            this.positiveButtonListener = listener
            return this
        }

        fun setPositiveButton(text: CharSequence?, listener: DialogInterface.OnClickListener?): Builder {
            this.positiveButtonText = text?.toString()
            this.positiveButtonListener = listener
            return this
        }

        fun build(): BottomSheet {
            return BottomSheet(
                this.context,
                this.title,
                this.content,
                this.negativeButtonText,
                this.negativeButtonListener,
                this.positiveButtonText,
                this.positiveButtonListener
            )
        }
    }
}