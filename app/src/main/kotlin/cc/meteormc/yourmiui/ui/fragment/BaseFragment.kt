package cc.meteormc.yourmiui.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class BaseFragment<T : Any>(
    private val factory: (inflater: LayoutInflater, container: ViewGroup?) -> T
) : Fragment() {
    protected lateinit var binding: T

    protected abstract fun onCreate(): View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = factory(inflater, container)
        return this.onCreate()
    }
}