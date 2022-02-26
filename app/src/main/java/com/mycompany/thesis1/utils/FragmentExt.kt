package com.mycompany.thesis1.utils

import android.os.Bundle
import androidx.fragment.app.Fragment

inline fun <T : Fragment> T.withArguments(crossinline block: Bundle.() -> Unit): T {
    val bundle = Bundle()
    block.invoke(bundle)
    arguments = bundle
    return this
}