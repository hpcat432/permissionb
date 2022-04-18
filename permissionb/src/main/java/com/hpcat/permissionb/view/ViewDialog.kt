package com.hpcat.permissionb.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.hpcat.permissionb.R

class ViewDialog(private val container: ViewGroup,
                 context: Context,
                 private val contentView: View,
                 gravity: Int = Gravity.BOTTOM) : DialogInterface {

    companion object {
        const val ANIM_DURATION = 200L
        const val DIALOG_TRANS_Y = 100F
    }

    var isShowing = false

    var cancelListener: DialogInterface.OnCancelListener? = null
    var dismissListener: DialogInterface.OnDismissListener? = null

    var setCanceledOnTouchOutside = true

    private val contentLayout = LayoutInflater.from(context).inflate(R.layout.view_dialog_layout, null, false) as FrameLayout

    init {
        contentLayout.addView(contentView)
        val params = contentView.layoutParams as FrameLayout.LayoutParams
        params.gravity = gravity
        params.height = FrameLayout.LayoutParams.WRAP_CONTENT

        contentLayout.setOnClickListener {
            if (setCanceledOnTouchOutside) {
                cancel()
            }
        }
    }

    fun setOutsideColor(@ColorInt color: Int) {
        contentLayout.setBackgroundColor(color)
    }

    override fun cancel() {
    }

    override fun dismiss() {
        if (isShowing) {
            showAnim(false, withCancel = true)
        }
    }

    fun safeShow() {
        isShowing = true
        showAnim(true)
    }

    private fun showAnim(show: Boolean, withCancel: Boolean = false) {
        if (show) {
            contentLayout.alpha = 0F
            contentView.translationY = DIALOG_TRANS_Y
            if (contentView.parent != null) {
                container.removeView(contentLayout)
            }
            container.addView(contentLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        } else {
            contentLayout.alpha = 1F
            contentView.translationY = 0F
        }

        val set = AnimatorSet()
        val alphaAnimator = ObjectAnimator.ofFloat(contentLayout, "alpha", if (show) 1F else 0F)
            .setDuration(ANIM_DURATION)
        val dialogTransAnimator = ObjectAnimator.ofFloat(contentView, "translationY", if (show) 0F else DIALOG_TRANS_Y)
            .setDuration(ANIM_DURATION)

        set.playTogether(alphaAnimator, dialogTransAnimator)
        set.start()
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                if (!show) {
                    container.removeView(contentLayout)
                    if (withCancel) cancelListener?.onCancel(this@ViewDialog)
                    dismissListener?.onDismiss(this@ViewDialog)
                }
                isShowing = show
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }

        })

    }
}