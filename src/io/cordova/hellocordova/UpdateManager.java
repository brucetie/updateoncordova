package io.cordova.hellocordova;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * @author bruce
 * @date 2013/12/10 实现软件更新的管理类
 */
public class UpdateManager {

	private String newVerName = "";// 新版本名称

	private Double newVerCode = 0.0;// 新版本号

	private final static String SERVICE_PATH = "http://222.211.19.7/oa/";

	private final static String APK_PATH = "app/moa.apk";

	private final static String VERSION_PATH = "app/version.xml";
	
	private String UPDATE_SERVERAPK = "moa.apk";
    // 下载中...
	private static final int DOWNLOAD = 1;
	// 下载完成
	private static final int DOWNLOAD_FINISH = 2;
	// 保存解析的XML信息
	HashMap<String, String> mHashMap;
	// 下载保存路径
	private String mSavePath;
	// 记录进度条数量
	private int progress;
	// 是否取消更新
	private boolean cancelUpdate = false;
	// 上下文对象
	private Context mContext;
	// 进度条
	private ProgressBar mProgressBar;
	// 更新进度条的对话框
	private Dialog mDownloadDialog;

	private Thread downLoadThread;
	//手动或者自动
	private String eventMode;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			// 下载中。。。
			case DOWNLOAD:
				// 更新进度条
				mProgressBar.setProgress(progress);
				break;
			// 下载完成
			case DOWNLOAD_FINISH:
				// 安装文件
				installApk();
				break;
			}
		};
	};

	public void updateVersion() {
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
		int time = 0;
		do {
			try {
				time += 1000;
				Thread.sleep(1000);
				if (time >= 8000) {
					throw new TimeoutException();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				break;
			}
		} while (newVerCode == 0);
		//
		int verCode = this.getVerCode(mContext);
		if (newVerCode > verCode) {
			doNewVersionUpdate();// 更新版本
		} else if("manual".equals(eventMode)) {
			Toast.makeText(mContext, "当前已是最新版本", Toast.LENGTH_SHORT).show();
		}

	}

	public UpdateManager(Context context,String eventMode) {
		this.mContext = context;
		this.eventMode = eventMode;
	}

	public int getVerCode(Context context) {
		int verCode = -1;
		try {
			String packName = context.getPackageName();
			verCode = context.getPackageManager().getPackageInfo(packName, 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e("版本号获取异常", e.getMessage());
		}
		return verCode;
	}

	public String getVerName(Context context) {
		String verName = "";
		try {
			String packName = context.getPackageName();
			verName = context.getPackageManager().getPackageInfo(packName, 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e("版本名称获取异常", e.getMessage());
		}
		return verName;
	}

	/**
	 * 检查软件是否有更新版本
	 * 
	 * @return
	 */

	private Runnable mdownApkRunnable = new Runnable() {
		public void run() {
		try {
				URL url = new URL(SERVICE_PATH + VERSION_PATH);
				HttpURLConnection httpConnection = (HttpURLConnection) url
						.openConnection();
				httpConnection.setDoInput(true);
				httpConnection.setDoOutput(true);
				httpConnection.setRequestMethod("GET");
				httpConnection.connect();
				ParseXmlService service = new ParseXmlService();
				mHashMap = service.parseXml(httpConnection.getInputStream());
				newVerCode = Double.valueOf(mHashMap.get("version"));
			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	};

	public void notNewVersionUpdate() {
		int verCode = this.getVerCode(mContext);
		String verName = this.getVerName(mContext);
		StringBuffer sb = new StringBuffer();
		sb.append("当前版本：");
		sb.append(verName);
		sb.append(" Code:");
		sb.append(verCode);
		sb.append("\n已是最新版本，无需更新");
		Dialog dialog = new AlertDialog.Builder(mContext).setTitle("软件更新")
				.setMessage(sb.toString())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// finish();
					}
				}).create();
		dialog.show();
	}

	public void doNewVersionUpdate() {
		int verCode = this.getVerCode(mContext);
		String verName = this.getVerName(mContext);
		StringBuffer sb = new StringBuffer();
		sb.append("当前版本：");
		sb.append(verName);
		sb.append(" Code:");
		sb.append(verCode);
		sb.append(",发现版本：");
		sb.append(newVerName);
		sb.append(" Code:");
		sb.append(newVerCode);
		sb.append(",是否更新");
		Dialog dialog = new AlertDialog.Builder(mContext)
				.setTitle("软件更新")
				.setMessage(sb.toString())
				.setPositiveButton("更新", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						showDownloadDialog();
					}
				})
				.setNegativeButton("暂不更新",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								// finish();
							}
						}).create();
		// 显示更新框
		dialog.show();
	}

	private void showDownloadDialog() {
		// 构造软件下载对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("更新中");
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.progress, null);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
		builder.setView(view);
		builder.setNegativeButton("取消", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				// 设置取消状态
				cancelUpdate = true;
			}
		});
		mDownloadDialog = builder.create();
		mDownloadDialog.show();

		// 下载文件
		// new downloadApkThread().start();
		downloadApk();

	}

	private void downloadApk() {
		downLoadThread = new Thread(downloadApkThread);
		downLoadThread.start();
	}

	/**
	 * 下载文件线程
	 */

	private Runnable downloadApkThread = new Runnable() {

		public void run() {
			try {

				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// 获取SDCard的路径
					String sdpath = Environment.getExternalStorageDirectory()+ "/";
					mSavePath = sdpath + "download";
					URL url = new URL(SERVICE_PATH + APK_PATH);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setDoInput(true);
					conn.setDoOutput(true);
					conn.setRequestMethod("GET");
					conn.connect();
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					// 如果文件不存在，新建目录
					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(mSavePath, UPDATE_SERVERAPK);
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						count += numread;
						// 计算进度条的位置
						progress = (int) (((float) count / length) * 100);
						// 更新进度
						handler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0) {
							// 下载完成
							handler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 取消下载对话框显示
			mDownloadDialog.dismiss();
		}

	};

	/**
	 * 安装APK文件
	 */
	private void installApk() {
		File apkfile = new File(mSavePath, UPDATE_SERVERAPK);
		if (!apkfile.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		mContext.startActivity(i);
	}
}
