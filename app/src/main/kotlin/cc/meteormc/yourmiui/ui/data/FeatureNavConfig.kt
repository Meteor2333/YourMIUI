package cc.meteormc.yourmiui.ui.data

import android.os.Parcel
import android.os.Parcelable
import cc.meteormc.yourmiui.common.Option
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
data class FeatureNavConfig(
    val key: String,
    val name: String,
    val description: String,
    val warning: String?,
    val testEnvironment: String?,
    val originalAuthor: String?,
    @TypeParceler<Option, OptionParceler>
    val options: List<Option>
): Parcelable {
    @Suppress("UNCHECKED_CAST")
    private object OptionParceler : Parceler<Option> {
        override fun Option.write(parcel: Parcel, flags: Int) {
            val type = Option.Type.getTypeByObject<Any>(getType()) ?: return

            parcel.writeString(getPreferenceKey())
            parcel.writeInt(getNameRes())
            parcel.writeInt(getSummaryRes())
            parcel.writeSerializable(type)
            parcel.writeString(type.serializer(getDefaultValue()))
        }

        override fun create(parcel: Parcel): Option {
            return object : Option {
                private val key = parcel.readString() ?: throw IllegalArgumentException("key")
                private val nameRes = parcel.readInt()
                private val summaryRes = parcel.readInt()
                private val type = Option.Type.getTypeByObject<Any>(
                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                        parcel.readSerializable(
                            this.javaClass.classLoader,
                            Option.Type::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        parcel.readSerializable() as? Option.Type<*>
                    }
                ) ?: throw IllegalArgumentException("type")
                private val defaultValue = parcel.readString()?.let {
                    type.deserializer(it)
                } ?: throw IllegalArgumentException("defaultValue")

                override fun getPreferenceKey() = this.key

                override fun getNameRes() = this.nameRes

                override fun getSummaryRes() = this.summaryRes

                override fun getType(): Option.Type<*> = this.type

                override fun getDefaultValue() = this.defaultValue
            }
        }
    }
}
