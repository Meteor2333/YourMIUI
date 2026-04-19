package cc.meteormc.yourmiui.xposed.systemui.feature

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.graphics.Region
import android.widget.ImageView
import cc.meteormc.yourmiui.core.Option
import cc.meteormc.yourmiui.xposed.ConstructorOps
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedOption

object EditGxzwQuickOpen : XposedFeature(
    key = "systemui_edit_gxzw_quick_open",
    nameRes = R.string.feature_systemui_edit_gxzw_quick_open_name,
    descriptionRes = R.string.feature_systemui_edit_gxzw_quick_open_description,
    testEnvironmentRes = R.string.feature_systemui_edit_gxzw_quick_open_test_environment
) {
    private lateinit var keptItems: Set<QuickOpenItem>

    private const val EXTRA_ITEM_CLASS = "com.android.keyguard.fod.item.AddEventItem"
    private const val EXTRA_ITEM_IDENTIFIER = "cc.meteormc.yourmiui.xposed.EditGxzwQuickOpen#ExtraQuickOpenItem"

    override fun onLoadPackage() {
        QuickOpenItem.entries.forEach { it.extra?.init() }

        helper("com.android.keyguard.fod.MiuiGxzwQuickOpenUtil") {
            method("getValidItemIdList")?.hookBefore {
                it.result = keptItems.map { item -> item.id }.sorted()
            }

            method("generateQuickOpenItem")?.hookBefore {
                val id = it.args.filterIsInstance<Int>().firstOrNull() ?: return@hookBefore
                val item = QuickOpenItem.entries.firstOrNull { entry -> entry.id == id } ?: return@hookBefore
                val newArgs = it.args.copyOfRange(0, 3)
                it.result = if (item.extra != null) {
                    item.extra.newInstance(*newArgs) ?: return@hookBefore
                } else {
                    item.constructor?.new(*newArgs)
                }
            }
        }
    }

    override fun getOptions(): Iterable<XposedOption<Set<String>>> {
        return listOf(
            XposedOption(
                "kept_items",
                R.string.option_systemui_edit_gxzw_quick_open_kept_items_name,
                R.string.option_systemui_edit_gxzw_quick_open_kept_items_summary,
                Option.Type.MULTI_LIST(
                    QuickOpenItem.ADD_EVENT.key to R.string.option_systemui_edit_gxzw_quick_open_kept_items_add_event,
                    QuickOpenItem.QR_CODE.key to R.string.option_systemui_edit_gxzw_quick_open_kept_items_qr_code,
                    QuickOpenItem.SEARCH.key to R.string.option_systemui_edit_gxzw_quick_open_kept_items_search,
                    QuickOpenItem.ALIPAY_PAY.key to R.string.option_systemui_edit_gxzw_quick_open_kept_items_alipay_pay,
                    QuickOpenItem.ALIPAY_SCAN.key to R.string.option_systemui_edit_gxzw_quick_open_kept_items_alipay_scan,
                    QuickOpenItem.WECHAT_PAY.key to R.string.option_systemui_edit_gxzw_quick_open_kept_items_wechat_pay,
                    QuickOpenItem.WECHAT_SCAN.key to R.string.option_systemui_edit_gxzw_quick_open_kept_items_wechat_scan,
                    QuickOpenItem.XIAOAI.key to R.string.option_systemui_edit_gxzw_quick_open_kept_items_xiaoai,
                    QuickOpenItem.TORCH.key to R.string.option_systemui_edit_gxzw_quick_open_kept_items_torch
                ),
                setOf(
                    QuickOpenItem.ALIPAY_PAY.key,
                    QuickOpenItem.ALIPAY_SCAN.key,
                    QuickOpenItem.XIAOAI.key,
                    QuickOpenItem.WECHAT_PAY.key,
                    QuickOpenItem.WECHAT_SCAN.key
                )
            ) { keptItems = it.mapNotNull { key ->
                QuickOpenItem.entries.firstOrNull { entry -> entry.key == key}
            }.toSet() }
        )
    }

    private enum class QuickOpenItem(
        val id: Int,
        val key: String,
        clazzName: String?,
        val extra: ExtraQuickOpenItem? = null
    ) {
        ADD_EVENT(6, "add_event", "AddEventItem"),
        QR_CODE(5, "qr_code", "QrCodeItem"),
        SEARCH(4, "search", "SearchItem"),
        ALIPAY_PAY(1, "alipay_pay", "AlipayPayItem"),
        ALIPAY_SCAN(2, "alipay_scan", "AlipayScanItem"),
        WECHAT_PAY(8, "wechat_pay", "WechatPayItem"),
        WECHAT_SCAN(9, "wechat_scan", "WechatScanItem"),
        XIAOAI(3, "xiaoai", "XiaoaiItem"),
        TORCH(7, "torch", null, ExtraQuickOpenItem("Torch", "YourMIUI/Torch", "keyguard_left_view_torchlight_n", "打开手电筒", "") {
            helper("com.miui.systemui.util.CommonUtil") {
                method("toggleTorch")?.call(null)
            }
        });

        val constructor: ConstructorOps<*>? by lazy {
            if (clazzName == null) return@lazy null
            helper("com.android.keyguard.fod.item.$clazzName") {
                constructor(RectF::class.java, Region::class.java, Context::class.java)
            }
        }
    }

    private data class ExtraQuickOpenItem(
        val identifier: String,
        val tag: String,
        val iconResName: String,
        val title: String,
        val subtitle: String,
        val handler: () -> Unit
    ) {
        private fun identifierOf() = "$EXTRA_ITEM_IDENTIFIER$$identifier"

        fun newInstance(vararg args: Any): Any? {
            // 利用AddEventItem来实现额外功能项
            return helper(EXTRA_ITEM_CLASS) {
                val viewField = field("mView") ?: return@helper null
                val identifierField = field("mPackageName") ?: return@helper null
                val instance = constructor(RectF::class.java, Region::class.java, Context::class.java)?.new(*args) ?: return@helper null
                val context = args.filterIsInstance<Context>().firstOrNull() ?: return@helper null
                @SuppressLint("DiscouragedApi")
                val identifier = context.resources.getIdentifier(iconResName, "drawable", "com.android.systemui")
                viewField[instance, ImageView::class.java]?.setImageResource(identifier)
                identifierField[instance] = identifierOf()
                instance
            }
        }

        fun init() {
            val identifierField = helper(EXTRA_ITEM_CLASS) {
                field("mPackageName")
            } ?: return
            fun Any.isExtraClass(): Boolean {
                val clazz = this.javaClass.name
                val identifier = identifierField[this, String::class.java] ?: return false
                return clazz == EXTRA_ITEM_CLASS && identifier == identifierOf()
            }

            helper(EXTRA_ITEM_CLASS) {
                method("getTag")?.hookBefore {
                    if (!it.thisObject.isExtraClass()) return@hookBefore
                    it.result = tag
                }

                method("getTitle")?.hookBefore {
                    if (!it.thisObject.isExtraClass()) return@hookBefore
                    it.result = title
                }

                method("getSubTitle")?.hookBefore {
                    if (!it.thisObject.isExtraClass()) return@hookBefore
                    it.result = subtitle
                }
            }

            helper("com.android.keyguard.fod.MiuiGxzwQuickOpenView") {
                (method("handleQucikOpenItemTouchUp") ?: method("handleQuickOpenItemTouchUp"))?.hookBefore {
                    val item = it.args[0]
                    if (!item.isExtraClass()) return@hookBefore
                    handler()
                    it.result = null
                }
            }
        }
    }
}