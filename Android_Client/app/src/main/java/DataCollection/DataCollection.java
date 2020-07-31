package DataCollection;

//import android.Manifest;
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.location.LocationManager;
//
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//public class DataCollection {
//    private static final int MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION = 1;
//    final Context context;
//    final Activity activity;
//    double longitude, latitude;
//
//    public DataCollection(Activity activity,Context context) {
//        this.context = context;
//        this.activity = activity;
//
//
//    }
//
//    public void get_coordinates(){
//        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if (location == null) {
//                locationManager.removeUpdates(activity);
//
//            } else {
//                System.out.println(location.getLongitude());
//                System.out.println(location.getAltitude());
//            }
//        } else {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION) && activity.isFinishing()) {
//                new AlertDialog.Builder(context)
//                        .setTitle("Permission Required")
//                        .setMessage("This app requires your gps signal in order to determine your position")
//                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                ActivityCompat.requestPermissions(activity,
//                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                                        MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION);
//                            }
//                        })
//                        .setNegativeButton("no", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                ActivityCompat.requestPermissions(activity,
//                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                                        MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION);
//                            }
//                        })
//                        .create()
//                        .show();
//
//            } else {
//                ActivityCompat.requestPermissions(activity,
//                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                        MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION);
//            }
//
//        }
//    }
//
//
//
//}
