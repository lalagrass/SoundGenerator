package com.lalagrass.soundgenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private int sampleRate = 44100;
    private final static int maxF = 4000;
    private double[] tmpData = new double[maxF * 4];
    private Button bStart;
    private TextView tFreq;
    private SeekBar seekBar;
    private static volatile boolean isPlaying = false;
    private static volatile int _frequency = 220;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bStart = (Button) findViewById(R.id.buttonStart);
        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    bStart.setText("Start");
                } else {
                    bStart.setText("Stop");
                }
                isPlaying = !isPlaying;
            }
        });
        tFreq = (TextView) findViewById(R.id.textFreq);
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
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        for (int i  = 0; i < tmpData.length; i++) {
            tmpData[i] = Math.sin(Math.PI * i * 2 / tmpData.length);
        }
    }

    @Override
    protected void onPause() {
        isPlaying = false;
        bStart.setText("Start");
        super.onPause();
    }

    @Override
    protected void onResume() {
        isPlaying = false;
        bStart.setText("Start");
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
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, _bufSize,
                    AudioTrack.MODE_STREAM);
            generatedSnd = new short[_bufSize];
            audioTrack.play();
            int angle0 = 0;
            double incr0 = 0;
            boolean first = true;
            while (isPlaying) {
                int freqOfTone = _frequency;
                incr0 = freqOfTone * _data.length / sampleRate;
                for (int i = 0; i < generatedSnd.length; i++) {
                    if (first && i < 200) {
                        generatedSnd[i] = (short) (_data[(int) angle0] * (i / 200));
                    }else {
                        generatedSnd[i] = _data[(int) angle0];
                    }
                    angle0 += incr0;
                    if (angle0 >= _data.length)
                        angle0 -= _data.length;
                }
                audioTrack.write(generatedSnd, 0, generatedSnd.length);
            }
            audioTrack.stop();
            audioTrack.release();
            return null;
        }
    }
}
