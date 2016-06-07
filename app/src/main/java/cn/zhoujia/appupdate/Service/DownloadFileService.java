package cn.zhoujia.appupdate.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.zhoujia.appupdate.CallBack.DownloadFileCallback;
import cn.zhoujia.appupdate.R;
import cn.zhoujia.appupdate.Util.DownloadFileUtils;


/**
 * 文件下载的service
 * 
 * @author 
 * 
 */
public class DownloadFileService extends Service {
	protected static final String STATUS_BAR_COVER_CLICK_ACTION = "quxiao";
	private DownloadFileUtils downloadFileUtils;// 文件下载工具类
	private String filePath;// 保存在本地的路径
	private NotificationManager notificationManager;// 状态栏通知管理类
	private NotificationCompat.Builder builder;
	private RemoteViews remoteViews;// 状态栏通知显示的view
	private int notificationID = 1;// 通知的id
	private final int updateProgress = 1;// 更新状态栏的下载进度
	private final int downloadSuccess = 2;// 下载成功
	private final int downloadError = 3;// 下载失败
	private final int no_file = 0;
	private final String TAG = "DownloadFileService";
	private Timer timer;// 定时器，用于更新下载进度
	private TimerTask task;// 定时器执行的任务
	private String fileName, fileId, fileUrl;
	private long fileSize;
	private boolean indeterminate = false;
	public static boolean isDownload = false;
	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public void onCreate() {
		
	}

	private void init() {
		notificationID = (int) new Date().getTime();
		filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getPackageName();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		builder = new NotificationCompat.Builder(getBaseContext())
					.setSmallIcon(R.mipmap.ic_launcher)
					.setContentTitle("安装包正在下载...")
					.setContentText("0%");
		builder.setProgress(100, 0,indeterminate);
		notificationManager.notify(notificationID, builder.build());
		timer = new Timer();
		task = new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(updateProgress);
			}
		};
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(!isDownload){
			isDownload = true;
			init();
			Bundle bundle = null;
			if(intent!=null){
				if (intent.getExtras() != null) {
					bundle = intent.getExtras();
				}
			}
			if (bundle != null) {
				fileName = bundle.getString("fileName");
				// fileSize = bundle.getLong("fileSize");
				fileUrl = bundle.getString("fileUrl");
				new Thread(new Runnable() {
					@Override
					public void run() {
						downloadFileUtils = new DownloadFileUtils(fileUrl,filePath,fileName, 1, callback, fileSize);
						downloadFileUtils.downloadFile();
						System.out.println("开始下载");
					}
				}).start();
				timer.schedule(task, 500, 500);
			} else {
				builder.setContentTitle("下载失败");
			}
		}else{
			Toast.makeText(getApplicationContext(), "c", Toast.LENGTH_SHORT).show();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, TAG + " is onDestory...");
		super.onDestroy();
	}
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == updateProgress) {// 更新下载进度
				long fileSize = downloadFileUtils.getFileSize();
				long totalReadSize = downloadFileUtils.getTotalReadSize();
				if (totalReadSize > 0) {
					float size = (float) totalReadSize * 100 / (float) fileSize;
					int progress = (int) size;
					if(progress>100){
						progress = 99;
					}
					builder.setContentText(progress+"%")
						   .setProgress(100, progress, indeterminate);
					notificationManager.notify(notificationID, builder.build());
				}
			} else if (msg.what == downloadSuccess) {// 下载完成
				builder.setContentTitle("下载完成")
					   .setContentText("100%")
					   .setProgress(100, 100, indeterminate);
				File file = new File(filePath, fileName);
				Intent intent = new Intent();
		        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        intent.setAction(android.content.Intent.ACTION_VIEW);
		        intent.setDataAndType(Uri.fromFile(file),
		                        "application/vnd.android.package-archive");
				PendingIntent contentIntent = PendingIntent.getActivity(
						getApplicationContext(), R.string.app_name,intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				Notification notification = builder.build();
				notification.contentIntent = contentIntent;
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				notificationManager.notify(notificationID,notification);
				if (timer != null && task != null) {
					timer.cancel();
					task.cancel();
					timer = null;
					task = null;
				}
				startActivity(intent);
				notificationManager.cancel(notificationID);
				stopService(new Intent(getApplicationContext(),
						DownloadFileService.class));// stop service
			} else if (msg.what == downloadError) {// 下载失败
				if (timer != null && task != null) {
					timer.cancel();
					task.cancel();
					timer = null;
					task = null;
				}
				builder.setContentTitle("下载失败"); 
				notificationManager.notify(notificationID, builder.build());
				stopService(new Intent(getApplicationContext(),
						DownloadFileService.class));// stop service
			}else{
				builder.setContentTitle("下载失败"); 
				notificationManager.notify(notificationID, builder.build());
				stopService(new Intent(getApplicationContext(),
						DownloadFileService.class));// stop service
			}
		}

	};
	/**
	 * 下载回调
	 */
	DownloadFileCallback callback = new DownloadFileCallback() {

		@Override
		public void downloadSuccess(Object obj) {
			isDownload = false;
			handler.sendEmptyMessage(downloadSuccess);
		}

		@Override
		public void downloadError(Exception e, String msg) {
			isDownload = false;
			if("no file".equals(msg)){
				handler.sendEmptyMessage(no_file);
			}else{
				handler.sendEmptyMessage(downloadError);
			}
		}
	};
	
	

}
