package com.swipetogive;

import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import com.swipetogive.bluetooth.BluetoothActivity;
import com.swipetogive.wifidirect.WiFiDirectActivity;

public class MainActivity extends ActionBarActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int SWIPE_MIN_DISTANCE = 450;

    float y1,y2;
    ImageAdapter myImageAdapter;

    Uri myUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("RESULT data.getData", "" + data.getData());
        super.onActivityResult(requestCode, resultCode, data);

        ArrayList<String> images = new ArrayList<>();

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == -1) {

            if(data.getData()!=null) {
                Uri imgUri = data.getData();
                Log.d("path", imgUri.getLastPathSegment());
                myUri = data.getData();
                String path = getPath(this,imgUri);
                images.add(path);
            } else if(data.getClipData()!=null){
                ClipData mClipData=data.getClipData();

                for(int i=0;i<mClipData.getItemCount();i++){
                    ClipData.Item item = mClipData.getItemAt(i);
                    Uri uri = item.getUri();
                    String tempPath = getPath(this,uri);
                    images.add(tempPath);
                }

                Log.v("LOG_TAG", "Selected Images: "+ mClipData.getItemCount());
            } else {
                Log.e("ERR", "No data received!");
            }

            /*
            String fileType = getType(uri);
            Log.d("Type", fileType);

            if(fileType.equals("image")) {
                //ImageView imageView = (ImageView) findViewById(R.id.imgView);
                //imageView.setImageBitmap(BitmapFactory.decodeFile(path));
            } else if(fileType.equals("video")) {
                VideoView videoView = (VideoView)findViewById(R.id.VideoView);
                videoView.setVideoPath(path);
                videoView.seekTo(1000);
            }*/

            setContentView(R.layout.activity_main);
            final GridView gridview = (GridView) findViewById(R.id.gridview);
            myImageAdapter = new ImageAdapter(this);
            gridview.setAdapter(myImageAdapter);
            myImageAdapter.notifyDataSetChanged();
            gridview.setOnTouchListener(new TextView.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        // when user first touches the screen we get y coordinate
                        case MotionEvent.ACTION_DOWN: {
                            y1 = event.getY();
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            y2 = event.getY();

                            float length = Math.abs(y2 - y1);

                            if (y1 > y2 && length >= SWIPE_MIN_DISTANCE) {
                                //if(imageView.getDrawable() != null) {
                                Toast.makeText(getApplicationContext(),"Down to UP Swipe Performed!",Toast.LENGTH_SHORT).show();
                                setContentView(R.layout.empty_view);
                                return true;
                            }
                            break;
                        }
                    }
                    return true;
                }
            });

            // add array of images to ImageAdapter
            myImageAdapter.add(images);
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;

                switch(type) {
                    case "image":
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "video":
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "audio":
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        break;
                    default:
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.buttonLoadPicture:
                Intent buttonLoadPictureIntent = new Intent();
                buttonLoadPictureIntent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                buttonLoadPictureIntent.setType("image/*");
                buttonLoadPictureIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                buttonLoadPictureIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(buttonLoadPictureIntent, "Select Picture"), RESULT_LOAD_IMAGE);
                return true;
            case R.id.showBluetoothPeers:
                Intent showBluetoothPeersIntent = new Intent(this, BluetoothActivity.class);
                startActivity(showBluetoothPeersIntent);
                return true;
            case R.id.showWiFiPeers:
                Intent showWiFiPeersIntent = new Intent(this, WiFiDirectActivity.class);
                startActivity(showWiFiPeersIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
    }

}

