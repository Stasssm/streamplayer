package stasssm.streamplayer.visualizer;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamlibrary.visualizer.VisualizerView;
import stasssm.streamlibrary.visualizer.renderer.BarGraphRenderer;
import stasssm.streamlibrary.visualizer.renderer.CircleBarRenderer;
import stasssm.streamlibrary.visualizer.renderer.CircleRenderer;
import stasssm.streamlibrary.visualizer.renderer.LineRenderer;
import stasssm.streamplayer.PlayerActivity;
import stasssm.streamplayer.R;

/**
 * Created by Stas on 16.04.2016.
 */
public class VisualizerFragment extends Fragment {


    private static final String TAG = VisualizerFragment.class.getName();

    @Bind(R.id.visualizer_view)
    VisualizerView visualizerView;


    public static void start(PlayerActivity coreActivity) {
        VisualizerFragment tagsFragment = new VisualizerFragment();
        coreActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_fragment_continer, tagsFragment, TAG)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_vizualization, container, false);
        ButterKnife.bind(this, view);
        initVisualization();
        return view;
    }

    private void initVisualization() {
        PlayerService playerService = PlayerService.getSharedService() ;
        if (playerService != null) {
             AudioTrack audioTrack =  playerService.getCurrentAudioTrack();
            if (audioTrack != null) {
                visualizerView.link(audioTrack);
                addCircleRenderer();
            }
        }

    }

    @OnClick(R.id.circle_animation)
    public void circle() {
        visualizerView.clearRenderers();
        addCircleRenderer();
    }

    @OnClick(R.id.circle_bar_animation)
    public void circleBar() {
        visualizerView.clearRenderers();
        addCircleBarRenderer();
    }

    @OnClick(R.id.graph_animation)
    public void circleGraph() {
        visualizerView.clearRenderers();
        addBarGraphRenderers();
    }

    @OnClick(R.id.line_animation)
    public void line() {
        visualizerView.clearRenderers();
        addLineRenderer();
    }


    private void addLineRenderer()
    {
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1f);
        linePaint.setAntiAlias(true);
        linePaint.setColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.c_900));

        Paint lineFlashPaint = new Paint();
        lineFlashPaint.setStrokeWidth(5f);
        lineFlashPaint.setAntiAlias(true);
        lineFlashPaint.setColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.c_900));
        LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, true);
        visualizerView.addRenderer(lineRenderer);
    }

    private void addBarGraphRenderers()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(50f);
        paint.setAntiAlias(true);
        paint.setColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.c_900));
        BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(16, paint, false);
        visualizerView.addRenderer(barGraphRendererBottom);
    }

    private void addCircleBarRenderer()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
        paint.setColor(ContextCompat.getColor(getActivity().getApplicationContext(),R.color.c_900));
        CircleBarRenderer circleBarRenderer = new CircleBarRenderer(paint, 32, true);
        visualizerView.addRenderer(circleBarRenderer);
    }

    private void addCircleRenderer()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(3f);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.c_900));
        CircleRenderer circleRenderer = new CircleRenderer(paint, true);
        visualizerView.addRenderer(circleRenderer);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


}
