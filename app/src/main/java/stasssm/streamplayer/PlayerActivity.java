package stasssm.streamplayer;

import android.content.Intent;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.triggertrap.seekarc.SeekArc;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stasssm.streamlibrary.audiowidget.AudioWidget;
import stasssm.streamlibrary.helpers.PlayerHelperActivity;
import stasssm.streamlibrary.main.FFmpegMediaPlayer;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamlibrary.model.StreamSong;
import stasssm.streamlibrary.playercore.PlayerController;
import stasssm.streamlibrary.tagging.FileTagger;
import stasssm.streamplayer.drawer.NawAdapter;
import stasssm.streamplayer.equalizer.EqualizerFragment;
import stasssm.streamplayer.mediaStore.StoreFragment;
import stasssm.streamplayer.settings.SettingsFragment;
import stasssm.streamplayer.storage.StorageFragment;
import stasssm.streamplayer.streamUrl.UrlFragment;
import stasssm.streamplayer.tags.TagsFragment;
import stasssm.streamplayer.visualizer.VisualizerFragment;


public class PlayerActivity extends PlayerHelperActivity {


    @Bind(R.id.play_pause_btn)
    ImageButton playPauseBtn;
    @Bind(R.id.player_prev)
    ImageButton playerPrev;
    @Bind(R.id.seekArc)
    SeekArc seekArc;
    @Bind(R.id.player_next)
    ImageButton playerNext;
    @Bind(R.id.choose_songs)
    Button chooseSongs;
    @Bind(R.id.left_drawer)
    RecyclerView leftDrawerRecycler;
    @Bind(R.id.player_fragment_continer)
    FrameLayout playerFragmentContainer;

    PlayerService playerService;
    DrawerLayout drawerLayout;
    PlayerController playerController ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_player);
        ButterKnife.bind(this);
        playerController = PlayerController.getInstance() ;
        runPlayer();
        initSeekBar();
        initDrawer();
        initRecycler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playerController.attachListener(this);
        changePlayBtn(playerService.isPlaying());
    }


    @Override
    protected void onPause() {
        super.onPause();
        playerController.detachListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerController = null ;
    }

    private void initRecycler() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        leftDrawerRecycler.setLayoutManager(linearLayoutManager);
        NawAdapter nawAdapter = new NawAdapter(this);
        leftDrawerRecycler.setAdapter(nawAdapter);
    }

    public void checkVersionWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1234);
        } else {
            setupWidget();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
               setupWidget();
            }
        }
    }

    public void setupWidget() {
        AudioWidget audioWidget = new AudioWidget.Builder(getApplicationContext())
                .additionalDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.volume_logo))
                .playDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play_widget))
                .nextTrackDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.next_widget))
                .prevTrackDrawale(ContextCompat.getDrawable(getApplicationContext(),R.drawable.prev_widget))
                .build();
        audioWidget.show(100,100);
    }


    private void initDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        )

        {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
                syncState();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                syncState();
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBarDrawerToggle.syncState();
    }


    private void runPlayer() {
        playerService = PlayerService.getSharedService();
        if (playerService != null) {
            List<StreamSong> songs = Song.generateSongs() ;
            playerService.setCatalogSongs(songs);
            playerService.selectSong(0);
        }
    }

    @OnClick(R.id.play_pause_btn)
    public void clickPlay() {
        playPauseBtn.setImageResource(playerService.togglePlayPause()? R.drawable.pause_black
                : R.drawable.play_black);
    }

    private void changePlayBtn(boolean isPlaying) {
        playPauseBtn.setImageResource(isPlaying ? R.drawable.pause_black : R.drawable.play_black);
    }


    private void initSeekBar() {
        seekArc.setTouchInSide(false);
        seekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
                stopProgressBar();
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                if (playerService.getMp() != null) {
                    int totalDuration = playerService.getMp().getDuration();
                    int currentPosition = utils.progressToTimer(seekArc.getProgress(), totalDuration);
                    playerService.seekTo(currentPosition);
                    updateProgressBar();
                }
            }
        });
    }

    @Override
    public void onSongChanged(StreamSong item, int position) {
        super.onSongChanged(item, position);
        //FileTagger.getAllTags(item);
    }

    @Override
    public void onTotalDurationChanged(String duration) {
        //   mPlayerTotalTime.setText("Total duration : " + duration);
    }

    @Override
    public void onPlayingTimeChanged(String currentTime) {
        // mPlayerCurrentTime.setText("CurrentTime : " + currentTime);
    }

    @Override
    protected void updateSeekProgress(int progress) {
        Log.d("PlayerTag", progress + "");
        seekArc.setProgress(progress);
    }

    @Override
    public void onStateChanged(boolean isPlaying) {
        super.onStateChanged(isPlaying);
        changePlayBtn(isPlaying);
        if (isPlaying) {
            //       mPlayerState.setText("State : " + "Playing");
        } else {
            //       mPlayerState.setText("State : " + "Paused");
        }
    }

    @OnClick(R.id.player_prev)
    public void prevButton() {
        playerService.playPrevious();
    }

    @OnClick(R.id.player_next)
    public void nextButton() {
        playerService.playPrevious();
    }

    @OnClick(R.id.choose_songs)
    public void chooseSongs() {
        StoreFragment fragment = new StoreFragment() ;
        fragment.show(getFragmentManager(), StoreFragment.TAG);
    }

    @OnClick(R.id.choose_url)
    public void chooseUrl() {
        UrlFragment fragment = new UrlFragment() ;
        fragment.show(getFragmentManager(),UrlFragment.TAG);
    }

    @OnClick(R.id.create_widget)
    public void createWidget() {
        checkVersionWidget();
    }


    // @Override
    public void onBufferingUpdate(FFmpegMediaPlayer mp, int percent) {
        super.onBufferingUpdate(mp, percent);
        //   mPlayerBuffPercent.setText("Buffered percent : " + percent);
    }

    public void clickMenu(int position) {
        drawerLayout.closeDrawer(Gravity.LEFT);
        switch (position) {
            case 0: {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playerFragmentContainer.setVisibility(View.INVISIBLE);
                    }
                }, 700);
                break;
            }
            case 1: {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playerFragmentContainer.setVisibility(View.VISIBLE);
                        TagsFragment.start(PlayerActivity.this);
                    }
                }, 700);
                break;
            }
            case 2: {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playerFragmentContainer.setVisibility(View.VISIBLE);
                        StorageFragment.start(PlayerActivity.this);
                    }
                }, 700);
                break;
            }
            case 3: {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playerFragmentContainer.setVisibility(View.VISIBLE);
                        EqualizerFragment.start(PlayerActivity.this);

                    }
                }, 700);
                break;
            }
            case 4: {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playerFragmentContainer.setVisibility(View.VISIBLE);
                        VisualizerFragment.start(PlayerActivity.this);

                    }
                }, 700);
                break;
            }
            case 5 : {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playerFragmentContainer.setVisibility(View.VISIBLE);
                        SettingsFragment.start(PlayerActivity.this);

                    }
                }, 700);
                break;
            }
        }

    }



}
