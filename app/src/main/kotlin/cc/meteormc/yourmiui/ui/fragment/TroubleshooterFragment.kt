package cc.meteormc.yourmiui.ui.fragment

import android.os.Build
import android.view.View
import androidx.lifecycle.lifecycleScope
import cc.meteormc.yourmiui.BuildConfig
import cc.meteormc.yourmiui.databinding.FragmentTroubleshooterBinding
import cc.meteormc.yourmiui.helper.GithubIssue
import cc.meteormc.yourmiui.helper.SysVersion
import kotlinx.coroutines.launch

class TroubleshooterFragment : BaseFragment<FragmentTroubleshooterBinding>({ inflater, container ->
    FragmentTroubleshooterBinding.inflate(inflater, container, false)
}) {
    override fun onCreate(): View {
        binding.button.setOnClickListener {
            openBugReport()
        }
        return binding.root
    }

    private fun openBugReport() {
        val template = GithubIssue.BugReport(
            "[Bug] test",
            "test\ntest\ntest",
            null,
            null,
            "${Build.BRAND} ${Build.MODEL}",
            SysVersion.getCurrent().fullName,
            BuildConfig.VERSION_NAME
        )
        lifecycleScope.launch {
            GithubIssue.open(requireContext(), template)
        }
    }
}