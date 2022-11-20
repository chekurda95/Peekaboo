package com.chekurda.peekaboo.main_screen.presentation

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import com.chekurda.common.base_fragment.BasePresenterFragment
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.peekaboo.main_screen.R
import com.chekurda.peekaboo.main_screen.contact.MainScreenFragmentFactory
import com.chekurda.peekaboo.main_screen.data.Message
import com.chekurda.peekaboo.main_screen.presentation.views.ConnectionStateView
import com.chekurda.peekaboo.main_screen.presentation.views.pine.PineScreenView
import com.chekurda.peekaboo.main_screen.presentation.views.user.UserScreenView
import com.chekurda.peekaboo.main_screen.utils.PermissionsHelper
import com.chekurda.peekaboo.main_screen.utils.RecordingDeviceHelper
import kotlin.math.roundToInt

/**
 * Фрагмент главного экрана.
 */
internal class MainScreenFragment : BasePresenterFragment<MainScreenContract.View, MainScreenContract.Presenter>(),
    MainScreenContract.View {

    companion object : MainScreenFragmentFactory {
        override fun createMainScreenFragment(): Fragment = MainScreenFragment()
    }

    override val layoutRes: Int = R.layout.main_screen_fragment

    private var mainScreenView: ViewGroup? = null
    private var pineModeButton: Button? = null
    private var userModeButton: Button? = null
    private var pineScreenView: PineScreenView? = null
    private var userScreenView: UserScreenView? = null

    private var permissionsHelper: PermissionsHelper? = null
    private var deviceHelper: RecordingDeviceHelper? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        permissionsHelper = PermissionsHelper(requireActivity(), permissions, PERMISSIONS_REQUEST_CODE)
        deviceHelper = RecordingDeviceHelper(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews(view: View) {
        mainScreenView = view.findViewById(R.id.main_screen_root)
        pineModeButton = view.findViewById<Button?>(R.id.pine_mode).apply {
            setOnClickListener {
                permissionsHelper?.withPermissions {
                    onPineModeSelected()
                }
            }
        }
        userModeButton = view.findViewById<Button?>(R.id.user_mode).apply {
            setOnClickListener {
                permissionsHelper?.withPermissions {
                    onUserModeSelected()
                }
            }
        }
    }

    private fun onPineModeSelected() {
        mainScreenView?.apply {
            pineScreenView = PineScreenView(context)
            showScreen(pineScreenView!!)
            presenter.onPineModeSelected()
        }
    }

    private fun onUserModeSelected() {
        mainScreenView?.apply {
            userScreenView = UserScreenView(context).apply {
                attachController(presenter)
            }
            showScreen(userScreenView!!)
            presenter.onUserModeSelected()
        }
    }

    override fun updateSearchState(isRunning: Boolean) {
        pineScreenView?.apply {
            if (isRunning) state = ConnectionStateView.State.SEARCH_PINE_LOVERS
        }
    }

    override fun updateConnectionState(isConnected: Boolean) {
        pineScreenView?.apply {
            if (isConnected) state = ConnectionStateView.State.CONNECTED
        } ?: userScreenView?.updateConnectionState(isConnected)
    }

    override fun updateMessageList(messageList: List<Message>) {
        userScreenView?.updateMessageList(messageList)
    }

    override fun onStart() {
        super.onStart()
        deviceHelper?.configureDevice(isStartRecording = true)
    }

    override fun onStop() {
        super.onStop()
        deviceHelper?.configureDevice(isStartRecording = false)
    }

    override fun onResume() {
        super.onResume()
        permissionsHelper?.requestPermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainScreenView = null
        pineModeButton = null
        userModeButton = null
        pineScreenView = null
        userScreenView = null
    }

    private fun showScreen(view: View) {
        mainScreenView?.apply {
            addView(view, MATCH_PARENT, MATCH_PARENT)
            view.translationZ = dp(20).toFloat()
            view.alpha = 0f
            ValueAnimator.ofFloat(0f, 1f).apply {
                interpolator = DecelerateInterpolator()
                duration = 300
                var startPosition = 0
                addUpdateListener {
                    view.alpha = it.animatedFraction
                    view.translationX = startPosition * (1f - animatedFraction)
                }
                doOnEnd {
                    removeView(pineModeButton)
                    removeView(userModeButton)
                    view.translationZ = 0f
                    pineModeButton = null
                    userModeButton = null
                }
                doOnPreDraw {
                    startPosition = ((mainScreenView?.width ?: 0) * 0.2f).roundToInt()
                    resume()
                }
                start()
                pause()
            }
        }
    }

    override fun provideHandler(): Handler = requireView().handler

    override fun provideActivity(): Activity = requireActivity()

    /**
     * DI Press F.
     */
    override fun createPresenter(): MainScreenContract.Presenter = MainScreenPresenterImpl()
}

private val permissions = arrayOf(
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)
private const val PERMISSIONS_REQUEST_CODE = 102