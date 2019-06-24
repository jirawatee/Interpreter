package com.example.interpreter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import static com.example.interpreter.MainActivity.MY_DATA_CHECK_CODE;
import static com.example.interpreter.MainActivity.RESULT_SPEECH;
import static com.example.interpreter.MainActivity.mIndexOutput;
import static com.example.interpreter.MainActivity.mLanguages;
import static com.example.interpreter.MainActivity.mLocales;
import static com.example.interpreter.MainActivity.myTTS;

public class BaseActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
	@Override
	public void onInit(int initStatus) {
		String[] statusMsg = getResources().getStringArray(R.array.tts_init_msg);
		Locale locale = getOutputLocale();
		String msg = "";

		switch (initStatus) {
			case TextToSpeech.SUCCESS:
				if (myTTS.isLanguageAvailable(locale) == 1) {
					int result = myTTS.setLanguage(locale);
					if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
						msg = String.format(statusMsg[0], mLanguages[mIndexOutput]);
						myTTS.setLanguage(Locale.ENGLISH);
					} else {
						msg = String.format(statusMsg[1], mLanguages[mIndexOutput]);
					}
				} else {
					myTTS.setLanguage(Locale.ENGLISH);
					msg = String.format(statusMsg[0], mLanguages[mIndexOutput]);
				}
				break;
			case TextToSpeech.ERROR:
				msg = statusMsg[2];
				break;
			case TextToSpeech.ERROR_NETWORK:
				msg = statusMsg[3];
				break;
			case TextToSpeech.ERROR_NETWORK_TIMEOUT:
				msg = statusMsg[4];
				break;
			case TextToSpeech.ERROR_NOT_INSTALLED_YET:
				msg = statusMsg[5];
				break;
		}
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myTTS != null) {
			myTTS.stop();
			myTTS.shutdown();
		}
	}

	protected boolean isSpeechRecognitionActivityPresented(Activity callerActivity, String locale) {
		PackageManager pm = callerActivity.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			if (mLocales[2].equals(locale)) {
				// Chinese support only "zh"
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.split("-")[0]);
			} else {
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale);
			}
			try {
				startActivityForResult(intent, RESULT_SPEECH);
				return true;
			} catch (ActivityNotFoundException a) {
				a.printStackTrace();
				return false;
			}

		} else {
			return false;
		}
	}

	protected void initialTextToSpeech(int resultCode) {
		if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
			myTTS = new TextToSpeech(this, this);
		} else {
			Intent installTTSIntent = new Intent();
			installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
			startActivity(installTTSIntent);
		}
	}

	private Locale getOutputLocale() {
		switch (mLocales[mIndexOutput]) {
			case "en-US":
				return Locale.US;
			case "zh-CN":
				return Locale.CHINA;
			case "ja-JP":
				return Locale.JAPAN;
			case "ko-KR":
				return Locale.KOREA;
			default:
				return new Locale("th", "TH");
		}
	}

	protected void checkTTSCodeData() {
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
	}
}