package cut.the.crap.qreverywhere.utils

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.setTitle(@StringRes title: Int){
    (activity as AppCompatActivity).supportActionBar?.setTitle(title)
}