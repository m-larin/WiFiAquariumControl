package ru.larin.wifipowercontroller.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import ru.larin.wifipowercontroller.R;
import ru.larin.wifipowercontroller.data.WpcDatabaseHelper;
import ru.larin.wifipowercontroller.model.Device;

public class DeviseActivity extends AppCompatActivity {
    public static final int PICK_IMAGE = 1;

    private EditText idTextBox;
    private EditText ipTextBox;
    private EditText nameTextBox;
    private ImageView imageView;
    private byte[] img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devise);

        idTextBox = (EditText)findViewById(R.id.editTextId);
        ipTextBox = (EditText)findViewById(R.id.editTextIp);
        nameTextBox = (EditText)findViewById(R.id.editTextName);
        imageView = (ImageView) findViewById(R.id.imageView);

        Device device = (Device)getIntent().getSerializableExtra("device");
        if (device != null) {
            idTextBox.setText(String.valueOf(device.getId()));
            ipTextBox.setText(device.getIp());
            nameTextBox.setText(device.getName());
            if (device.getImg() == null) {
                imageView.setImageResource(R.drawable.controller_button_0);
            }else{
                Bitmap image = BitmapFactory.decodeByteArray(device.getImg(), 0, device.getImg().length);
                imageView.setImageBitmap(image);
                img = device.getImg();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_config_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        WpcDatabaseHelper dataHelper = new WpcDatabaseHelper(this);

        Device devise = new Device();
        devise.setId(Long.valueOf(idTextBox.getText().toString()));
        devise.setIp(ipTextBox.getText().toString());
        devise.setName(nameTextBox.getText().toString());
        if (img != null) {
            devise.setImg(img);
        }

        dataHelper.saveDevice(devise);

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();

        return super.onOptionsItemSelected(item);
    }

    public void onSelect(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            InputStream inputStream = null;
            try {
                Uri uri = data.getData();
                inputStream = getContentResolver().openInputStream(uri);

                Bitmap origBitmap = BitmapFactory.decodeStream(inputStream);
                //Урезаем до размера не более половины ширины экрана по любой из сторон
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int maxSize = metrics.widthPixels < metrics.heightPixels ? metrics.widthPixels / 2 : metrics.heightPixels /2;
                Bitmap bitmap = scaleBitmap(origBitmap, maxSize);
                if (bitmap != origBitmap) {
                    origBitmap.recycle();
                }

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.WEBP, 25, stream);
                img = stream.toByteArray();

                imageView.setImageBitmap(bitmap);

            }catch (Exception ex){
                ErrorDialog.showError(this, "Error", ex);
            }finally {
                try {
                    inputStream.close();
                }catch (Exception ignoreEx){
                }
            }
        }
    }

    public Bitmap scaleBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float excessSizeRatio = width > height ? width / maxSize : height / maxSize;
        Bitmap newBitmap = Bitmap.createScaledBitmap(
                bitmap, (int)(width/excessSizeRatio),(int) (height/excessSizeRatio), true);
        return newBitmap;
    }

}
