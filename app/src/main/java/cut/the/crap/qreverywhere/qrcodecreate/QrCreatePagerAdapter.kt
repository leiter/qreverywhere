package cut.the.crap.qreverywhere.qrcodecreate

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import cut.the.crap.qreverywhere.R
import java.lang.IllegalArgumentException

class QrCreatePagerAdapter (val context: Context, fragmentManger: FragmentManager) :
    FragmentPagerAdapter(fragmentManger, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

    private val title = listOf(
        R.string.qr_create_page_1,
        R.string.qr_create_page_1,
        R.string.qr_create_page_1,
        R.string.qr_create_page_1
    )

    override fun getPageTitle(position: Int): CharSequence {
        return context.getString(title[position])
    }

    override fun getCount(): Int {
        return title.size
    }

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> CreateEmailQrCodeFragment()
            1 -> CreateEmailQrCodeFragment()
            2 -> CreateEmailQrCodeFragment()
            3 -> CreateEmailQrCodeFragment()
            else -> throw IllegalArgumentException("Invalid fragment position.")
        }
    }
}