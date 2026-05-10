package cc.meteormc.yourmiui.helper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.preferences.SettingsPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.toList
import kotlin.time.Duration.Companion.milliseconds

@Suppress("unused")
object GithubIssue {
    private const val GITHUB_APP_PACKAGE = "com.github.android"
    private const val NEW_ISSUE_URL = "https://github.com/Meteor2333/YourMIUI/issues/new"

    abstract class Template(
        private val title: String?,
        private val defaultName: String,
        private vararg val localeNames: Pair<SettingsPreferences.LanguageOption, String>
    ) {
        suspend fun open(context: Context, view: View) {
            val name = localeNames.firstOrNull { SettingsPreferences.language == it.first }?.second ?: defaultName
            val builder = NEW_ISSUE_URL.toUri()
                .buildUpon()
                .appendQueryParameter("template", name)
                .appendQueryParameter("title", title)

            for (field in this.javaClass.declaredFields) {
                field.isAccessible = true
                val key = field.name ?: continue
                val value = field[this]?.toString() ?: continue
                builder.appendQueryParameter(key, value)
            }

            // 由于 Github App 目前还不支持 Issue 模板功能
            // 所以不能简单地直接打开链接
            val uri = builder.build()
            val browsers = context.packageManager.queryIntentActivities(
                Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(uri),
                PackageManager.MATCH_ALL
            ).asFlow().mapNotNull {
                val activityInfo = it.activityInfo
                val packageName = activityInfo.packageName
                if (packageName == GITHUB_APP_PACKAGE) {
                    Toast.makeText(
                        context,
                        R.string.settings_misc_troubleshooter_github_app,
                        Toast.LENGTH_SHORT
                    ).show()
                    delay(2500.milliseconds)
                    null
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage(packageName)
                    intent.setClassName(packageName, activityInfo.name)
                    intent
                }
            }.onEmpty {
                Toast.makeText(
                    context,
                    R.string.settings_misc_troubleshooter_no_browser,
                    Toast.LENGTH_LONG
                ).show()
            }.toList()

            val base = browsers.lastOrNull() ?: return
            val chooser = Intent.createChooser(base, null)
            chooser.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                browsers.dropLast(1).toTypedArray()
            )
            context.startActivity(chooser)
        }
    }

    class BugReport(
        title: String?,
        private val description: String?,
        private val device: String?,
        private val os: String?,
        private val module: String?
    ) : Template(
        "[Bug] $title",
        "bug-report-english.yml",
        SettingsPreferences.LanguageOption.SIMPLIFIED_CHINESE to "bug-report-chinese.yml"
    )
}