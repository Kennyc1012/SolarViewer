package com.kennyc.solarviewer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.kennyc.solarviewer.di.components.FragmentComponent

abstract class BindingFragment<T : ViewBinding> : Fragment() {
    protected val loggerTag = javaClass.simpleName

    protected open lateinit var binding: T

    protected val app: SolarApp
        get() {
            return requireContext().applicationContext as SolarApp
        }

    override fun onStart() {
        super.onStart()
        app.logger.d(loggerTag, "onStart")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app.logger.d(loggerTag, "onCreate")
    }

    override fun onPause() {
        super.onPause()
        app.logger.d(loggerTag, "onPause")
    }

    override fun onResume() {
        super.onResume()
        app.logger.d(loggerTag, "onResume")
    }

    override fun onStop() {
        super.onStop()
        app.logger.d(loggerTag, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        app.logger.d(loggerTag, "onDestroy")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        app.logger.d(loggerTag, "onDestroyView")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        app.logger.d(loggerTag, "onAttach")
        inject(app.component.fragmentComponentBuilder().fragment(this).build())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        app.logger.d(loggerTag, "onCreateView")
        binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app.logger.d(loggerTag, "onViewCreated")
    }

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): T

    abstract fun inject(component: FragmentComponent)
}