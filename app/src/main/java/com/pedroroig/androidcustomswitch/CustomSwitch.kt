package com.pedroroig.androidcustomswitch

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class CustomSwitch @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var onSwitchChangeListener: (Boolean) -> Unit = {}
    private var switchOn = false

    // By now set a default animation time
    private var animationTime: Long = 300
    private lateinit var leftText: String
    private lateinit var rightText: String

    private val toggle: ViewGroup
    private val textToggle: TextView
    private val textBackRight: TextView
    private val textBackLeft: TextView


    init {
        LayoutInflater.from(context).inflate(R.layout.custom_switch, this)

        toggle = findViewById(R.id.toggle_custom_switch)
        textToggle = findViewById(R.id.text_toggle)
        textBackRight = findViewById(R.id.text_back_right)
        textBackLeft = findViewById(R.id.text_back_left)

        initListener()
        initAttributes(attrs)

        textToggle.text = leftText
        textBackLeft.text = leftText
        textBackRight.text = rightText

    }

    private fun initAttributes(attrs: AttributeSet?) {
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.custom_switch, 0, 0
        )
        try {
            animationTime =
                attributes.getInt(R.styleable.custom_switch_animationTime, 300).toLong()
            leftText = attributes.getString(R.styleable.custom_switch_leftText)
                    ?: throw IllegalArgumentException("Left text must be provided for the custom switch")
            rightText = attributes.getString(R.styleable.custom_switch_rightText)
                ?: throw IllegalArgumentException("Right text must be provided for the custom switch")
        } finally {
            attributes.recycle()
        }
    }


    /**
     * when click received:
     *  change internal checked state
     *  run toggle moving animation
     *  run text fading animation
     *  execute the provided check change listener
     */
    private fun initListener() {
        setOnClickListener {
            if(!switchOn) {
                goToRightAnimation().start()
                fadeToggleTextAnimation().start()
            } else {
                goToLeftAnimation().start()
                fadeToggleTextAnimation().start()
            }
            switchOn = !switchOn

            onSwitchChangeListener(switchOn)
        }
    }

    /**
     * Moves toggle to the right. Disables click listener during animation
     */
    private fun goToRightAnimation(): ValueAnimator {
        val initX = toggle.x
        val finalX  = toggle.x + toggle.width
        return ValueAnimator.ofFloat(
            initX,
            finalX
        ).apply {
            this.duration = animationTime
            addUpdateListener { valueAnimator ->
                val currentValue = valueAnimator!!.animatedValue as Float
                toggle.x = currentValue
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    textToggle.text = rightText
                }
            })
        }
    }

    /**
     * Moves toggle to the left. Disables click listener during animation
     */
    private fun  goToLeftAnimation(): ValueAnimator {
        val initX = toggle.x
        val finalX = toggle.x - toggle.width
        return ValueAnimator.ofFloat(
            initX,
            finalX
        ).apply {
            this.duration = animationTime
            addUpdateListener { valueAnimator ->
                val currentValue = valueAnimator!!.animatedValue as Float
                toggle.x = currentValue
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    isClickable = false
                }
            })
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    textToggle.text = leftText
                    isClickable = true
                }
            })
        }
    }

    /**
     * Gradually fades the text inside the toggle. When finished it resets opacity to maximum
     */
    private fun  fadeToggleTextAnimation(): ValueAnimator {
        val initAlpha = 1F
        val finalAlpha = 0F
        return ValueAnimator.ofFloat(
            initAlpha,
            finalAlpha
        ).apply {
            this.duration = animationTime
            addUpdateListener { valueAnimator ->
                val currentValue = valueAnimator!!.animatedValue as Float
                textToggle.alpha = currentValue
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    isClickable = false
                }
            })
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    textToggle.alpha = 1F
                    isClickable = true
                }
            })
        }
    }

    /**
     * Listener for switch changes. Will return true when it moves to the right, false to the left.
     */
    fun setOnSwitchChangeListener(listener: (Boolean) -> Unit) {
        onSwitchChangeListener = listener
    }

}
