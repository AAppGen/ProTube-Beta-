package io.awesome.gagtube.download.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import io.awesome.gagtube.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.Locale;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class Utility {
	
	public enum FileType {
		VIDEO,
		MUSIC,
		SUBTITLE,
		UNKNOWN
	}
	
	public static String formatBytes(long bytes) {
		Locale locale = Locale.getDefault();
		if (bytes < 1024) {
			return String.format(locale, "%d B", bytes);
		}
		else if (bytes < 1024 * 1024) {
			return String.format(locale, "%.2f kB", bytes / 1024d);
		}
		else if (bytes < 1024 * 1024 * 1024) {
			return String.format(locale, "%.2f MB", bytes / 1024d / 1024d);
		}
		else {
			return String.format(locale, "%.2f GB", bytes / 1024d / 1024d / 1024d);
		}
	}
	
	public static String formatSpeed(double speed) {
		Locale locale = Locale.getDefault();
		if (speed < 1024) {
			return String.format(locale, "%.2f B/s", speed);
		}
		else if (speed < 1024 * 1024) {
			return String.format(locale, "%.2f kB/s", speed / 1024);
		}
		else if (speed < 1024 * 1024 * 1024) {
			return String.format(locale, "%.2f MB/s", speed / 1024 / 1024);
		}
		else {
			return String.format(locale, "%.2f GB/s", speed / 1024 / 1024 / 1024);
		}
	}
	
	public static void writeToFile(@NonNull File file, @NonNull Serializable serializable) {
		
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			objectOutputStream.writeObject(serializable);
		}
		catch (Exception e) {
			//nothing to do
		}
		//nothing to do
	}
	
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T readFromFile(File file) {
		T object;
		ObjectInputStream objectInputStream = null;
		
		try {
			objectInputStream = new ObjectInputStream(new FileInputStream(file));
			object = (T) objectInputStream.readObject();
		}
		catch (Exception e) {
			Log.e("Utility", "Failed to deserialize the object", e);
			object = null;
		}
		
		if (objectInputStream != null) {
			try {
				objectInputStream.close();
			}
			catch (Exception e) {
				//nothing to do
			}
		}
		
		return object;
	}
	
	@Nullable
	public static String getFileExt(String url) {
		int index;
		if ((index = url.indexOf("?")) > -1) {
			url = url.substring(0, index);
		}
		
		index = url.lastIndexOf(".");
		if (index == -1) {
			return null;
		}
		else {
			String ext = url.substring(index);
			if ((index = ext.indexOf("%")) > -1) {
				ext = ext.substring(0, index);
			}
			if ((index = ext.indexOf("/")) > -1) {
				ext = ext.substring(0, index);
			}
			return ext.toLowerCase();
		}
	}
	
	public static FileType getFileType(char kind, String file) {
		switch (kind) {
			case 'v':
				return FileType.VIDEO;
			case 'a':
				return FileType.MUSIC;
			case 's':
				return FileType.SUBTITLE;
			//default '?':
		}
		
		if (file.endsWith(".srt") || file.endsWith(".vtt") || file.endsWith(".ssa")) {
			return FileType.SUBTITLE;
		}
		else if (file.endsWith(".mp3") || file.endsWith(".wav") || file.endsWith(".flac") || file.endsWith(".m4a") || file.endsWith(".opus")) {
			return FileType.MUSIC;
		}
		else if (file.endsWith(".mp4") || file.endsWith(".mpeg") || file.endsWith(".rm") || file.endsWith(".rmvb")
				|| file.endsWith(".flv") || file.endsWith(".webp") || file.endsWith(".webm")) {
			return FileType.VIDEO;
		}
		
		return FileType.UNKNOWN;
	}
	
	@ColorInt
	public static int getBackgroundForFileType(Context ctx, FileType type) {
		int colorRes = R.color.light_blue;
		/*switch (type) {
			case MUSIC:
				colorRes = R.color.audio_left_to_load_color;
				break;
			case VIDEO:
				colorRes = R.color.video_left_to_load_color;
				break;
			default:
				colorRes = R.color.youtube_primary_color;
		}*/
		
		return ContextCompat.getColor(ctx, colorRes);
	}
	
	@ColorInt
	public static int getForegroundForFileType(Context ctx, FileType type) {
		int colorRes = R.color.youtube_primary_color;
		/*switch (type) {
			case MUSIC:
				colorRes = R.color.audio_already_load_color;
				break;
			case VIDEO:
				colorRes = R.color.video_already_load_color;
				break;
			default:
				colorRes = R.color.gray;
				break;
		}*/
		
		return ContextCompat.getColor(ctx, colorRes);
	}
	
	@DrawableRes
	public static int getIconForFileType(FileType type) {
		switch (type) {
			case MUSIC:
				return R.drawable.ic_music_download;
			default:
			case VIDEO:
				return R.drawable.ic_video_download;
		}
	}
	
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static boolean mkdir(File p, boolean allDirs) {
		if (p.exists()) return true;
		
		if (allDirs)
			p.mkdirs();
		else
			p.mkdir();
		
		return p.exists();
	}
	
	public static long getContentLength(HttpURLConnection connection) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return connection.getContentLengthLong();
		}
		
		try {
			return Long.parseLong(connection.getHeaderField("Content-Length"));
		}
		catch (Exception err) {
			// nothing to do
		}
		
		return -1;
	}
	
	private static String pad(int number) {
		return number < 10 ? ("0" + number) : String.valueOf(number);
	}
	
	public static String stringifySeconds(double seconds) {
		int h = (int) Math.floor(seconds / 3600);
		int m = (int) Math.floor((seconds - (h * 3600)) / 60);
		int s = (int) (seconds - (h * 3600) - (m * 60));
		
		String str = "";
		
		if (h < 1 && m < 1) {
			str = "00:";
		}
		else {
			if (h > 0) str = pad(h) + ":";
			if (m > 0) str += pad(m) + ":";
		}
		
		return str + pad(s);
	}
}
