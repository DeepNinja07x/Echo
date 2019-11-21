package internshala.com.echomusik.fragments

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.NonNull
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import internshala.com.echomusik.R
import internshala.com.echomusik.R.id.action_sort_recent
import internshala.com.echomusik.adapters.MainScreenAdapter
import internshala.com.echomusik.fragments.MainScreenFragment.Statified.mMediaPlayer
import internshala.com.echomusik.models.Songs
import internshala.com.echomusik.utils.CurrentSongHelper
import org.jetbrains.annotations.NonNls
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by ADMIN on 6/19/2017.
 */
class MainScreenFragment : Fragment() {

    var getSongsList: ArrayList<Songs>? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var playPauseHelper: CurrentSongHelper? = null
    var songTitle: TextView? = null
    var recyclerView: RecyclerView? = null
    var _mainScreenAdapter: MainScreenAdapter? = null
    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null

    var myActivity: Activity? = null

    object Statified {
        var mMediaPlayer: MediaPlayer? = null
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bottomBar_setup()
        playPauseHelper = CurrentSongHelper()
        val prefs = activity?.getSharedPreferences("action_sort", MODE_PRIVATE)
        val action_sort_ascending = prefs?.getString("action_sort_ascending", "true")
        val action_sort_recent = prefs?.getString("action_sort_recent", "false")
        if (getSongsList != null) {
            if (action_sort_ascending!!.equals("true", ignoreCase = true)) {
                Collections.sort(getSongsList, Songs.Statified.nameComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            } else if (action_sort_recent!!.equals("true", ignoreCase = true)) {
                Collections.sort(getSongsList, Songs.Statified.dateComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.content_main, container, false)
        setHasOptionsMenu(true)
        activity?.title = "All songs"
        visibleLayout = (view?.findViewById(R.id.visibleLayout) as RelativeLayout)
        noSongs = view.findViewById(R.id.noSongs) as RelativeLayout
        nowPlayingBottomBar = view.findViewById(R.id.hiddenBarMainScreen) as RelativeLayout
        songTitle = view.findViewById(R.id.songTitleMainScreen) as TextView
        playPauseButton = view.findViewById(R.id.playpauseButton) as ImageButton
        (nowPlayingBottomBar as RelativeLayout).isClickable = false
        recyclerView = view.findViewById(R.id.contentMain) as RecyclerView


        playPauseHelper?.isPlaying = false
        //make the no songs layout visible
        if (getSongsList == null) {
            getSongsList = getSongsfromPhone()
            if (getSongsList == null) {
                visibleLayout?.visibility = View.INVISIBLE
                noSongs?.visibility = View.VISIBLE
            }
        } else {
            Log.d(MainScreenFragment::class.java.simpleName, " Data already there")
        }

        _mainScreenAdapter = MainScreenAdapter(getSongsList as ArrayList<Songs>, activity)
        val mLayoutManager = LinearLayoutManager(activity)
        (recyclerView as RecyclerView).layoutManager = mLayoutManager
        (recyclerView as RecyclerView).itemAnimator = DefaultItemAnimator()
        (recyclerView as RecyclerView).adapter = _mainScreenAdapter
        return view

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainScreen", " onCreate")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainScreen", " onResume")
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu!!.clear()
        inflater!!.inflate(R.menu.main, menu)
        return
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val switcher = item?.itemId
        if (switcher == R.id.action_sort_ascending) {
            val editor = myActivity?.getSharedPreferences("action_sort", MODE_PRIVATE)?.edit()
            editor?.putString("action_sort_ascending", "true")
            editor?.putString("action_sort_recent", "false")
            editor?.apply()
            if (getSongsList != null) {
                Collections.sort(getSongsList, Songs.Statified.nameComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        } else if (switcher == action_sort_recent) {
            if (getSongsList != null) {
                Collections.sort(getSongsList, Songs.Statified.dateComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            val editortwo = myActivity?.getSharedPreferences("action_sort", MODE_PRIVATE)?.edit()
            editortwo?.putString("action_sort_recent", "true")
            editortwo?.putString("action_sort_ascending", "false")
            editortwo?.apply()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getSongsfromPhone(): ArrayList<Songs> {
        val arrayList = ArrayList<Songs>()
        val contentResolver = activity?.contentResolver
        val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val songCursor = contentResolver?.query(songUri, null, null, null, null)
        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songPathInt = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while (songCursor.moveToNext()) {
                val currentId = songCursor.getLong(songId)
                val currentTitle = songCursor.getString(songTitle)
                val currentArtist = songCursor.getString(songArtist)
                val fullpath = songCursor.getString(songPathInt)
                val dateadded = songCursor.getLong(dateIndex)
                arrayList.add(Songs(currentId, currentTitle, currentArtist, fullpath, dateadded))
            }
        }
        return arrayList
    }

    fun bottomBar_setup() {
        nowPlayingBottomBar?.isClickable = false
        bottomBarClickHandlers()
        try {
            songTitle?.text = SongPlayingFragment.Statified.currentTrackHelper
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener({
                SongPlayingFragment.Staticated.on_song_complete()
                songTitle?.text = SongPlayingFragment.Statified.currentTrackHelper
            })
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {

                playPauseHelper?.isPlaying = true
                nowPlayingBottomBar?.visibility = View.VISIBLE
                nowPlayingBottomBar?.layoutParams?.height = RecyclerView.LayoutParams.WRAP_CONTENT
                nowPlayingBottomBar?.setPadding(0, 11, 0, 11)
                nowPlayingBottomBar?.requestLayout()
            } else {
                playPauseHelper?.isPlaying = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    fun bottomBarClickHandlers() {

        nowPlayingBottomBar?.setOnClickListener({


            try {
                mMediaPlayer = SongPlayingFragment.Statified.mediaPlayer
                val songPlayingFragment = SongPlayingFragment()
                val _fetch_from_Songs_Fragment = SongPlayingFragment.Statified.fetchSongs
                val args = Bundle()

                args.putString("BottomBar", "true")
                args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper.songTitle)
                args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper.songArtist)
                args.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper.currentPosition)
                args.putInt("SongId", SongPlayingFragment.Statified.currentSongHelper.songId.toInt())
                args.putParcelableArrayList("songsData", _fetch_from_Songs_Fragment)
                songPlayingFragment.arguments = args
                fragmentManager?.beginTransaction()
                        ?.replace(R.id.details_fragment, songPlayingFragment)
                        ?.addToBackStack("MainScreenFragment")
                        ?.commit()
            } catch (e: Exception) {
                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        })

        playPauseButton?.setOnClickListener({
            if (playPauseHelper?.isPlaying as Boolean) {
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                playPauseHelper?.TrackPosition = SongPlayingFragment.Statified.mediaPlayer?.getCurrentPosition()!!
                playPauseHelper?.isPlaying = false
                playPauseButton?.setBackgroundResource(R.drawable.ic_play_button)
            } else {
                SongPlayingFragment.Statified.mediaPlayer?.seekTo((playPauseHelper as CurrentSongHelper).TrackPosition)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseHelper?.isPlaying = true
                playPauseButton?.setBackgroundResource(R.drawable.ic_pause_button)
            }
        })

    }


}