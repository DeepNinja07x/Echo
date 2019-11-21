package internshala.com.echomusik.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import internshala.com.echomusik.R
import internshala.com.echomusik.activties.MainActivity
import internshala.com.echomusik.fragments.AboutUsFragment
import internshala.com.echomusik.fragments.FavouriteFragment
import internshala.com.echomusik.fragments.MainScreenFragment
import internshala.com.echomusik.fragments.SettingsFragment


/**
 * Created by ADMIN on 6/19/2017.
 */

class NavigationDrawerAdapter(_contentList: ArrayList<String>, _getImages: IntArray, _context: Context) : RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>() {
    var contentList: ArrayList<String>? = null
    var getImages: IntArray? = null
    var mContext: Context? = null


    init {
        this.contentList = _contentList
        this.getImages = _getImages
        this.mContext = _context
    }

    override fun onBindViewHolder(holder: NavigationDrawerAdapter.NavViewHolder, position: Int) {
        holder.text_GET?.setText(contentList?.get(position))
        holder.icon_GET?.setBackgroundResource(getImages?.get(position) as Int)
        holder.contentHolder?.setOnClickListener {
            when (position) {
                0 -> {
                    val mainScreenFragment = MainScreenFragment()
                    (mContext as MainActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, mainScreenFragment)
                            .commit()
                }
                1 -> {
                    val favouriteFragment = FavouriteFragment()
                    (mContext as MainActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, favouriteFragment)
                            .commit()
                }
                2 -> {
                    val settingsFragment = SettingsFragment()
                    (mContext as MainActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, settingsFragment)
                            .commit()

                }
                3 -> {
                    val aboutUsFragment = AboutUsFragment()
                    (mContext as MainActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, aboutUsFragment)
                            .commit()
                }
            }
            MainActivity.Staticated.drawerLayout?.closeDrawers()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_custom_navigationdrawer, parent, false)
        return NavViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return contentList?.size as Int
    }


    class NavViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var icon_GET: ImageView? = null
        var text_GET: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            icon_GET = view.findViewById(R.id.icon_navdrawer) as ImageView
            text_GET = view.findViewById(R.id.text_navdrawer) as TextView
            contentHolder = view.findViewById(R.id.navdrawer_item_content_holder) as RelativeLayout
        }

    }
}
