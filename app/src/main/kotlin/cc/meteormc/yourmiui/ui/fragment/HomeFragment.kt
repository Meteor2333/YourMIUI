package cc.meteormc.yourmiui.ui.fragment

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.databinding.FragmentHomeBinding
import cc.meteormc.yourmiui.databinding.ItemHomeHeaderBinding
import cc.meteormc.yourmiui.helper.ResourceParser
import cc.meteormc.yourmiui.helper.ResourceParser.getDiaplayName
import cc.meteormc.yourmiui.helper.UpdateChecker
import cc.meteormc.yourmiui.store.HostStore
import cc.meteormc.yourmiui.ui.adapter.BaseAdapter
import cc.meteormc.yourmiui.ui.adapter.CategoryAdapter
import cc.meteormc.yourmiui.ui.adapter.SearchResultAdapter
import cc.meteormc.yourmiui.ui.data.SearchResultItem
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.tanh

class HomeFragment : BaseFragment<FragmentHomeBinding>({ inflater, container ->
    FragmentHomeBinding.inflate(inflater, container, false)
}) {
    override fun onCreateView(): View {
        val pageList = binding.pageList
        pageList.layoutManager = LinearLayoutManager(requireContext())
        pageList.adapter = ConcatAdapter(
            HeaderAdapter(),
            CategoryAdapter(HostStore.features.keys.toList())
        )
        return binding.root
    }

    private inner class HeaderAdapter : BaseAdapter<ItemHomeHeaderBinding, Unit?>(
        arrayOfNulls(1),
        { inflater, parent -> ItemHomeHeaderBinding.inflate(inflater, parent, false) }
    ) {
        private var isInitialized: Boolean = false

        override fun newHolder(binding: ItemHomeHeaderBinding): BaseAdapter<ItemHomeHeaderBinding, Unit?>.BaseViewHolder {
            return object : BaseViewHolder(binding, binding.root) {
                override fun onBind(item: Unit?) {
                    if (isInitialized) return
                    binding.bindModuleStatus()
                    binding.bindModuleUpdate()
                    binding.bindDeviceInfo()
                    binding.bindSearchAnchor()
                    isInitialized = true
                }
            }
        }

        private fun ItemHomeHeaderBinding.bindModuleStatus() {
            var anchorX = 0f
            var anchorY = 0f
            statusCard.setOnTouchListener { view, event ->
                fun curve(v: Float): Float {
                    val sign = sign(v)
                    val abs = abs(v)
                    return sign * tanh(abs * 0.32f)
                }

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        view.parent.requestDisallowInterceptTouchEvent(true)
                        anchorX = event.x
                        anchorY = event.y
                        view.animate()
                            .scaleX(0.98f)
                            .scaleY(0.98f)
                            .setDuration(120)
                            .start()
                    }
                    MotionEvent.ACTION_CANCEL,
                    MotionEvent.ACTION_POINTER_DOWN,
                    MotionEvent.ACTION_UP -> {
                        view.parent.requestDisallowInterceptTouchEvent(false)
                        view.animate()
                            .translationX(0f)
                            .translationY(0f)
                            .rotationX(0f)
                            .rotationY(0f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(240)
                            .start()
                        view.performClick()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = curve((event.x - anchorX) / (view.width / 2f))
                        val dy = curve((event.y - anchorY) / (view.height / 2f))
                        view.translationX = dx * 24f
                        view.translationY = dy * 12f
                        view.rotationY = dx * 12f
                        view.rotationX = -dy * 6f
                    }
                    else -> {
                        return@setOnTouchListener false
                    }
                }

                true
            }

            statusIcon.setImageResource(
                if (HostStore.isActivated) R.drawable.ic_check_24dp
                else R.drawable.ic_cross_24dp
            )

            statusText.setText(
                if (HostStore.isActivated) R.string.status_active
                else R.string.status_inactive
            )

            statusVersion.text = getString(
                R.string.status_version,
                "${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}"
            )

            statusApi.text = if (HostStore.isActivated) {
                "Activated by ${HostStore.apiName} (API ${HostStore.apiVersion})"
            } else "Not activated"
        }

        private fun ItemHomeHeaderBinding.bindModuleUpdate() {
            lifecycleScope.launch {
                UpdateChecker.fetch(requireContext())
                if (!UpdateChecker.hasUpdate) return@launch

                updateCard.visibility = View.VISIBLE
                updateCard.setOnClickListener {
                    UpdateChecker.downloadUrl?.let {
                        val uri = it.toUri()
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                    }
                }
            }
        }

        private fun ItemHomeHeaderBinding.bindDeviceInfo() {
//        binding.infoDeviceModel.text = PropertiesUtil.get("ro.product.marketname")
//        binding.infoSystemCode.text = Build.DEVICE
//        binding.infoAndroidVersion.text = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
//        binding.infoMiuiVersion.text = SysVersion.getCurrent().code.toString()
//        binding.infoCpuAbi.text = Build.SUPPORTED_ABIS.firstOrNull() ?: System.getProperty("os.arch", Build.UNKNOWN)
        }

        private fun ItemHomeHeaderBinding.bindSearchAnchor() {
            val homeAppbar = binding.homeAppbar
            val pageList = binding.pageList
            val searchContainer = binding.searchContainer
            val searchPanel = binding.searchPanel
            val searchInput = binding.searchInput
            val searchCancel = binding.searchCancel
            val searchResultList = binding.searchResultList
            val searchEmpty = binding.searchEmpty

            var panelStartY = 0f
            var statusbarHeight = 0
            var animator: ValueAnimator? = null
            lateinit var adapter: SearchResultAdapter
            lateinit var backCallback: OnBackPressedCallback

            fun buildAnimator(
                from: Float, to: Float,
                startListener: (animator: Animator) -> Unit = { },
                endListener: (animator: Animator) -> Unit = { }
            ) {
                animator = ValueAnimator.ofFloat(from, to).apply {
                    duration = 300L
                    interpolator = PathInterpolator(0.2f, 0f, 0f, 1f)

                    addUpdateListener {
                        val progress = it.animatedValue as Float
                        val reverse = 1f - progress
                        val searchCancelLeftMargin = searchCancel.width + searchCancel.marginEnd
                        homeAppbar.alpha = reverse
                        pageList.alpha = reverse
                        pageList.translationY = panelStartY * progress * -0.382f
                        searchPanel.translationY = panelStartY * reverse
                        searchInput.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            marginStart = searchInput.marginStart
                            marginEnd = (searchCancelLeftMargin * progress).toInt() + marginStart
                        }
                        searchCancel.alpha = progress
                        searchCancel.translationX = searchCancelLeftMargin * reverse
                        searchResultList.alpha = progress
                        searchEmpty.alpha = progress
                    }
                    doOnStart(startListener)
                    doOnEnd(endListener)
                    start()
                }
            }

            fun isInSearch(): Boolean {
                return searchContainer.isVisible
            }

            fun enterSearch() {
                if (isInSearch()) return
                if (!searchAnchor.isLaidOut) {
                    searchAnchor.post { enterSearch() }
                    return
                }

                animator?.cancel()
                adapter.submitList(emptyList())
                pageList.stopScroll()
                searchInput.text?.clear()

                val rootLocation = IntArray(2)
                val viewLocation = IntArray(2)
                binding.root.getLocationInWindow(rootLocation)
                searchAnchor.getLocationInWindow(viewLocation)
                val rootY = (viewLocation[1] - rootLocation[1]).toFloat()
                panelStartY = (rootY - searchAnchor.marginTop - statusbarHeight).coerceAtLeast(0f)

                buildAnimator(
                    0f, 1f,
                    startListener = {
                        searchAnchor.alpha = 0f
                        homeAppbar.alpha = 1f
                        pageList.translationY = 0f
                        searchContainer.visibility = View.VISIBLE
                        searchPanel.translationY = panelStartY
                        searchInput.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            marginStart = searchInput.marginStart
                            marginEnd = marginStart
                        }
                        searchCancel.alpha = 0f
                        searchCancel.translationX = (searchCancel.width + searchCancel.marginEnd).toFloat()
                        searchResultList.alpha = 0f
                        searchEmpty.alpha = 0f
                    },
                    endListener = {
                        backCallback.isEnabled = true
                        pageList.visibility = View.GONE
                        searchInput.showKeyboard()
                        searchInput.requestFocus()
                    }
                )
            }

            fun exitSearch() {
                if (!isInSearch()) return

                animator?.cancel()
                adapter.submitList(emptyList())
                pageList.stopScroll()
                searchInput.text?.clear()

                buildAnimator(
                    1f, 0f,
                    startListener = {
                        backCallback.isEnabled = false
                        pageList.visibility = View.VISIBLE
                        searchInput.hideKeyboard()
                        searchInput.clearFocus()
                    },
                    endListener = {
                        searchAnchor.alpha = 1f
                        homeAppbar.alpha = 1f
                        pageList.translationY = 0f
                        searchContainer.visibility = View.GONE
                        searchPanel.translationY = 0f
                        searchInput.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            marginStart = searchInput.marginStart
                            marginEnd = marginStart
                        }
                        searchCancel.alpha = 0f
                        searchCancel.translationX = 0f
                        searchResultList.alpha = 0f
                        searchEmpty.alpha = 0f
                    }
                )
            }

            val searchPanelHeight by lazy { searchPanel.layoutParams.height }
            val searchPanelTopPadding by lazy { searchPanel.paddingTop }
            val searchResultListTopMargin by lazy { searchResultList.marginTop }
            val searchEmptyTopMargin by lazy { searchEmpty.marginTop }
            adapter = SearchResultAdapter(emptyArray())
            backCallback = object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    exitSearch()
                }
            }

            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
                statusbarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                searchPanel.updateLayoutParams {
                    height = searchPanelHeight + statusbarHeight
                }
                searchPanel.setPadding(
                    searchPanel.paddingLeft,
                    searchPanelTopPadding + statusbarHeight,
                    searchPanel.paddingRight,
                    searchPanel.paddingBottom
                )
                searchResultList.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = searchResultListTopMargin + statusbarHeight
                }
                searchEmpty.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = searchEmptyTopMargin + statusbarHeight
                }
                insets
            }
            ViewCompat.requestApplyInsets(binding.root)

            homeAppbar.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                behavior = object : AppBarLayout.Behavior() {
                    override fun onStartNestedScroll(
                        parent: CoordinatorLayout,
                        child: AppBarLayout,
                        directTargetChild: View,
                        target: View,
                        nestedScrollAxes: Int,
                        type: Int
                    ): Boolean {
                        if (isInSearch()) return false
                        return super.onStartNestedScroll(
                            parent,
                            child,
                            directTargetChild,
                            target,
                            nestedScrollAxes,
                            type
                        )
                    }
                }
            }
            searchInput.setOnEditorActionListener { view, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchInput.hideKeyboard()
                    searchInput.clearFocus()
                    true
                } else false
            }
            searchInput.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                var results = emptyList<SearchResultItem>()
                if (text.isNullOrBlank()) {
                    binding.searchEmpty.visibility = View.GONE
                } else {
                    val query = text.toString()
                    val context = requireContext()
                    val primaryColor by lazy {
                        val typedValue = TypedValue()
                        context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
                        typedValue.data
                    }

                    results = HostStore.features.entries.flatMap { entry ->
                        val category = entry.key
                        val features = entry.value
                        features.flatMap { feature ->
                            buildList {
                                fun match(title: String, path: String): SearchResultItem? {
                                    val spannableTitle = SpannableString(title)
                                    val index = title.indexOf(query, ignoreCase = true).takeIf { it >= 0 } ?: return null
                                    spannableTitle.setSpan(
                                        ForegroundColorSpan(primaryColor),
                                        index,
                                        index + query.length,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )

                                    val result = SearchResultItem(category, feature, spannableTitle, path)
                                    add(result)
                                    return result
                                }

                                val categoryName = category.getDiaplayName()
                                val featureName = context.getString(ResourceParser.parseResName(context, feature.name))
                                val featureDesc = context.getString(ResourceParser.parseResName(context, feature.description))
                                match(featureName, categoryName) ?:
                                match(featureDesc, "$categoryName / $featureName") ?:
                                feature.options.forEach { option ->
                                    val optionName = context.getString(ResourceParser.parseResName(context, option.name))
                                    val optionDesc = context.getString(ResourceParser.parseResName(context, option.description))
                                    match(optionName, "$categoryName / $featureName") ?:
                                    match(optionDesc, "$categoryName / $featureName / $optionName")
                                }
                            }
                        }
                    }

                    binding.searchEmpty.text = getString(R.string.feature_search_empty, query)
                    binding.searchEmpty.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
                }

                adapter.submitList(results)
            })
            searchCancel.setOnClickListener { exitSearch() }
            searchResultList.adapter = adapter
            searchResultList.layoutManager = LinearLayoutManager(requireContext())
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

            searchAnchor.setOnClickListener {
                enterSearch()
            }
        }
    }
}
