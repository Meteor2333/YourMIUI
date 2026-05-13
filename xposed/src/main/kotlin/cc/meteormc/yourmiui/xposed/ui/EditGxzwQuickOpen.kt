package cc.meteormc.yourmiui.xposed.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.graphics.Region
import android.widget.ImageView
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.common.Option.Type
import cc.meteormc.yourmiui.xposed.ConstructorWrapper
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

object EditGxzwQuickOpen : Feature(
    key = "edit_gxzw_quick_open",
    nameRes = R.string.feature_systemui_edit_gxzw_quick_open_name,
    descriptionRes = R.string.feature_systemui_edit_gxzw_quick_open_description,
    testEnvironmentRes = R.string.feature_systemui_edit_gxzw_quick_open_test_environment
) {
    private lateinit var reservedItems: Set<QuickOpenItem>

    private const val EXTRA_ITEM_CLASS = "com.android.keyguard.fod.item.AddEventItem"
    private const val EXTRA_ITEM_IDENTIFIER = "cc.meteormc.yourmiui.xposed.EditGxzwQuickOpen#ExtraQuickOpenItem"

    override fun onLoadPackage() {
        QuickOpenItem.entries.forEach { it.extra?.init() }

        operator("com.android.keyguard.fod.MiuiGxzwQuickOpenUtil") {
            // modifier: public static | signature: getValidItemIdList(Landroid/content/Context;)Ljava/util/List<Ljava/lang/Integer;>;
            method("getValidItemIdList")?.overrideResult {
                reservedItems.map { item -> item.id }.sorted()
            }

            // modifier: public static | signature: generateQuickOpenItem(Landroid/graphics/RectF;Landroid/graphics/Region;Landroid/content/Context;I)Lcom/android/keyguard/fod/item/IQuickOpenItem;
            method("generateQuickOpenItem")?.overrideResult {
                val id = it.intArg() ?: return@overrideResult Unit
                val item = QuickOpenItem.entries.firstOrNull { entry -> entry.id == id } ?: return@overrideResult Unit
                val rectF = it.argByGenerics<RectF>()
                val region = it.argByGenerics<Region>()
                val context = it.argByGenerics<Context>()
                if (item.extra != null) {
                    item.extra.newInstance(it.argByGenerics<Context>(), rectF, region, context)
                } else {
                    item.constructor?.new(rectF, region, context)
                }
            }
        }
    }

    override fun getOptions(): List<Option<Set<String>>> {
        return listOf(
            Option(
                "reserved_items",
                R.string.option_systemui_edit_gxzw_quick_open_reserved_items_name,
                R.string.option_systemui_edit_gxzw_quick_open_reserved_items_summary,
                Type.MultiChoiceList(
                    QuickOpenItem.ADD_EVENT.key to R.string.option_systemui_edit_gxzw_quick_open_reserved_items_add_event,
                    QuickOpenItem.QR_CODE.key to R.string.option_systemui_edit_gxzw_quick_open_reserved_items_qr_code,
                    QuickOpenItem.SEARCH.key to R.string.option_systemui_edit_gxzw_quick_open_reserved_items_search,
                    QuickOpenItem.ALIPAY_PAY.key to R.string.option_systemui_edit_gxzw_quick_open_reserved_items_alipay_pay,
                    QuickOpenItem.ALIPAY_SCAN.key to R.string.option_systemui_edit_gxzw_quick_open_reserved_items_alipay_scan,
                    QuickOpenItem.WECHAT_PAY.key to R.string.option_systemui_edit_gxzw_quick_open_reserved_items_wechat_pay,
                    QuickOpenItem.WECHAT_SCAN.key to R.string.option_systemui_edit_gxzw_quick_open_reserved_items_wechat_scan,
                    QuickOpenItem.XIAOAI.key to R.string.option_systemui_edit_gxzw_quick_open_reserved_items_xiaoai,
                    QuickOpenItem.TORCH.key to R.string.option_systemui_edit_gxzw_quick_open_reserved_items_torch
                ),
                setOf(
                    QuickOpenItem.ALIPAY_PAY.key,
                    QuickOpenItem.ALIPAY_SCAN.key,
                    QuickOpenItem.XIAOAI.key,
                    QuickOpenItem.WECHAT_PAY.key,
                    QuickOpenItem.WECHAT_SCAN.key
                )
            ) {
                reservedItems = it.mapNotNull { key ->
                    QuickOpenItem.entries.firstOrNull { entry -> entry.key == key }
                }.toSet()
            }
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
        TORCH(7, "torch", null, ExtraQuickOpenItem(
            "Torch",
            "YourMIUI/Torch",
            "keyguard_left_view_torchlight_n",
            "打开手电筒",
            ""
        ) {
            operator("com.miui.systemui.util.CommonUtil") {
                // modifier: public static | signature: toggleTorch()Z
                method("toggleTorch")?.call(null)
            }
            return@ExtraQuickOpenItem true
        });

        val constructor: ConstructorWrapper<*>? by lazy {
            if (clazzName == null) return@lazy null
            operator("com.android.keyguard.fod.item.$clazzName") {
                // modifier: public | signature: <init>(Landroid/graphics/RectF;Landroid/graphics/Region;Landroid/content/Context;)V
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
        val handler: () -> Boolean
    ) {
        private fun identifierOf() = "$EXTRA_ITEM_IDENTIFIER$$identifier"

        fun newInstance(context: Context?, vararg args: Any?): Any? {
            // 利用AddEventItem来实现额外功能项
            return operator(EXTRA_ITEM_CLASS) {
                // name: mView | type: android.widget.ImageView
                val viewField = field("mView") ?: return@operator null
                // name: mPackageName | type: java.lang.String
                val identifierField = field("mPackageName") ?: return@operator null
                // modifier: public | signature: <init>(Landroid/graphics/RectF;Landroid/graphics/Region;Landroid/content/Context;)V
                val instance = constructor(
                    RectF::class.java,
                    Region::class.java,
                    Context::class.java
                )?.new(*args) ?: return@operator null
                if (context != null) {
                    @SuppressLint("DiscouragedApi")
                    val identifier = context.resources.getIdentifier(
                        iconResName,
                        "drawable",
                        "com.android.systemui"
                    )
                    viewField.get<ImageView>(instance)?.setImageResource(identifier)
                    identifierField[instance] = identifierOf()
                }

                return@operator instance
            }
        }

        fun init() {
            val identifierField = operator(EXTRA_ITEM_CLASS) {
                // name: mPackageName | type: java.lang.String
                field("mPackageName")
            } ?: return

            fun Any.isExtraClass(): Boolean {
                if (this.javaClass.name != EXTRA_ITEM_CLASS) return false
                return identifierField.get<String>(this) == identifierOf()
            }

            operator(EXTRA_ITEM_CLASS) {
                // modifier: public | signature: getTag()Ljava/lang/String;
                method("getTag")?.overrideResult {
                    if (!it.instance!!.isExtraClass()) Unit
                    else tag
                }

                // modifier: public | signature: getTitle()Ljava/lang/String;
                method("getTitle")?.overrideResult {
                    if (!it.instance!!.isExtraClass()) Unit
                    else title
                }

                // modifier: public | signature: getSubTitle()Ljava/lang/String;
                method("getSubTitle")?.overrideResult {
                    if (!it.instance!!.isExtraClass()) Unit
                    else subtitle
                }
            }

            operator("com.android.keyguard.fod.MiuiGxzwQuickOpenView") {
                val itemClass = operator("com.android.keyguard.fod.item.IQuickOpenItem")?.delegate ?: return@operator
                // modifier: public final | signature: handleQuickOpenItemTouchUp(Lcom/android/keyguard/fod/item/IQuickOpenItem;)V
                (method("handleQucikOpenItemTouchUp") ?: method("handleQuickOpenItemTouchUp"))?.hookDoNothing {
                    handler() && it.argByClass(itemClass)?.isExtraClass() == true
                }
            }
        }
    }
}