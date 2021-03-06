package android.quikkly.net.samples;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import net.quikkly.android.scanning.scanner.CameraStateListener;
import net.quikkly.android.scanning.scanner.ScanFragment;
import net.quikkly.android.scanning.scanner.ScanResultListener;
import android.quikkly.net.samples.R;
import net.quikkly.java.scan.ScanResult;
import net.quikkly.java.scan.Symbol;

public class SocialScannerActivity extends AppCompatActivity implements CameraStateListener, ScanResultListener {

    private ScanFragment mScanFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.social_scanner_activity);
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle(R.string.scan_title);

        // We create an instance of the ScanFragment.
        // The ScanFragment wraps the detector and provides mechanisms to listen for the scan result.
        mScanFragment = new ScanFragment();
        mScanFragment.setScanResultListener(this);
        mScanFragment.setCameraStateListener(this);

        FragmentManager fragmentManager = super.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.social_scanner_activity, mScanFragment);
        fragmentTransaction.commit();


    }

    @Override
    public void onCameraReady(int width, int height, int rotation) {
        mScanFragment.getScannableBounds(true);
        mScanFragment.resumeScanning(true);
    }

    @Override
    public void onCameraError(final @Nullable String error) {
        super.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SocialScannerActivity.this, "Camera error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public @Nullable Symbol onScanResult(@Nullable ScanResult scanResult) {
        if (scanResult == null) {
            return null;
        }
        else {
            for (Symbol symbol : scanResult.getSymbols()) {
                if (symbol.isValid()) {

                    // We have a result, pause the scanning.
                    mScanFragment.pauseScanning(true);

                    // DO SOMETHING WITH THE RESULT
                    String id = symbol.getData();
                    if(id.isEmpty() == false) {
                        long longId = Long.decode(id);
                        User user = ProfileHelper.getInstance().getUser(longId);
                        if(user != null) {
                            String successMessage = String.format(getString(R.string.user_follow_success), user.getUsername());
                            showAlert(successMessage);
                        } else {
                            showAlert(getString(R.string.user_follow_error));
                        }
                    }
                }
            }

            // Returning null here as there isn't a need to pass the Symbol (Scannable) back up...
            return null;
        }
    }

    private void showAlert(String message) {

        // Set click listener for alert dialog buttons, in this instance to restart the scanner.
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mScanFragment.resumeScanning(true);
            }
        };

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Add Contact");
        dialogBuilder.setMessage(message);
        dialogBuilder.setNegativeButton("Dismiss", dialogClickListener);

        super.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog dialog = dialogBuilder.create();
                dialog.setCancelable(false);
                dialog.show();
            }
        });

    }

}