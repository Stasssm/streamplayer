package stasssm.streamlibrary.main;

import android.net.Uri;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import stasssm.streamlibrary.PlayerInitializer;
import stasssm.streamlibrary.cashefolder.StorageUtil;
import stasssm.streamlibrary.model.StreamSong;
import stasssm.streamlibrary.utils.Connectivity;


public class TTAudioFileStream {
	public static enum DownloadState {
		Initial, Downloading, Downloaded, Error, TemporaryError, Aborted;

		public boolean isTerminated() {
			return this == Downloaded || this == Error || this == TemporaryError || this == Aborted;
		}
	}

	public interface AudioFileStreamChangeListener {
		void onStateChange(TTAudioFileStream audioFileStream);
	}

	public static class AudioFileStreamState {
		public File file;
		public long totalSize;
		public long downloadedSize;
		public DownloadState downloadState;
	}

    private static final int MAX_REPEAT_COUNT = 15 ;
    private static final String cacheDirPath = StorageUtil.getStorage().getCasheDir() ;

	//private String songId = null;
	private StreamSong song = null;
	private final Object stateSync = new Object();
	private Object filenameSync = new Object();
	private DownloadState downloadState = DownloadState.Initial;
	private long totalSize = -1, downloadedSize = -1;
	private File file = null;
	private RandomAccessFile raf = null;
	private boolean stopRequested = false;
    private boolean isCashedSongPlaying = false ;
    private boolean isOfflinePlaying = false ;

    private int restartCount =  0 ;

	AudioFileStreamChangeListener listener;

	public long getDownloadedSize() {
		return downloadedSize;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public String getSongId() {
		if (song == null)
			return null;
		return song.getUniqueIdentifier();
	}

	/*public String getSongTitle() {
		if (song == null)
			return null;
		return song.getTitle() + " - " + song.getArtist();
	}*/

	public void setChangeListener(AudioFileStreamChangeListener listener) {
		this.listener = listener;
	}

	public DownloadState getState() {
		synchronized (stateSync) {
			return downloadState;
		}
	}

	public void fillAudioStreamState(AudioFileStreamState state) {
		synchronized (stateSync) {
			state.file = file;
			state.totalSize = totalSize;
			state.downloadedSize = downloadedSize;
			state.downloadState = downloadState;
		}
	}

	protected void setState(DownloadState state, long totalSize, long downloadedSize) {
		synchronized (stateSync) {
			this.downloadState = state;
			this.totalSize = totalSize;
			this.downloadedSize = downloadedSize;
			stateSync.notifyAll();
		}
		if (listener != null) {
			listener.onStateChange(this);
		}
	}

	public TTAudioFileStream(StreamSong song) {
		this.song = song;
		setState(DownloadState.Initial, -1, -1);
	}

	public interface AudioFileStreamStatePredicate {
		boolean satisfies(TTAudioFileStream stream);
	}

	public void waitForState(AudioFileStreamStatePredicate predicate) {
		synchronized (stateSync) {
			while (!predicate.satisfies(this)) {
				try {
					stateSync.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public interface AudioFileStreamFilenameAcceptor<T> {
		T process(File filename);
	}

	public <T> T getAudioFileName(AudioFileStreamFilenameAcceptor<T> filenameAcceptor) {
        return filenameAcceptor.process(file);
    }

    public void requestStop() {
		synchronized (stateSync) {
			stopRequested = true;
		}
	}

    private String url ;

    public void setDataSource(String url) {
       this.url = url ;
    }

    public void setDataSource(Uri url) {
        this.url = url.getPath() ;
    }

    public String getUrl() {
        return url;
    }

    //TODO
	public void start() {
        if (restartCount == 0 ) {
            String fileUri = song.isOfflineExists() ;
            if (fileUri != null) {
                if (song.isOffline()) {
                    isOfflinePlaying = true;
                }
                url = fileUri ;
                File fileOffline = new File(fileUri) ;
                file = fileOffline;
                long fileLength = fileOffline.length();
                setState(DownloadState.Downloaded, fileLength, fileLength);
                return;
            }


            if (url != null &&  !url.contains("http")) {
                isCashedSongPlaying = true;
                File totalFile = new File(cacheDirPath,url);
                if (totalFile.exists()) {
                    file = totalFile;
                    long fileLength = totalFile.length();
                    setState(DownloadState.Downloaded, fileLength, fileLength);
                    return;
                } else {
                    Log.d("TotalError", "totalFile.exists()");
                    setState(DownloadState.Error, -1, -1);
                    return;
                }
            }
        }
        //TODO

		File cacheDir = new File(cacheDirPath);
		if (!cacheDir.exists()) {
			if (!cacheDir.mkdirs()) {
                Log.d("TotalError","cacheDir.mkdirs()");
				setState(DownloadState.Error, -1, -1);
				return;
			}
		}

		cacheDir = new File(cacheDirPath);

		if (!cacheDir.exists()) {
            Log.d("TotalError","cacheDir.exists()");
			setState(DownloadState.Error, -1, -1);
			return;
		}

        file = new File(cacheDirPath, "filen" + this.getSongId()+".mp3");

        if (!file.exists()) {
            try {
               boolean usCreated =  file.createNewFile() ;
               Log.d("CreationOfFile", usCreated+"") ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

		downloadedSize = file.length();

		try {
			raf = new RandomAccessFile(file, "rw");
			raf.seek(downloadedSize);
		} catch (IOException e) {
			e.printStackTrace();
            Log.d("TotalError","IOException");
			//setState(DownloadState.Error, -1, -1);
            //TODO check it
            start();
			return;
		}

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				downloadSong();
			}
		});
		thread.start();
	}

	private void downloadSong() {
        //List<HttpCookie> arrayList =  App.getHttpCookies() ;
      //  StringBuilder stringBuilder = new StringBuilder() ;
      //  for (HttpCookie httpCookie : arrayList) {
      //      stringBuilder.append(httpCookie.toString()).append("; ") ;
     //   }
		URL url;
		HttpURLConnection urlConnection = null;
		try {

			long from = raf.length();

			String downloadUrl = this.url  ; //this.song.getDownloadUrl();  //TODO
			Log.d("MylogUrl",downloadUrl) ;
			while (true) {
				url = new URL(downloadUrl);
				urlConnection = (HttpURLConnection)url.openConnection();
				urlConnection.setConnectTimeout(30000);
				urlConnection.setReadTimeout(30000);
				urlConnection.setUseCaches(false);



              //  urlConnection.setRequestProperty("Cookie", stringBuilder.toString());


				from = raf.length();
				if (from > 0) {
					urlConnection.setRequestProperty("Range", "bytes=" + String.valueOf(from) + "-");
				}

				urlConnection.setRequestMethod("GET");
				urlConnection.connect();

				int responseCode = urlConnection.getResponseCode();
				if (responseCode != HttpURLConnection.HTTP_OK
						&& responseCode != HttpURLConnection.HTTP_PARTIAL
						&& responseCode != HttpURLConnection.HTTP_MOVED_TEMP
                        &&  responseCode != HttpURLConnection.HTTP_MOVED_PERM
						&& responseCode != 307) {
					throw new Exception(url.toString() + " returned " + responseCode);
				}

	            if (responseCode == 301 || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
	            	// redirect detected, follow redirect
	            	downloadUrl = urlConnection.getHeaderField("location");
	            	if (downloadUrl == null || downloadUrl.length() == 0) {
	            		break;
	            	}
	            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("ISFILEExists" , file.exists()+"1" ) ;
                    if (file != null && !file.exists()) {
                        boolean isCreated = file.createNewFile() ;
                        Log.d("Iscreated", isCreated+" reinit") ;
                        raf = new RandomAccessFile(file, "rw");
                    }
                    raf.seek(0);
                    Log.d("ISFILEExists" , file.exists()+"2" ) ;
	            	raf.setLength(0);
	            	break;
	            } else {
	            	break;
	            }
			}

			int contentLength = urlConnection.getContentLength();
			setState(DownloadState.Downloading, from + contentLength, from);

			InputStream inputStream = urlConnection.getInputStream();

			byte[] buffer = new byte[64 * 1024];

			while (true) {
				if (stopRequested) {
                    //file.delete();
					setState(DownloadState.Aborted, totalSize, downloadedSize);
					return;
				}

				int count = inputStream.read(buffer);

				if (count == -1) {
                    //L.d("WeDownloadIt", "wow") ;
					if (totalSize == downloadedSize) {
						setState(DownloadState.Downloaded, totalSize, downloadedSize);
					} else {
                       // file.delete();
						setState(DownloadState.Aborted, totalSize, downloadedSize);
                    }
                    break;
				}

				raf.write(buffer, 0, count);

				setState(DownloadState.Downloading, totalSize, downloadedSize + count);
			}
            close(urlConnection);
		} catch (Exception e) {
            e.printStackTrace();
            Log.d("TotalError","IOException");
            boolean isConnected = false ;
            //Handler handler = new Handler(Looper.getMainLooper()) ;
            if (restartCount < MAX_REPEAT_COUNT) {
                while (!isConnected && !stopRequested) {
                    if (Connectivity.isConnected(PlayerInitializer.getContext())) {
                        isConnected = true;
                        restartCount++;
                        try {
                            Thread.sleep(200);
                            Log.d("TotalError", "Restart");
                            close(urlConnection);
                            Log.i("FileM",file.length()+"") ;
                            start();

                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                       // isNetworkProblemDetected = true ;
                    }
                }
            } else {
                setState(DownloadState.TemporaryError, -1, -1);
            }
		}
	}

    private void close(HttpURLConnection urlConnection) {
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
        try {
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Runnable restartRunnable = new Runnable() {
        @Override
        public void run() {
            start();
        }
    } ;


	private final static long requiredSpace = 50000000L;
	public static boolean isCanCache() {
        final String cacheDirPath = StorageUtil.getStorage().getCasheDir() ;
		File cacheDir = new File(cacheDirPath);
		if ( !cacheDir.exists() ) {
			if ( !cacheDir.mkdirs() ) {
			}
		}

		cacheDir = new File(cacheDirPath);
		if ( cacheDir.exists() ) {
			StatFs stat = new StatFs(cacheDirPath);
			long freeSpace = (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();

			return freeSpace > requiredSpace;
		}

		return false;
	}

    public File getFile() {
        return file;
    }

    public boolean isCashedSongPlaying() {
        return isCashedSongPlaying;
    }

    public void setCashedSongPlaying(boolean isCashedSongPlaying) {
        this.isCashedSongPlaying = isCashedSongPlaying;
    }

    public boolean isOfflinePlaying() {
        return isOfflinePlaying;
    }
}
