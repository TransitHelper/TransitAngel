package com.transitangel.transitangel.Manager;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Created by vidhurvoora on 8/30/16.
 */
public class TTSManager implements TextToSpeech.OnInitListener {

    private static TTSManager sInstance;
    protected Context mApplicationContext;
    private TextToSpeech mTTS;
    private boolean isInitialized = false;
    private String pendingTextToSpeak;

    public static synchronized TTSManager getSharedInstance() {
        if ( sInstance == null ) {
            sInstance = new TTSManager();
        }
        return sInstance;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setupTTS(Context context) {
        mApplicationContext = context;
        mTTS = new TextToSpeech(mApplicationContext, this);
    }

    public void speak(String speech) {


        if ( mTTS != null) {
            if ( !isInitialized ) {
                pendingTextToSpeak = speech;
            }
            else {
                if (TransitManager.getSharedInstance().isAccessibilityEnabled()) {
                    mTTS.speak(speech, TextToSpeech.QUEUE_ADD, null);
                }

            }

        }

    }

    @Override
    public void onInit(int initStatus) {
        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if(mTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                mTTS.setLanguage(Locale.US);
                isInitialized = true;
                //this logic is if speak was called while initialize is in progress
                if ( pendingTextToSpeak != null ) {
                    speak(pendingTextToSpeak);
                    pendingTextToSpeak = null;
                }
        }
        else if (initStatus == TextToSpeech.ERROR) {

        }
    }
}
