package com.lalagrass.soundgenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final static int volumeScaleBound = 2000;
    private final static double volumeScale = 0.9;
    private static volatile double volumeFactor = 0;
    private static volatile int noteFactor = -1;
    private final static int sampleRate = 44100;
    private double[] fastSin = new double[sampleRate];
    private final static int maxF = 1600;
    private Button bStart;
    private TextView tFreq;
    private SeekBar seekBar;
    private static volatile boolean isPlaying = false;
    private static volatile int _frequency = 220;
    private static volatile double incr = (double)220 / 44100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bStart = (Button) findViewById(R.id.buttonStart);
        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noteFactor == 1) {
                    bStart.setText("Start");
                    noteFactor = -1;
                } else if (noteFactor == -1) {
                    bStart.setText("Stop");
                    noteFactor = 1;
                }
            }
        });
        tFreq = (TextView) findViewById(R.id.textFreq);
        tFreq.setText(_frequency + "Hz");
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setProgress(_frequency);
        seekBar.setMax(maxF);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    _frequency = 1;
                } else {
                    _frequency = progress;
                }
                noteFactor = 1;
                tFreq.setText(_frequency + "Hz");
                incr = (double)_frequency / 44100;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        for (int i = 0 ; i < fastSin.length; i++) {
            fastSin[i] = Math.sin(Math.PI * i * 2/ fastSin.length);
        }
    }

    @Override
    protected void onPause() {
        isPlaying = false;
        noteFactor = -1;
        bStart.setText("Start");
        super.onPause();
    }

    @Override
    protected void onResume() {
        isPlaying = true;
        bStart.setText("Start");
        new Generator().execute();
        super.onResume();
    }

    private class Generator extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            AudioTrack audioTrack;
            short generatedSnd[];// = new byte[2 * numSamples];
            int _bufSize;
            _bufSize = AudioTrack.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            _bufSize *= 2;
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, _bufSize,
                    AudioTrack.MODE_STREAM);
            generatedSnd = new short[_bufSize];
            audioTrack.play();
            double offset = 0;
            while (isPlaying) {
                for (int i = 0; i < generatedSnd.length; i++) {
                    offset += incr;
                    if (offset > 1)
                        offset -= 1;
                    volumeFactor += noteFactor;
                    if (volumeFactor > volumeScaleBound)
                        volumeFactor = volumeScaleBound;
                    else if (volumeFactor < 0)
                        volumeFactor = 0;
                    generatedSnd[i] = (short) (volumeScale * volumeFactor / volumeScaleBound * Short.MAX_VALUE * Math.sin(2 * Math.PI * offset));
                }
                audioTrack.write(generatedSnd, 0, generatedSnd.length);
            }
            audioTrack.stop();
            audioTrack.release();
            return null;
        }
    }
}
