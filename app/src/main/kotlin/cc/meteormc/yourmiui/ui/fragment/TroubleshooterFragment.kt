package cc.meteormc.yourmiui.ui.fragment

import android.os.Build
import android.view.View
import androidx.lifecycle.lifecycleScope
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.databinding.FragmentTroubleshooterBinding
import cc.meteormc.yourmiui.helper.GithubIssue
import cc.meteormc.yourmiui.helper.SysVersion
import cc.meteormc.yourmiui.store.HostStore
import kotlinx.coroutines.launch

class TroubleshooterFragment : BaseFragment<FragmentTroubleshooterBinding>({ inflater, container ->
    FragmentTroubleshooterBinding.inflate(inflater, container, false)
}) {
    private val problems = listOf(
        Problem.ModuleNotActivated,
        Problem.FeatureNotWorking,
        Problem.AppCrash
    )
    private var currentStep: Step? = null

    override fun onCreate(): View {
        return binding.root
    }

    private fun openIssue(template: GithubIssue.Template) {
        lifecycleScope.launch {
            GithubIssue.open(requireContext(), template)
        }
    }

    private sealed class Problem<T : Problem<T>>(
        val name: String,
        val steps: List<T.() -> Step>,
        val template: T.() -> GithubIssue.Template,
        val condition: () -> Boolean = { true }
    ) {
        private val existSteps = mutableListOf<Step>()
        private val tips = listOf(
            "<!--",
            "tips:",
            "我们已提前填充了一些模板内容，你仅需完成剩余必填部分",
            "也可以继续详细描述：问题出现的频率、是否有特定的触发条件等",
            "最后请提供你的框架日志文件以及截图，这些信息有助于更快地定位和解决问题",
            "(提交前可删除此段内容)",
            "-->"
        )

        fun createBugReport(description: String): GithubIssue.Template {
            val builder = mutableListOf(description)
            builder.add("")
            builder.add("已按照疑难解答步骤进行检查，但问题依旧存在")
            builder.addAll(existSteps.map { "- ${it.name}" })
            builder.add("")
            builder.addAll(tips)
            return GithubIssue.BugReport(
                name,
                builder.joinToString("\n"),
                null,
                null,
                "${Build.BRAND} ${Build.MODEL}",
                SysVersion.getCurrent().fullName,
                BuildConfig.VERSION_NAME
            )
        }

        protected fun createFeatureRequest(): GithubIssue.Template {
            return GithubIssue.FeatureRequest(
                name
            )
        }

        object ModuleNotActivated : Problem<ModuleNotActivated>(
            "模块无法激活",
            listOf(
                { Step.ModuleCheck },
                { Step.Reboot }
            ),
            {
                createBugReport("模块不能正常激活")
            },
            {
                HostStore.isActivated.value != true
            }
        )

        object FeatureNotWorking : Problem<FeatureNotWorking>(
            "功能不生效",
            listOf(
                { Step.ModuleCheck },
                { Step.FeatrueSelect { scope, feature ->
                    this.scope = scope
                    this.feature = feature
                } },
                { Step.FeatureCheck(this.feature) },
                { Step.ScopeCheck(this.scope) }
            ),
            {
                createBugReport("于 `${scope.id}` 的功能 `${feature.id}` 未预期工作")
            }
        ) {
            private lateinit var scope: Scope
            private lateinit var feature: Feature
        }

        object AppCrash : Problem<AppCrash>(
            "应用闪退",
            listOf(
                { Step.ScopeSelect { scope ->
                    this.scope = scope
                } }
            ),
            { createBugReport(
                "作用域 `${scope.id}` 闪退"
            ) }
        ) {
            private lateinit var scope: Scope
        }
    }

    private sealed class Step(
        val name: String,
        val title: String,
        val subtitle: String?
    ) {
        object ModuleCheck : Step(
            "模块检查",
            "请确保已正确安装并在框架中启用了模块",
            "请检查框架的模块管理界面是否存在并勾选了本模块，检查无误后点击下一步"
        )

        class ScopeSelect(private val onSelect: (Scope) -> Unit) : Step(
            "作用域选择",
            "请在下列选项中选择遇到问题的作用域",
            null
        )

        class FeatrueSelect(private val onSelect: (Scope, Feature) -> Unit) : Step(
            "功能选择",
            "请在下列选项中选择遇到问题的功能",
            null
        )

        class ScopeCheck(scope: Scope) : Step(
            "作用域检查",
            "请确保已正确在框架中启用了对应作用域",
            "请检查框架的模块管理界面是否勾选了以下作用域，检查无误后点击下一步"
        )

        class FeatureCheck(feature: Feature) : Step(
            "功能检查",
            "请确保已正确在框架中启用了对应功能",
            "请检查框架的模块管理界面是否勾选了以下功能，检查无误后点击下一步"
        )

        object Reboot : Step(
            "重启设备",
            "请重启设备后再次尝试",
            "请长按电源键重启设备，完成后再次尝试相关功能，检查问题是否解决。如果已按上述要求进行检查，问题依旧存在，请点击下一步"
        )
    }
}