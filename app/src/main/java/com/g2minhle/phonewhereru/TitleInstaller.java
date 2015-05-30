package com.g2minhle.phonewhereru;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.tiles.BandTile;
import com.microsoft.band.tiles.pages.FlowPanel;
import com.microsoft.band.tiles.pages.FlowPanelOrientation;
import com.microsoft.band.tiles.pages.PageData;
import com.microsoft.band.tiles.pages.PageLayout;
import com.microsoft.band.tiles.pages.TextBlock;
import com.microsoft.band.tiles.pages.TextBlockData;
import com.microsoft.band.tiles.pages.TextBlockFont;

import java.util.List;
import java.util.UUID;

/**
 * Created by root on 5/30/15.
 */
public class TitleInstaller extends AsyncTask<Void, Void, Void> {


    private TextView txt_Status;
    private Activity baseActivity;
    private BandClient client = null;

    // Phone! Where R U
    public  static UUID tileId = UUID.fromString("50686F6E-6521-2057-6865-726520522055");
    // PhoneWhereRUMain
    public  static UUID mainPage = UUID.fromString("50686F6E-6557-6865-7265-52554D61696E");


    public TitleInstaller(Activity baseActivity, TextView txt_Status) {
        this.txt_Status = txt_Status;
        this.baseActivity = baseActivity;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            if (getConnectedBandClient()) {
                appendToUI("Band is connected.\n");
                if (addTile()) {
                    updatePages();
                }
            } else {
                appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
            }

        } catch (BandException e) {
            String exceptionMessage = "";
            switch (e.getErrorType()) {
                case DEVICE_ERROR:
                    exceptionMessage = "Please make sure bluetooth is on and the band is in range.";
                    break;
                case UNSUPPORTED_SDK_VERSION_ERROR:
                    exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.";
                    break;
                case SERVICE_ERROR:
                    exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.";
                    break;
                case BAND_FULL_ERROR:
                    exceptionMessage = "Band is full. Please use Microsoft Health to remove a tile.";
                    break;
                default:
                    exceptionMessage = "Unknown error occured: " + e.getMessage();
                    break;
            }
            appendToUI(exceptionMessage);

        } catch (Exception e) {
            appendToUI(e.getMessage());
        }
        return null;
    }

    private void appendToUI(final String string) {
        this.baseActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt_Status.append(string);
            }
        });
    }

    private boolean doesTileExist(List<BandTile> tiles, UUID tileId) {
        for (BandTile tile : tiles) {
            if (tile.getTileId().equals(tileId)) {
                return true;
            }
        }
        return false;
    }

    private boolean addTile() throws Exception {
        if (doesTileExist(client.getTileManager().getTiles().await(), tileId)) {
            return true;
        }

        /* Set the options */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap tileIcon = BitmapFactory.decodeResource(
                this.baseActivity.getBaseContext().getResources(), R.raw.phone_where_ru, options);

        BandTile tile = new BandTile.Builder(tileId, "Phone!WhereRU Tile", tileIcon)
                .setPageLayouts(createLayout())
                .build();
        appendToUI("Phone!WhereRU Tile is adding ...\n");
        if (client.getTileManager().addTile(baseActivity, tile).await()) {
            appendToUI("Phone!WhereRU Tile is added.\n");
            return true;
        } else {
            appendToUI("Unable to add Phone!WhereRU tile to the band.\n");
            return false;
        }
    }

    private PageLayout createLayout() {
        return new PageLayout(
                new FlowPanel(15, 0, 245, 105, FlowPanelOrientation.VERTICAL)
                        .addElements(new TextBlock(0, 0, 230, 30, TextBlockFont.SMALL, 0)
                                .setId(21).setColor(Color.WHITE)));
    }

    private void updatePages() throws BandIOException {
        client.getTileManager().setPages(tileId,
                new PageData(mainPage, 0)
                        .update(new TextBlockData(21, "Let's find the phone !! ")));
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                appendToUI("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(
                    this.baseActivity.getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }
}
