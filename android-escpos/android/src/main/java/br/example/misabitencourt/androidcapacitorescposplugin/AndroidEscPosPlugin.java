package br.example.misabitencourt.androidcapacitorescposplugin;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.dantsu.escposprinter.EscPosCharsetEncoding;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.EscPosPrinterCommands;
import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "AndroidEscPos")
public class AndroidEscPosPlugin extends Plugin {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private AndroidEscPos implementation = new AndroidEscPos();

    public EscPosPrinterCommands printerCommands;

    public void requestUsb(BroadcastReceiver usbReceiver) {
        UsbConnection usbConnection = UsbPrintersConnections.selectFirstConnected(this.getContext());
        UsbManager usbManager = (UsbManager) this.getContext().getSystemService(Context.USB_SERVICE);

        if (usbConnection != null && usbManager != null) {
            PendingIntent permissionIntent = PendingIntent.getBroadcast(this.getContext(), 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            this.getContext().registerReceiver(usbReceiver, filter);
            usbManager.requestPermission(usbConnection.getDevice(), permissionIntent);
        }
    }

    @PluginMethod
    public void echo(PluginCall call) {
        // THIS IS JUST A SAMPLE
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void print(final PluginCall call) {
        final String linesStr = call.getString("template");
        AndroidEscPosPlugin parent = this;
        final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        UsbManager usbManager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);
                        UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, true)) {
                            if (usbManager != null && usbDevice != null) {
                                try {
                                    EscPosPrinter printer;
                                    UsbConnection conn = new UsbConnection(usbManager, usbDevice);
                                    parent.printerCommands = new EscPosPrinterCommands(conn);
                                    printer = new EscPosPrinter(conn, 90, 48f, 1,
                                            new EscPosCharsetEncoding("860", 3));
                                    try {
                                        printer.printFormattedTextAndCut(linesStr);
                                        getContext().unregisterReceiver(this);
                                        call.resolve();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        call.reject(e.toString());
                                        getContext().unregisterReceiver(this);
                                    }
                                    printer.disconnectPrinter();
                                } catch (EscPosConnectionException e) {
                                    e.printStackTrace();
                                    call.reject(e.toString());
                                }
                            } else {
                                call.reject("Printer not found");
                            }
                        }
                    }
                }
            }
        };
        this.requestUsb(usbReceiver);
    }

}
