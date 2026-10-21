package com.tank171.qrreader;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView resultText;
    private Button copyButton;
    private Button openButton;
    private String decodedText = "";

    private final ActivityResultLauncher<ScanOptions> cameraScanner =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Pemindaian dibatalkan.", Toast.LENGTH_SHORT).show();
                } else {
                    showResult(result.getContents());
                }
            });

    private final ActivityResultLauncher<String> galleryPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) decodeGalleryImage(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showSplash();
        new Handler(Looper.getMainLooper()).postDelayed(this::buildScreen, 2000);
    }

    private void showSplash() {
        LinearLayout splash = new LinearLayout(this);
        splash.setOrientation(LinearLayout.VERTICAL);
        splash.setGravity(Gravity.CENTER);
        splash.setPadding(dp(28), dp(28), dp(28), dp(28));
        splash.setBackgroundColor(0xFF071426);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_qr_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(132), dp(132));
        logoParams.bottomMargin = dp(22);
        splash.addView(logo, logoParams);

        TextView title = new TextView(this);
        title.setText("QR Reader 171");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        splash.addView(title, matchWrap());

        TextView brand = new TextView(this);
        brand.setText("Tank171 app");
        brand.setTextColor(0xFF38DDF8);
        brand.setTextSize(13);
        brand.setLetterSpacing(0.08f);
        brand.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams brandParams = matchWrap();
        brandParams.topMargin = dp(8);
        splash.addView(brand, brandParams);

        setContentView(splash);
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(24), dp(22), dp(24));
        root.setGravity(Gravity.TOP);
        root.setBackgroundColor(0xFF071426);
        scroll.addView(root);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_qr_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        header.addView(logo, new LinearLayout.LayoutParams(dp(66), dp(66)));

        LinearLayout titleGroup = new LinearLayout(this);
        titleGroup.setOrientation(LinearLayout.VERTICAL);
        titleGroup.setPadding(dp(14), 0, 0, 0);
        titleGroup.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("QR Reader 171");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(23);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        titleGroup.addView(title, matchWrap());

        TextView watermark = new TextView(this);
        watermark.setText("Tank171 app");
        watermark.setTextColor(0xFF38DDF8);
        watermark.setTextSize(11);
        watermark.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams watermarkParams = matchWrap();
        watermarkParams.topMargin = dp(3);
        titleGroup.addView(watermark, watermarkParams);

        header.addView(titleGroup, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        root.addView(header, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("Pindai QR dari kamera atau gambar");
        subtitle.setTextColor(0xFFB8C8D9);
        subtitle.setTextSize(14);
        LinearLayout.LayoutParams subParams = matchWrap();
        subParams.topMargin = dp(22);
        subParams.bottomMargin = dp(22);
        root.addView(subtitle, subParams);

        Button cameraButton = makeButton("Pindai dengan Kamera", false);
        cameraButton.setOnClickListener(v -> startCameraScan());
        root.addView(cameraButton, buttonParams());

        Button galleryButton = makeButton("Pilih Gambar dari Galeri", true);
        galleryButton.setOnClickListener(v -> galleryPicker.launch("image/*"));
        LinearLayout.LayoutParams galleryParams = buttonParams();
        galleryParams.topMargin = dp(12);
        root.addView(galleryButton, galleryParams);

        TextView resultHeading = new TextView(this);
        resultHeading.setText("HASIL PEMINDAIAN");
        resultHeading.setTextColor(0xFF38DDF8);
        resultHeading.setTextSize(12);
        resultHeading.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams headingParams = matchWrap();
        headingParams.topMargin = dp(28);
        headingParams.bottomMargin = dp(10);
        root.addView(resultHeading, headingParams);

        resultText = new TextView(this);
        resultText.setText("Hasil QR akan muncul di sini.");
        resultText.setTextColor(0xFFEAF2FA);
        resultText.setTextSize(16);
        resultText.setTextIsSelectable(true);
        resultText.setPadding(dp(16), dp(16), dp(16), dp(16));
        GradientDrawable resultBackground = new GradientDrawable();
        resultBackground.setColor(0xFF10243A);
        resultBackground.setCornerRadius(dp(16));
        resultBackground.setStroke(dp(1), 0xFF244663);
        resultText.setBackground(resultBackground);
        root.addView(resultText, matchWrap());

        copyButton = makeButton("Salin Hasil", true);
        copyButton.setEnabled(false);
        copyButton.setOnClickListener(v -> copyResult());
        LinearLayout.LayoutParams copyParams = buttonParams();
        copyParams.topMargin = dp(12);
        root.addView(copyButton, copyParams);

        openButton = makeButton("Buka Tautan", false);
        openButton.setVisibility(View.GONE);
        openButton.setOnClickListener(v -> openLink());
        LinearLayout.LayoutParams openParams = buttonParams();
        openParams.topMargin = dp(10);
        root.addView(openButton, openParams);

        TextView footer = new TextView(this);
        footer.setText("Tautan hanya dibuka saat kamu menekan tombol.");
        footer.setTextColor(0xFF8FA6BC);
        footer.setTextSize(12);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerParams = matchWrap();
        footerParams.topMargin = dp(22);
        root.addView(footer, footerParams);

        setContentView(scroll);
    }

    private void startCameraScan() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Arahkan kamera ke QR Code");
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        cameraScanner.launch(options);
    }

    private void decodeGalleryImage(Uri uri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source, (decoder, info, src) ->
                        decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE));
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
            if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                showError("Gambar tidak bisa dibaca. Coba pilih gambar lain.");
                return;
            }
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binary = new BinaryBitmap(new HybridBinarizer(source));
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(BarcodeFormat.QR_CODE));
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            try {
                Result result = new MultiFormatReader().decode(binary, hints);
                showResult(result.getText());
            } catch (NotFoundException e) {
                showError("QR Code tidak ditemukan pada gambar. Coba gambar yang lebih jelas.");
            } finally {
                bitmap.recycle();
            }
        } catch (OutOfMemoryError e) {
            showError("Gambar terlalu besar untuk diproses. Pilih gambar yang lebih kecil.");
        } catch (Exception e) {
            showError("Foto tidak bisa dibaca. Coba pilih gambar JPG atau PNG yang lain.");
        }
    }

    private void showResult(String text) {
        decodedText = text == null ? "" : text;
        resultText.setText(decodedText.isEmpty() ? "QR tidak berisi teks." : decodedText);
        copyButton.setEnabled(!decodedText.isEmpty());
        openButton.setVisibility(isHttpUrl(decodedText) ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        decodedText = "";
        resultText.setText(message);
        copyButton.setEnabled(false);
        openButton.setVisibility(View.GONE);
    }

    private boolean isHttpUrl(String value) {
        if (value == null) return false;
        String lower = value.trim().toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("https://") || lower.startsWith("http://");
    }

    private void copyResult() {
        if (decodedText.isEmpty()) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("QR Reader 171", decodedText));
            Toast.makeText(this, "Hasil QR disalin.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Clipboard tidak tersedia.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openLink() {
        if (!isHttpUrl(decodedText)) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(decodedText.trim()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Tidak ada aplikasi untuk membuka tautan ini.", Toast.LENGTH_SHORT).show();
        }
    }

    private Button makeButton(String text, boolean secondary) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(15);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        button.setTextColor(secondary ? 0xFFBDEFFA : 0xFF061526);
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(dp(16));
        if (secondary) {
            background.setColor(0xFF102943);
            background.setStroke(dp(1), 0xFF23769A);
        } else {
            background.setColor(0xFF32D9F5);
        }
        button.setBackground(background);
        button.setBackgroundTintList(null);
        button.setElevation(dp(2));
        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = matchWrap();
        params.height = dp(56);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
