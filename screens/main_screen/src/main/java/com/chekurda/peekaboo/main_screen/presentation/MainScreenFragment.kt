package com.chekurda.peekaboo.main_screen.presentation

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
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
import com.chekurda.peekaboo.main_screen.presentation.views.ConnectionStateView
import com.chekurda.peekaboo.main_screen.presentation.views.game_master.GameMasterScreenView
import com.chekurda.peekaboo.main_screen.presentation.views.player.PlayerScreenView
import com.chekurda.peekaboo.main_screen.utils.PermissionsHelper
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
    private var gameMasterModeButton: Button? = null
    private var playerModeButton: Button? = null
    private var gameMasterScreenView: GameMasterScreenView? = null
    private var playerScreenView: PlayerScreenView? = null

    private var permissionsHelper: PermissionsHelper? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        permissionsHelper = PermissionsHelper(requireActivity(), permissions, PERMISSIONS_REQUEST_CODE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews(view: View) {
        mainScreenView = view.findViewById(R.id.main_screen_root)
        gameMasterModeButton = view.findViewById<Button?>(R.id.master_mode_button).apply {
            setOnClickListener {
                permissionsHelper?.withPermissions {
                    onGameMasterModeSelected()
                }
            }
        }
        playerModeButton = view.findViewById<Button?>(R.id.player_mode_button).apply {
            setOnClickListener {
                permissionsHelper?.withPermissions {
                    onPlayerModeSelected()
                }
            }
        }
    }

    private fun onGameMasterModeSelected() {
        mainScreenView?.apply {
            gameMasterScreenView = GameMasterScreenView(context)
            showScreen(gameMasterScreenView!!)
            presenter.onMasterModeSelected()
        }
    }

    private fun onPlayerModeSelected() {
        mainScreenView?.apply {
            playerScreenView = PlayerScreenView(context)
            showScreen(playerScreenView!!)
            presenter.onPlayerModeSelected()
        }
    }

    override fun showMaxRssi(rssi: Int) {
        Log.e("TAGTAG", "rssi $rssi")
    }

    override fun updateSearchState(isRunning: Boolean) {
        gameMasterScreenView?.apply {
            if (isRunning) state = ConnectionStateView.State.SEARCHING_PLAYERS
        }
    }

    override fun updateConnectionState(isConnected: Boolean) {
        gameMasterScreenView?.apply {
            if (isConnected) state = ConnectionStateView.State.READY
        } ?: playerScreenView?.updateConnectionState(isConnected)
    }

    override fun onResume() {
        super.onResume()
        permissionsHelper?.requestPermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainScreenView = null
        gameMasterModeButton = null
        playerModeButton = null
        gameMasterScreenView = null
        playerScreenView = null
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
                    removeView(gameMasterModeButton)
                    removeView(playerModeButton)
                    view.translationZ = 0f
                    gameMasterModeButton = null
                    playerModeButton = null
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
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)
private const val PERMISSIONS_REQUEST_CODE = 102