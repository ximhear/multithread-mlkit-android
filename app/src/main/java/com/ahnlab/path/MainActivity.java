package com.ahnlab.path;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.*;
import io.reactivex.functions.*;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;


class RecogInfo {
	Integer index;
	Bitmap bitmap;

	RecogInfo(Integer a, Bitmap b) {
		index = a;
		bitmap = b;
	}
}



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	public static Integer stateReady = 0;
	public static Integer stateLoading = 1;


	private static final String TAG = MainActivity.class.getSimpleName();
	public static final int MY_PERMISSIONS_REQUEST = 1;
    TextView tv;
    ContentResolver mContentResolver;
    Cursor mCursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();
        tv = findViewById(R.id.tv_path_list);
        tv.setOnClickListener(this);

    }

	@Override
	protected void onResume() {
		super.onResume();


	}

	private void getPermission() {
		Log.d(TAG, "getPermission: ");
		int accessCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
		int accessStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

		List<String> listRequestPermission = new ArrayList<String>();

		if (accessCamera != PackageManager.PERMISSION_GRANTED) {
			listRequestPermission.add(Manifest.permission.CAMERA);
		}
		if (accessStorage != PackageManager.PERMISSION_GRANTED) {
			listRequestPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}


		if (listRequestPermission.isEmpty()) {
			// initialization();
			// init();
		} else {
			String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
			ActivityCompat.requestPermissions(this, strRequestPermission, MY_PERMISSIONS_REQUEST);
		}
	}


	@Override
    public void onClick(View v) {
    	switch (v.getId()) {
		    case R.id.tv_path_list:

		    	dodo();
		    	break;
	    }
    }

    public Bitmap loadBitmap(String filePath) {
//    	long start = System.currentTimeMillis();
		File imageFile = new File(filePath);
		BitmapFactory.Options options = getBitmapSubSampleOptions(imageFile);
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
//		Bitmap bitmap = BitmapFactory.decodeFile(filePath);
//		Log.d("duration", filePath + " : " + (System.currentTimeMillis() - start));
		return bitmap;
	}

//	public void recogize(Bitmap bitmap) {
//
////		final long start = System.currentTimeMillis();
//		FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
//		Task<FirebaseVisionText> result =
//				detector.processImage(image)
//						.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//							@Override
//							public void onSuccess(FirebaseVisionText firebaseVisionText) {
//								Log.d("recogizeText", firebaseVisionText.getText());
////								Log.d("duration", "" + (System.currentTimeMillis() - start));
//							}
//						})
//						.addOnFailureListener(
//								new OnFailureListener() {
//									@Override
//									public void onFailure(@NonNull Exception e) {
//									}
//								});
//	}

	int fileIndex = 0;
    int completedCount = 0;
	FirebaseVisionTextRecognizer detector0 = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
	FirebaseVisionTextRecognizer detector1 = detector0;//FirebaseVision.getInstance().getOnDeviceTextRecognizer();
	FirebaseVisionTextRecognizer detector2 = detector0;//FirebaseVision.getInstance().getOnDeviceTextRecognizer();
	FirebaseVisionTextRecognizer detector3 = detector0;//FirebaseVision.getInstance().getOnDeviceTextRecognizer();
    public void dodo() {

		if (mContentResolver == null) {
			mContentResolver = getContentResolver();
		}
		mCursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				null,
				null,
				null,
				null);

		Log.d(TAG, "onCreate: 총 이미지 파일 갯수 : " + mCursor.getCount() + "\n");




		final long start = System.currentTimeMillis();
		final ArrayList<Bitmap> bitmaps = new ArrayList();
		final BehaviorSubject<Integer> bitmapQueueState = BehaviorSubject.createDefault(0);

		final ArrayList<String> filePaths = new ArrayList();
		while (mCursor.moveToNext()) {
			String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
			if (path.contains("DCIM")) {
				filePaths.add(path);
				Log.d(TAG, path);
			}
//			Log.d(TAG, "onCreate: 파일 경로 : " + mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)) + "\n");
		}

		final BehaviorSubject<Integer> loaderState0 = BehaviorSubject.createDefault(MainActivity.stateReady);
		final PublishSubject<String> loader0 = PublishSubject.create();
		loader0.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<String>() {
					@Override
					public void accept(String filePath) throws Exception {
//						Log.d("loader0", "onNext : " + Thread.currentThread());
						Log.d("loader0", "onNext : " + filePath);

						Bitmap bitmap = loadBitmap(filePath);
						if (bitmap != null) {
							synchronized (bitmaps) {
								bitmaps.add(bitmap);
								bitmapQueueState.onNext(bitmaps.size());
							}
						}
						loaderState0.onNext(MainActivity.stateReady);

					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("loader0", "onError : " + Thread.currentThread());
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("loader0", "onCompleted : " + Thread.currentThread());
					}
				});

		final BehaviorSubject<Integer> loaderState1 = BehaviorSubject.createDefault(MainActivity.stateReady);
		final PublishSubject<String> loader1 = PublishSubject.create();
		loader1.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<String>() {
					@Override
					public void accept(String filePath) throws Exception {
//						Log.d("loader1", "onNext : " + Thread.currentThread());
						Log.d("loader1", "onNext : " + filePath);
						Bitmap bitmap = loadBitmap(filePath);
						if (bitmap != null) {
							synchronized (bitmaps) {
								bitmaps.add(bitmap);
								bitmapQueueState.onNext(bitmaps.size());
							}
						}
						loaderState1.onNext(MainActivity.stateReady);
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("loader1", "onError : " + Thread.currentThread());
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("loader1", "onCompleted : " + Thread.currentThread());
					}
				});

		final BehaviorSubject<Integer> loaderState2 = BehaviorSubject.createDefault(MainActivity.stateReady);
		final PublishSubject<String> loader2 = PublishSubject.create();
		loader2.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<String>() {
					@Override
					public void accept(String filePath) throws Exception {
//						Log.d("loader2", "onNext : " + Thread.currentThread());
						Log.d("loader2", "onNext : " + filePath);
						Bitmap bitmap = loadBitmap(filePath);
						if (bitmap != null) {
							synchronized (bitmaps) {
								bitmaps.add(bitmap);
								bitmapQueueState.onNext(bitmaps.size());
							}
						}
						loaderState2.onNext(MainActivity.stateReady);
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("loader2", "onError : " + Thread.currentThread());
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("loader2", "onCompleted : " + Thread.currentThread());
					}
				});

		final BehaviorSubject<Integer> loaderState3 = BehaviorSubject.createDefault(MainActivity.stateReady);
		final PublishSubject<String> loader3 = PublishSubject.create();
		loader3.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<String>() {
					@Override
					public void accept(String filePath) throws Exception {
//						Log.d("loader3", "onNext : " + Thread.currentThread());
						Log.d("loader3", "onNext : " + filePath);
						Bitmap bitmap = loadBitmap(filePath);
						if (bitmap != null) {
							synchronized (bitmaps) {
								bitmaps.add(bitmap);
								bitmapQueueState.onNext(bitmaps.size());
							}
						}
						loaderState3.onNext(MainActivity.stateReady);
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("loader3", "onError : " + Thread.currentThread());
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("loader3", "onCompleted : " + Thread.currentThread());
					}
				});

		final BehaviorSubject<Integer> loaderState4 = BehaviorSubject.createDefault(MainActivity.stateReady);
		final PublishSubject<String> loader4 = PublishSubject.create();
		loader4.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<String>() {
					@Override
					public void accept(String filePath) throws Exception {
//						Log.d("loader3", "onNext : " + Thread.currentThread());
						Log.d("loader4", "onNext : " + filePath);
						Bitmap bitmap = loadBitmap(filePath);
						if (bitmap != null) {
							synchronized (bitmaps) {
								bitmaps.add(bitmap);
								bitmapQueueState.onNext(bitmaps.size());
							}
						}
						loaderState4.onNext(MainActivity.stateReady);
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("loader3", "onError : " + Thread.currentThread());
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("loader3", "onCompleted : " + Thread.currentThread());
					}
				});

		final BehaviorSubject<Integer> loaderState5 = BehaviorSubject.createDefault(MainActivity.stateReady);
		final PublishSubject<String> loader5 = PublishSubject.create();
		loader5.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<String>() {
					@Override
					public void accept(String filePath) throws Exception {
//						Log.d("loader3", "onNext : " + Thread.currentThread());
						Log.d("loader5", "onNext : " + filePath);
						Bitmap bitmap = loadBitmap(filePath);
						if (bitmap != null) {
							synchronized (bitmaps) {
								bitmaps.add(bitmap);
								bitmapQueueState.onNext(bitmaps.size());
							}
						}
						loaderState5.onNext(MainActivity.stateReady);
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("loader3", "onError : " + Thread.currentThread());
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("loader3", "onCompleted : " + Thread.currentThread());
					}
				});

		final BehaviorSubject<Integer> loaderState6 = BehaviorSubject.createDefault(MainActivity.stateReady);
		final PublishSubject<String> loader6 = PublishSubject.create();
		loader6.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<String>() {
					@Override
					public void accept(String filePath) throws Exception {
//						Log.d("loader3", "onNext : " + Thread.currentThread());
						Log.d("loader6", "onNext : " + filePath);
						Bitmap bitmap = loadBitmap(filePath);
						if (bitmap != null) {
							synchronized (bitmaps) {
								bitmaps.add(bitmap);
								bitmapQueueState.onNext(bitmaps.size());
							}
						}
						loaderState6.onNext(MainActivity.stateReady);
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("loader3", "onError : " + Thread.currentThread());
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("loader3", "onCompleted : " + Thread.currentThread());
					}
				});

		final BehaviorSubject<Integer> loaderState7 = BehaviorSubject.createDefault(MainActivity.stateReady);
		final PublishSubject<String> loader7 = PublishSubject.create();
		loader7.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<String>() {
					@Override
					public void accept(String filePath) throws Exception {
//						Log.d("loader3", "onNext : " + Thread.currentThread());
						Log.d("loader7", "onNext : " + filePath);
						Bitmap bitmap = loadBitmap(filePath);
						if (bitmap != null) {
							synchronized (bitmaps) {
								bitmaps.add(bitmap);
								bitmapQueueState.onNext(bitmaps.size());
							}
						}
						loaderState7.onNext(MainActivity.stateReady);
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("loader3", "onError : " + Thread.currentThread());
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("loader3", "onCompleted : " + Thread.currentThread());
					}
				});

		final PublishSubject<Integer> bitmapLoaderBalancer = PublishSubject.create();
		bitmapLoaderBalancer
				.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread()).subscribe(new Consumer<Integer>() {
			@Override
			public void accept(Integer loaderIndex) throws Exception {
//				Log.d("bitmapLoaderBalancer", "onNext : " + Thread.currentThread());
				if (filePaths.size() <= fileIndex) {
					return;
				}
//				Log.d("bitmapLoaderBalancer", "onNext : loaderIndex - " + loaderIndex);
				if (loaderIndex == 0) {
					loaderState0.onNext(MainActivity.stateLoading);
					loader0.onNext(filePaths.get(fileIndex));
					fileIndex++;
				}
				else if (loaderIndex == 1) {
					loaderState1.onNext(MainActivity.stateLoading);
					loader1.onNext(filePaths.get(fileIndex));
					fileIndex++;
				}
				else if (loaderIndex == 2) {
					loaderState2.onNext(MainActivity.stateLoading);
					loader2.onNext(filePaths.get(fileIndex));
					fileIndex++;
				}
				else if (loaderIndex == 3) {
					loaderState3.onNext(MainActivity.stateLoading);
					loader3.onNext(filePaths.get(fileIndex));
					fileIndex++;
				}
				else if (loaderIndex == 4) {
					loaderState4.onNext(MainActivity.stateLoading);
					loader4.onNext(filePaths.get(fileIndex));
					fileIndex++;
				}
				else if (loaderIndex == 5) {
					loaderState5.onNext(MainActivity.stateLoading);
					loader5.onNext(filePaths.get(fileIndex));
					fileIndex++;
				}
				else if (loaderIndex == 6) {
					loaderState6.onNext(MainActivity.stateLoading);
					loader6.onNext(filePaths.get(fileIndex));
					fileIndex++;
				}
				else if (loaderIndex == 7) {
					loaderState7.onNext(MainActivity.stateLoading);
					loader7.onNext(filePaths.get(fileIndex));
					fileIndex++;
				}
			}
		}, new Consumer<Throwable>() {
			@Override
			public void accept(Throwable throwable) throws Exception {
//				Log.d("bitmapLoaderBalancer", "onError : " + Thread.currentThread());
			}
		}, new Action() {
			@Override
			public void run() throws Exception {
//				Log.d("bitmapLoaderBalancer", "onCompleted : " + Thread.currentThread());
			}
		});

		final BehaviorSubject<Integer> recogState0 = BehaviorSubject.createDefault(MainActivity.stateReady);
		final PublishSubject<Bitmap> recog0 = PublishSubject.create();
		recog0
				.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<Bitmap>() {
					@Override
					public void accept(Bitmap bitmap) throws Exception {
//						Log.d("recog0", "onNext : " + Thread.currentThread());

//						final long start = System.currentTimeMillis();
						FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
						Task<FirebaseVisionText> result =
								detector0.processImage(image)
										.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
											@Override
											public void onSuccess(FirebaseVisionText firebaseVisionText) {
//												Log.d("recogizeText success", "recog0 " + firebaseVisionText.getText());
												completedCount++;
												Log.d("duration s", "" + completedCount + " : " + (System.currentTimeMillis() - start));
												recogState0.onNext(MainActivity.stateReady);
											}
										})
										.addOnFailureListener(
												new OnFailureListener() {
													@Override
													public void onFailure(@NonNull Exception e) {
//														Log.d("recogizeText fail", "recog0 ");
														completedCount++;
														Log.d("duration f", "" + completedCount + " : " + (System.currentTimeMillis() - start));
														recogState0.onNext(MainActivity.stateReady);
													}
												})
										.addOnCompleteListener(new OnCompleteListener<FirebaseVisionText>() {
											@Override
											public void onComplete(@NonNull Task<FirebaseVisionText> task) {
//												Log.d("recogizeText complete", "recog0 ");
//												Log.d("duration", "" + (System.currentTimeMillis() - start));
											}
										});
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("recog0", "onError : " + Thread.currentThread());
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("recog0", "onCompleted : " + Thread.currentThread());
					}
				});

		final BehaviorSubject<Integer> recogState1 = BehaviorSubject.createDefault(MainActivity.stateReady);
		final PublishSubject<Bitmap> recog1 = PublishSubject.create();
		recog1
				.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<Bitmap>() {
					@Override
					public void accept(Bitmap bitmap) throws Exception {
//						Log.d("recog1", "onNext : " + Thread.currentThread());
						FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
						Task<FirebaseVisionText> result =
								detector1.processImage(image)
										.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
											@Override
											public void onSuccess(FirebaseVisionText firebaseVisionText) {
//												Log.d("recogizeText success", "recog1 " + firebaseVisionText.getText());
												completedCount++;
												Log.d("duration s", "" + completedCount + " : " + (System.currentTimeMillis() - start));
												recogState1.onNext(MainActivity.stateReady);
											}
										})
										.addOnFailureListener(
												new OnFailureListener() {
													@Override
													public void onFailure(@NonNull Exception e) {
//														Log.d("recogizeText fail", "recog1 ");
														completedCount++;
														Log.d("duration f", "" + completedCount + " : " + (System.currentTimeMillis() - start));
														recogState1.onNext(MainActivity.stateReady);
													}
												})
										.addOnCompleteListener(new OnCompleteListener<FirebaseVisionText>() {
											@Override
											public void onComplete(@NonNull Task<FirebaseVisionText> task) {
//												Log.d("recogizeText complete", "recog1 ");
//												Log.d("duration", "" + (System.currentTimeMillis() - start));
											}
										});
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("recog1", "onError : " + Thread.currentThread());
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("recog1", "onCompleted : " + Thread.currentThread());
					}
				});

		final BehaviorSubject<Integer> recogState2 = BehaviorSubject.createDefault(MainActivity.stateReady);
		final PublishSubject<Bitmap> recog2 = PublishSubject.create();
		recog2
				.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<Bitmap>() {
					@Override
					public void accept(Bitmap bitmap) throws Exception {
//						Log.d("recog1", "onNext : " + Thread.currentThread());
						FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
						Task<FirebaseVisionText> result =
								detector2.processImage(image)
										.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
											@Override
											public void onSuccess(FirebaseVisionText firebaseVisionText) {
//												Log.d("recogizeText success", "recog1 " + firebaseVisionText.getText());
												completedCount++;
												Log.d("duration s", "" + completedCount + " : " + (System.currentTimeMillis() - start));
												recogState2.onNext(MainActivity.stateReady);
											}
										})
										.addOnFailureListener(
												new OnFailureListener() {
													@Override
													public void onFailure(@NonNull Exception e) {
//														Log.d("recogizeText fail", "recog1 ");
														completedCount++;
														Log.d("duration f", "" + completedCount + " : " + (System.currentTimeMillis() - start));
														recogState2.onNext(MainActivity.stateReady);
													}
												})
										.addOnCompleteListener(new OnCompleteListener<FirebaseVisionText>() {
											@Override
											public void onComplete(@NonNull Task<FirebaseVisionText> task) {
//												Log.d("recogizeText complete", "recog1 ");
//												Log.d("duration", "" + (System.currentTimeMillis() - start));
											}
										});
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("recog1", "onError : " + Thread.currentThread());
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("recog1", "onCompleted : " + Thread.currentThread());
					}
				});

		final BehaviorSubject<Integer> recogState3 = BehaviorSubject.createDefault(MainActivity.stateReady);
		final PublishSubject<Bitmap> recog3 = PublishSubject.create();
		recog3
				.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<Bitmap>() {
					@Override
					public void accept(Bitmap bitmap) throws Exception {
//						Log.d("recog1", "onNext : " + Thread.currentThread());
						FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
						Task<FirebaseVisionText> result =
								detector3.processImage(image)
										.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
											@Override
											public void onSuccess(FirebaseVisionText firebaseVisionText) {
//												Log.d("recogizeText success", "recog1 " + firebaseVisionText.getText());
												completedCount++;
												Log.d("duration s", "" + completedCount + " : " + (System.currentTimeMillis() - start));
												recogState3.onNext(MainActivity.stateReady);
											}
										})
										.addOnFailureListener(
												new OnFailureListener() {
													@Override
													public void onFailure(@NonNull Exception e) {
//														Log.d("recogizeText fail", "recog1 ");
														completedCount++;
														Log.d("duration f", "" + completedCount + " : " + (System.currentTimeMillis() - start));
														recogState3.onNext(MainActivity.stateReady);
													}
												})
										.addOnCompleteListener(new OnCompleteListener<FirebaseVisionText>() {
											@Override
											public void onComplete(@NonNull Task<FirebaseVisionText> task) {
//												Log.d("recogizeText complete", "recog1 ");
//												Log.d("duration", "" + (System.currentTimeMillis() - start));
											}
										});
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("recog1", "onError : " + Thread.currentThread());
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("recog1", "onCompleted : " + Thread.currentThread());
					}
				});

		final PublishSubject<RecogInfo> recogLoaderBalancer = PublishSubject.create();
		recogLoaderBalancer
				.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<RecogInfo>() {
			@Override
			public void accept(RecogInfo info) throws Exception {
//				Log.d("recogLoaderBalancer", "onNext : " + Thread.currentThread());
//				Log.d("recogLoaderBalancer", "index : " + info.index);
				if (info.index == 0) {
					recogState0.onNext(MainActivity.stateLoading);
					recog0.onNext(info.bitmap);
				}
				else if (info.index == 1) {
					recogState1.onNext(MainActivity.stateLoading);
					recog1.onNext(info.bitmap);
				}
				else if (info.index == 2) {
					recogState2.onNext(MainActivity.stateLoading);
					recog2.onNext(info.bitmap);
				}
				else if (info.index == 3) {
					recogState3.onNext(MainActivity.stateLoading);
					recog3.onNext(info.bitmap);
				}
			}
		}, new Consumer<Throwable>() {
			@Override
			public void accept(Throwable throwable) throws Exception {
//				Log.d("recogLoaderBalancer", "onError : " + Thread.currentThread());
			}
		}, new Action() {
			@Override
			public void run() throws Exception {
//				Log.d("recogLoaderBalancer", "onCompleted : " + Thread.currentThread());
			}
		});

		Observable.combineLatest(recogState0, recogState1, recogState2, recogState3, bitmapQueueState, new Function5<Integer, Integer, Integer, Integer, Integer, Integer[]>() {

			public Integer[] apply(Integer a, Integer b, Integer c, Integer d, Integer bitmapCount) throws Exception {

				if (bitmapCount == 0) {
					return new Integer[] {};
				}
				return new Integer[] {a, b, c, d};
			}

		})
				.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<Integer[]>() {
					@Override
					public void accept(Integer[] states) throws Exception {
//						Log.d("combineLatest", "recog onNext : " + Thread.currentThread());
						if (states != null && states.length == 4 && bitmaps.size() > 0) {
							if (states[0] == MainActivity.stateReady) {
								synchronized (bitmaps) {
									RecogInfo a = new RecogInfo(0, bitmaps.get(0));
									bitmaps.remove(0);
									bitmapQueueState.onNext(bitmaps.size());
									recogLoaderBalancer.onNext(a);
								}
							}
							else if (states[1] == MainActivity.stateReady) {
								synchronized (bitmaps) {
									RecogInfo a = new RecogInfo(1, bitmaps.get(0));
									bitmaps.remove(0);
									bitmapQueueState.onNext(bitmaps.size());
									recogLoaderBalancer.onNext(a);
								}
							}
							else if (states[2] == MainActivity.stateReady) {
								synchronized (bitmaps) {
									RecogInfo a = new RecogInfo(2, bitmaps.get(0));
									bitmaps.remove(0);
									bitmapQueueState.onNext(bitmaps.size());
									recogLoaderBalancer.onNext(a);
								}
							}
							else if (states[3] == MainActivity.stateReady) {
								synchronized (bitmaps) {
									RecogInfo a = new RecogInfo(3, bitmaps.get(0));
									bitmaps.remove(0);
									bitmapQueueState.onNext(bitmaps.size());
									recogLoaderBalancer.onNext(a);
								}
							}
						}
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("combineLatest", "recog onError : " + Thread.currentThread());
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("combineLatest", "recog onCompleted : " + Thread.currentThread());
					}
				});

		Observable.combineLatest(loaderState0, loaderState1, loaderState2, loaderState3, loaderState4, loaderState5, loaderState6, loaderState7, bitmapQueueState, new Function9<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer[]>() {

			public Integer[] apply(Integer a, Integer b, Integer c, Integer d, Integer e, Integer f, Integer g, Integer h, Integer bitmapCount) throws Exception {

				if (filePaths.size() <= fileIndex) {
//					Log.d("combineLatest", "completed");
					return new Integer[]{};
				}
				if (bitmapCount < 8) {
					return new Integer[] {a, b, c, d, e, f, g, h};
				}
				return new Integer[]{};
			}

		})
				.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.newThread())
				.subscribe(new Consumer<Integer[]>() {
					@Override
					public void accept(Integer[] states) throws Exception {
//						Log.d("combineLatest", "onNext : " + Thread.currentThread());
						if (states != null && states.length == 8) {
							if (states[0] == MainActivity.stateReady) {
								bitmapLoaderBalancer.onNext(0);
							}
							else if (states[1] == MainActivity.stateReady) {
								bitmapLoaderBalancer.onNext(1);
							}
							else if (states[2] == MainActivity.stateReady) {
								bitmapLoaderBalancer.onNext(2);
							}
							else if (states[3] == MainActivity.stateReady) {
								bitmapLoaderBalancer.onNext(3);
							}
							else if (states[4] == MainActivity.stateReady) {
								bitmapLoaderBalancer.onNext(4);
							}
							else if (states[5] == MainActivity.stateReady) {
								bitmapLoaderBalancer.onNext(5);
							}
							else if (states[6] == MainActivity.stateReady) {
								bitmapLoaderBalancer.onNext(6);
							}
							else if (states[7] == MainActivity.stateReady) {
								bitmapLoaderBalancer.onNext(7);
							}
						}
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
//						Log.d("combineLatest", "onError : " + Thread.currentThread());
//						System.out.println(throwable);
					}
				}, new Action() {
					@Override
					public void run() throws Exception {
//						Log.d("combineLatest", "onCompleted : " + Thread.currentThread());
					}
				});

	}

	// 이미지 Resize 함수
	private int setSimpleSize(BitmapFactory.Options options, int requestSize){
		// 이미지 사이즈를 체크할 원본 이미지 가로/세로 사이즈를 임시 변수에 대입.
		int originalWidth = options.outWidth;
		int originalHeight = options.outHeight;
//		Log.d(TAG, "originalWidth_" + originalWidth + " originalHeight_"+originalHeight);

		// 원본 이미지 비율인 1로 초기화
		int size = 1;

		// 해상도가 깨지지 않을만한 요구되는 사이즈까지 2의 배수의 값으로 원본 이미지를 나눈다.
		if (originalWidth > originalHeight) {       // 가로가 크면 가로기준으로 2씩 나누고
			while(requestSize < originalWidth){
				originalWidth = originalWidth / 2;
				originalHeight = originalHeight / 2;

				size = size * 2;
			}
		} else {                                    // 세로가 크면 세로 기준으로 2씩 나눈다.
			while(requestSize < originalHeight){
				originalWidth = originalWidth / 2;
				originalHeight = originalHeight / 2;

				size = size * 2;
			}
		}

		return size;
	}

	private BitmapFactory.Options getBitmapSubSampleOptions(File imgFile) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		// inJustDecodeBounds = true일때 BitmapFactory.decodeResource는 리턴하지 않는다.
		// 즉 bitmap은 반환하지않고, options 변수에만 값이 대입된다.
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

//		Log.d(TAG, "before options.inSampleSize : " + options.inSampleSize);

		// 이미지 사이즈를 필요한 사이즈로 적당히 줄이기위해 계산한 값을
		// options.inSampleSize 에 2의 배수의 값으로 넣어준다.
		options.inSampleSize = setSimpleSize(options, 1024);
//		Log.d(TAG, "end options.inSampleSize : " + options.inSampleSize);

		// options.inJustDecodeBounds 에 false 로 다시 설정해서 BitmapFactory.decodeResource의 Bitmap을 리턴받을 수 있게한다.
		options.inJustDecodeBounds = false;
		return options;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String permissions[],
	                                       @NonNull int[] grantResults) {
		Log.d(TAG, "onRequestPermissionsResult: ");
		if (requestCode == MY_PERMISSIONS_REQUEST && grantResults.length > 0) {
			for (int gr : grantResults) {
				if (gr != PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(MainActivity.this, "사용 승인을 받지 못했습니다.",
							Toast.LENGTH_LONG).show();
					return;
				}
			}
			// initialization();
		} else {
			Toast.makeText(MainActivity.this, "사용 승인을 받지 못했습니다.",
					Toast.LENGTH_LONG).show();
		}
	}
}
