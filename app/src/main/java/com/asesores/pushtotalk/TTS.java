package com.asesores.pushtotalk;

import java.util.Locale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;



public class TTS extends Service implements TextToSpeech.OnInitListener, OnUtteranceCompletedListener {
    private TextToSpeech mTts;
    private String spokenText;


    @Override
    public void onStart(Intent intent, int startId) {
                // TODO Auto-generated method stub
                super.onStart(intent, startId);
                try{
                //Log.d("TTS", "OnStart");
                 spokenText=intent.getStringExtra("mensaje");
                 mTts = new TextToSpeech(this, this);
                }catch (Exception e) {
                                if (mTts != null) {
                            mTts.stop();
                            mTts.shutdown();
                        }
                               }
         // This is a good place to set spokenText
    }
    
    public void onInit(int status) {
                //Log.d("TTS", "ONInit");
                
        if (status == TextToSpeech.SUCCESS) {
                Locale loc = new Locale("es", "es_ES"); // "es" es para que pronuncie en espanol
            int result = mTts.setLanguage(loc);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d("TTS", "ENTRA EN EL SPEECH");
                AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                int amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, amStreamMusicMaxVol, 0);
                mTts.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    public void onUtteranceCompleted(String uttId) {
                //Log.d("TTS", "stopSelf");
        stopSelf();
    }

    @Override
    public void onDestroy() {
                //Log.d("TTS", "ONDESTROY");
        if (mTts != null) {
                
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
       return null;
    }
}
