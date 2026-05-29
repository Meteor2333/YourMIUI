package cc.meteormc.yourmiui.common.util

import java.lang.reflect.Field

object PrefsUtil {
    const val SHARED_PREFERENCES_NAME = "shared"

    private val regex = Regex("([a-z0-9])([A-Z])")

    fun getFeatureKey(
        featureId: String
    ) = "feature_${
        featureId.replace(regex, "$1_$2")
            .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1_$2")
            .lowercase()
    }"

    fun getFeatureEnabledKey(
        featureId: String
    ) = "${this.getFeatureKey(featureId)}_enabled"

    fun getFeatureOptionKey(
        featureId: String,
        optionField: Field
    ) = "${this.getFeatureKey(featureId)}_${optionField.name.replace(regex, "$1_$2")}"
}