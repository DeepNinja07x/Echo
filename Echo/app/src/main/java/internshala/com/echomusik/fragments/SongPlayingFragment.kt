package internshala.com.echomusik.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import internshala.com.echomusik.R
import internshala.com.echomusik.database.EchoDatabase
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.MY_PREFS_LOOP
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.MY_PREFS_NAME
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.MY_PREFS_SHUFFLE
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.UpdateSongTime
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.activity
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.audioV
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.currentPosition
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.currentSongHelper
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.endTimeText
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.fab
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.fastforwardImageButton
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.favouriteContent
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.fetchSongs
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.loopImageButton
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.mSensorManager
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.mediaPlayer
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.playpauseImageButton
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.rewindImageButton
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.seekbar
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.shuffleImageButton
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.songArtist
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.songTitle
import internshala.com.echomusik.fragments.SongPlayingFragment.Statified.startTimeText
import internshala.com.echomusik.models.Songs
import internshala.com.echomusik.utils.CurrentSongHelper
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by ADMIN on 6/20/2017.
 */
class SongPlayingFragment : Fragment() {

    object Statified {
        var MY_PREFS_NAME = "ShakeFeature"
        var MY_PREFS_SHUFFLE = "ShuffleSave"
        var MY_PREFS_LOOP = "LoopSave"
        var seekbar: SeekBar? = null
        var mediaPlayer: MediaPlayer? = null
        var fetchSongs: ArrayList<Songs>? = arrayListOf()
        var currentTrackHelper: String? = null
        var favouriteContent: EchoDatabase? = null
        var currentSongHelper = CurrentSongHelper()
        var currentPosition: Int = 0
        var fab: ImageButton? = null
        var mSensorManager: SensorManager? = null
        var audioV: AudioVisualization? = null
        var activity: Activity? = null
        var songArtist: TextView? = null
        var songTitle: TextView? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playpauseImageButton: ImageButton? = null
        var rewindImageButton: ImageButton? = null
        var fastforwardImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var shuffleImageButton: ImageButton? = null
        var UpdateSongTime = object : Runnable {
            override fun run() {
                val getCurrent = mediaPlayer?.getCurrentPosition()
                startTimeText!!.text = String.format("%d: %d",
                        TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong()!!),
                        TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong()!!) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong())))
                seekbar?.setProgress(getCurrent.toInt())

                Handler().postDelayed(this, 1000)
            }
        }

    }

    var glView: GLAudioVisualizationView? = null
    private var mAccel: Float = 0.toFloat()
    private var mAccelCurrent: Float = 0.toFloat() // current acceleration including gravity
    private var mAccelLast: Float = 0.toFloat() // last acceleration including gravity
    private var mSensorListener: SensorEventListener? = null


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        SongPlayingFragment.Statified.activity = context as Activity?
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        SongPlayingFragment.Statified.activity = activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_songplayingrightnow, container, false)
        activity?.title = "Now playing"
        currentSongHelper.isLoop = false
        currentSongHelper.isShuffle = false
        currentSongHelper.isPlaying = true
        seekbar = view?.findViewById(R.id.seekBar) as SeekBar
        startTimeText = view.findViewById(R.id.startTime) as TextView
        endTimeText = view.findViewById(R.id.endTime) as TextView
        playpauseImageButton = view.findViewById(R.id.playpauseButton) as ImageButton
        fastforwardImageButton = (view.findViewById(R.id.fastforwardButton) as ImageButton)
        rewindImageButton = (view.findViewById(R.id.rewindButton) as ImageButton)
        loopImageButton = (view.findViewById(R.id.loopButton) as ImageButton)
        shuffleImageButton = (view.findViewById(R.id.shuffleButton) as ImageButton)
        fab = view.findViewById(R.id.favoriteIcon) as ImageButton
        glView = view.findViewById(R.id.visualizer_view) as GLAudioVisualizationView
        songArtist = view.findViewById(R.id.songTitle) as TextView
        songTitle = view.findViewById(R.id.songArtist) as TextView
        fab?.setAlpha(0.8f)
        return view
    }

    override fun onResume() {
        super.onResume()
        audioV?.onResume()
        if (mediaPlayer?.isPlaying() as Boolean) {
            currentSongHelper.isPlaying = true
            playpauseImageButton?.setBackgroundResource(R.drawable.ic_pause_button)
        } else {
            currentSongHelper.isPlaying = false
            playpauseImageButton?.setBackgroundResource(R.drawable.ic_play_button)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mSensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorManager?.registerListener(mSensorListener,
                mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)
        mAccel = 0.00f
        mAccelCurrent = SensorManager.GRAVITY_EARTH
        mAccelLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()

    }

    override fun onPause() {
        super.onPause()
        audioV?.onPause()
        mSensorManager?.unregisterListener(mSensorListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioV?.release()
    }


    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_redirect -> {
                activity?.onBackPressed()
                return false
            }

        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        audioV = glView as AudioVisualization
        favouriteContent = EchoDatabase(activity)
        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long = 0
        try {
            path = arguments?.getString("path")
            currentPosition = arguments?.getInt("songPosition") as Int
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            fetchSongs = arguments?.getParcelableArrayList("songsData")
            songId = arguments?.getInt("SongId")?.toLong() as Long
            if (_songArtist.equals("<unknown>", true)) {
                _songArtist = "unknown"
            }
            currentSongHelper.songArtist = _songArtist
            currentSongHelper.songTitle = _songTitle
            currentSongHelper.songPath = path
            currentSongHelper.currentPosition = currentPosition
            currentSongHelper.songId = songId

        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (favouriteContent?.checkifIdExists(currentSongHelper.songId.toInt()) as Boolean) {
            fab?.setImageDrawable(ContextCompat.getDrawable(activity as Context, R.drawable.ic_heart))
        }
        songArtist?.text = currentSongHelper.songArtist
        songTitle?.text = currentSongHelper.songTitle
        SongPlayingFragment.Statified.currentTrackHelper = currentSongHelper.songTitle

        val fromBottomBar = arguments?.get("BottomBar") as? String
        val fromfavBottomBar = arguments?.get("FavBottomBar") as? String
        if (fromBottomBar != null) {
            mediaPlayer = MainScreenFragment.Statified.mMediaPlayer
        } else if (fromfavBottomBar != null) {
            mediaPlayer = FavouriteFragment.Statified.meediaPlayer
        } else {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                mediaPlayer?.setDataSource(activity, Uri.parse(path))
                mediaPlayer?.prepare()
            } catch (e: Exception) {
                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
            mediaPlayer?.start()
        }
        audioV?.linkTo(DbmHandler.Factory.newVisualizerHandler(activity as Context, mediaPlayer?.audioSessionId as Int))
        if (mediaPlayer?.isPlaying as Boolean) {
            playpauseImageButton?.setBackgroundResource(R.drawable.ic_pause_button)
        } else {
            playpauseImageButton?.setBackgroundResource(R.drawable.ic_play_button)
        }
        SongPlayingFragment.Staticated.processInformation(mediaPlayer as MediaPlayer)
        clickHandler()

        mediaPlayer?.setOnCompletionListener {
            SongPlayingFragment.Staticated.on_song_complete()
        }
        var prefs = activity?.getSharedPreferences(MY_PREFS_SHUFFLE, MODE_PRIVATE)
        var isShuffleAllowed = prefs?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            currentSongHelper.isShuffle = true
            currentSongHelper.isLoop = false
            shuffleImageButton?.setBackgroundResource(R.drawable.ic_shuffle_yellow)
            loopImageButton?.setBackgroundResource(R.drawable.ic_loop_white)
        } else {
            shuffleImageButton?.setBackgroundResource(R.drawable.ic_shuffle_white)
            currentSongHelper.isShuffle = false
        }
        var prefsforLoop = activity?.getSharedPreferences(MY_PREFS_LOOP, MODE_PRIVATE)
        var isLoopAllowed = prefsforLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            currentSongHelper.isLoop = true
            currentSongHelper.isShuffle = false
            loopImageButton?.setBackgroundResource(R.drawable.ic_loop_yellow)
            shuffleImageButton?.setBackgroundResource(R.drawable.ic_shuffle_white)

        } else {
            currentSongHelper.isLoop = false
            loopImageButton?.setBackgroundResource(R.drawable.ic_loop_white)
        }

    }

    object Staticated {

        fun on_song_complete() {
            if (!(currentSongHelper.isShuffle)) {
                if (currentSongHelper.isLoop) {
                    currentSongHelper.isPlaying = true
                    var nextSong = fetchSongs?.get(currentPosition)
                    SongPlayingFragment.Statified.currentTrackHelper = nextSong?.songTitle

                    if (nextSong?.artist.equals("<unknown>", true)) {
                        currentSongHelper.songArtist = "unknown"
                    } else {
                        currentSongHelper.songArtist = nextSong?.artist
                    }

                    currentSongHelper.songTitle = nextSong?.songTitle
                    currentSongHelper.songPath = nextSong?.songData
                    currentSongHelper.currentPosition = currentPosition
                    currentSongHelper.songId = nextSong?.songID as Long
                    if (favouriteContent?.checkifIdExists(currentSongHelper.songId.toInt()) as Boolean) {
                        fab?.setImageDrawable(ContextCompat.getDrawable(activity as Context, R.drawable.ic_heart))
                    }
                    mediaPlayer?.reset()
                    try {
                        mediaPlayer?.setDataSource(activity, Uri.parse(nextSong.songData))
                        mediaPlayer?.prepare()
                        mediaPlayer?.start()
                        songArtist?.text = nextSong.artist
                        songTitle?.text = nextSong.songTitle
                        processInformation(mediaPlayer as MediaPlayer)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    currentSongHelper.isPlaying = true
                    SongPlayingFragment.Staticated.playNext("PlayNextNormal")
                }
            } else {
                currentSongHelper.isPlaying = true
                playNext("playNextLikeNormalShuffle")
            }
        }

        fun playNext(check: String) {
            if (!(currentSongHelper.isPlaying as Boolean)) {
                playpauseImageButton?.setBackgroundResource(R.drawable.ic_play_button)
            } else {
                playpauseImageButton?.setBackgroundResource(R.drawable.ic_pause_button)
            }
            if (check.equals("PlayNextNormal", true)) {

                currentPosition += 1
                if (currentPosition == fetchSongs?.size) {
                    currentPosition = 0
                }

            } else if (check.equals("playNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var Randomposition = randomObject?.nextInt((fetchSongs?.size)?.plus(1) as Int)
                currentSongHelper.isLoop = false
                currentPosition = Randomposition
                if (currentPosition == fetchSongs?.size) {
                    currentPosition = 0
                }
            }
            var nextSong = fetchSongs?.get(currentPosition)
            SongPlayingFragment.Statified.currentTrackHelper = nextSong?.songTitle
            if (nextSong?.artist.equals("<unknown>", true)) {
                currentSongHelper.songArtist = "unknown"
            } else {
                currentSongHelper.songArtist = nextSong?.artist
            }
            currentSongHelper.songTitle = nextSong?.songTitle
            currentSongHelper.songPath = nextSong?.songData
            currentSongHelper.currentPosition = currentPosition
            currentSongHelper.songId = nextSong?.songID as Long

            try {
                if (favouriteContent?.checkifIdExists(currentSongHelper.songId.toInt()) as Boolean) {
                    fab?.setImageDrawable(ContextCompat.getDrawable(activity as Activity, R.drawable.ic_heart))
                } else {
                    fab?.setImageDrawable(ContextCompat.getDrawable(activity as Activity, R.drawable.ic_heart_outline))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mediaPlayer?.reset()
            try {
                mediaPlayer?.setDataSource(activity, Uri.parse(currentSongHelper.songPath))
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                if (nextSong.artist.equals("<unknown>", true)) {
                    songArtist?.text = "unknown"
                } else {
                    songArtist?.text = nextSong.artist
                }
                songTitle?.text = nextSong.songTitle
                processInformation(mediaPlayer as MediaPlayer)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            seekbar?.setMax(finalTime)
            startTimeText?.setText(String.format("%d: %d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())))
            )
            endTimeText?.setText(String.format("%d: %d",
                    TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))
            )
            seekbar?.setProgress(startTime)
            Handler().postDelayed(UpdateSongTime, 1000)
        }
    }


    fun bindShakeListener() {
        mSensorListener = object : SensorEventListener {
            override fun onSensorChanged(se: SensorEvent) {
                val x = se.values[0]
                val y = se.values[1]
                val z = se.values[2]
                mAccelLast = mAccelCurrent
                mAccelCurrent = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = mAccelCurrent - mAccelLast
                mAccel = mAccel * 0.9f + delta // perform low-cut filter

                if (mAccel > 12) {
                    val prefs = activity?.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)

                    if (isAllowed as Boolean) {

                        SongPlayingFragment.Staticated.playNext("PlayNextNormal")
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }


    private fun clickHandler() {

        fab?.setOnClickListener({

            if (favouriteContent?.checkifIdExists(currentSongHelper.songId.toInt()) as Boolean) {
                favouriteContent?.deleteFavourite(currentSongHelper.songId.toInt())
                Toast.makeText(Statified.activity, "Removed from favorites", Toast.LENGTH_SHORT).show()
                fab?.setImageDrawable(ContextCompat.getDrawable(activity as Context, R.drawable.ic_heart_outline))

            } else {
                Toast.makeText(Statified.activity, "Added to favorites", Toast.LENGTH_SHORT).show()
                favouriteContent?.storeasFavourite(currentSongHelper.songId.toInt(), currentSongHelper.songArtist,
                        currentSongHelper.songTitle, currentSongHelper.songPath)
                fab?.setImageDrawable(ContextCompat.getDrawable(activity as Context, R.drawable.ic_heart))

            }
        })
        shuffleImageButton?.setOnClickListener({

            val editorShuffle = Statified.activity?.getSharedPreferences(MY_PREFS_SHUFFLE, MODE_PRIVATE)?.edit()
            val editorLoop = Statified.activity?.getSharedPreferences(MY_PREFS_LOOP, MODE_PRIVATE)?.edit()
            if (currentSongHelper.isShuffle) {
                shuffleImageButton?.setBackgroundResource(R.drawable.ic_shuffle_white)
                currentSongHelper.isShuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                currentSongHelper.isShuffle = true
                currentSongHelper.isLoop = false
                shuffleImageButton?.setBackgroundResource(R.drawable.ic_shuffle_yellow)
                loopImageButton?.setBackgroundResource(R.drawable.ic_loop_white)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }

        })
        fastforwardImageButton?.setOnClickListener({
            currentSongHelper.isPlaying = true
            if (currentSongHelper.isShuffle) {
                SongPlayingFragment.Staticated.playNext("playNextLikeNormalShuffle")
            } else {
                SongPlayingFragment.Staticated.playNext("PlayNextNormal")
            }
        })
        rewindImageButton?.setOnClickListener({
            currentSongHelper.isPlaying = true
            if (currentSongHelper.isLoop) {
                loopImageButton?.setBackgroundResource(R.drawable.ic_loop_white)
            }
            currentSongHelper.isLoop = false
            playPrevious()
        })
        loopImageButton?.setOnClickListener({

            val editorLoop = Statified.activity?.getSharedPreferences(MY_PREFS_LOOP, MODE_PRIVATE)?.edit()
            val editorShuffle = Statified.activity?.getSharedPreferences(MY_PREFS_SHUFFLE, MODE_PRIVATE)?.edit()
            if (currentSongHelper.isLoop) {
                currentSongHelper.isLoop = false
                loopImageButton?.setBackgroundResource(R.drawable.ic_loop_white)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                currentSongHelper.isLoop = true
                currentSongHelper.isShuffle = false
                loopImageButton?.setBackgroundResource(R.drawable.ic_loop_yellow)
                shuffleImageButton?.setBackgroundResource(R.drawable.ic_shuffle_white)
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            }

        })
        playpauseImageButton?.setOnClickListener {
            if (mediaPlayer?.isPlaying as Boolean) {
                currentSongHelper.isPlaying = true
                playpauseImageButton?.setBackgroundResource(R.drawable.ic_play_button)
                mediaPlayer?.pause()
            } else {
                currentSongHelper.isPlaying = false
                playpauseImageButton?.setBackgroundResource(R.drawable.ic_pause_button)
                mediaPlayer?.seekTo(seekbar?.progress as Int)
                mediaPlayer?.start()
            }
        }

        seekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBarget: SeekBar?) {
                seekbar?.setProgress(seekbar?.getProgress() as Int)
                mediaPlayer?.seekTo(seekbar?.getProgress() as Int)
            }
        })


    }

    private fun playPrevious() {
        currentPosition = currentPosition - 1
        if (currentPosition == -1) {
            currentPosition = 0
        }
        if (currentSongHelper.isPlaying as Boolean) {
            playpauseImageButton?.setBackgroundResource(R.drawable.ic_pause_button)
        } else {
            playpauseImageButton?.setBackgroundResource(R.drawable.ic_play_button)
        }
        val nextSong = fetchSongs?.get(currentPosition)

        currentSongHelper.songTitle = nextSong?.songTitle
        currentSongHelper.songPath = nextSong?.songData

        currentSongHelper.songId = nextSong?.songID as Long

        currentSongHelper.currentPosition = currentPosition
        SongPlayingFragment.Statified.currentTrackHelper = currentSongHelper.songTitle

        if (nextSong?.artist.equals("<unknown>", true)) {
            currentSongHelper.songArtist = "unknown"
        } else {
            currentSongHelper.songArtist = nextSong?.artist
        }
        try {
            if (favouriteContent?.checkifIdExists(currentSongHelper.songId.toInt()) as Boolean) {
                fab?.setImageDrawable(ContextCompat.getDrawable(activity as Context, R.drawable.ic_heart))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer?.reset()
        try {
            mediaPlayer?.setDataSource(activity, Uri.parse(nextSong.songData))
            mediaPlayer?.prepare()
            mediaPlayer?.start()

            songArtist?.setText(nextSong.artist)
            songTitle?.setText(nextSong.songTitle)
            SongPlayingFragment.Staticated.processInformation(mediaPlayer as MediaPlayer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}