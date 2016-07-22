package braingames.memorygame;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * @author Miguel Alejandro Moreno Barrientos, @2016
 * @version 1.0
 */
public class MemoryGame extends AppCompatActivity implements View.OnClickListener
{
    private static final String FILENAME_RECORDS = "records";

    private int moves;
    private int rows;
    private int cols;
    private Card[][] cardGrid;
    private CardButton firstCardButton, secondCardButton;
    private final Dimen[] dimens = new Dimen[] {
        new Dimen( 3, 4 ),
        new Dimen( 4, 4 ),
        new Dimen( 5, 4 ),
        new Dimen( 6, 5 ),
        new Dimen( 6, 6 ),
        new Dimen( 7, 6 ),
        new Dimen( 8, 6 )
    };

    // IMAGES
    private int backward;
    private int[] images;

    // GUI
    private LinearLayout mainLayout;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        // save an empty record file if it doesn't exist
        checkRecordFile();

        // init size to first size item
        rows = dimens[0].rows;
        cols = dimens[0].cols;

        // set resource images
        backward = R.mipmap.backcard;
        images = new int[] {
                R.mipmap.bart,
                R.mipmap.homer,
                R.mipmap.lisa,
                R.mipmap.marge,
                R.mipmap.maggie,
                R.mipmap.flanders,
                R.mipmap.milhouse,
                R.mipmap.skinner,
                R.mipmap.nelson,
                R.mipmap.mr_burns,
                R.mipmap.smithers,
                R.mipmap.abraham,
                R.mipmap.lenny,
                R.mipmap.carl,
                R.mipmap.quimby,
                R.mipmap.wiggum,
                R.mipmap.eddie,
                R.mipmap.lou,
                R.mipmap.santa,
                R.mipmap.snowball,
                R.mipmap.rod_tod,
                R.mipmap.edna,
                R.mipmap.patty,
                R.mipmap.selma
        };

        // avoid landscape
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

        // game protection
        if ( PROTECTED )
        {
            final EditText editPass = new EditText(this);
            new AlertDialog.Builder( this )
                    .setTitle("Access")
                    .setMessage("Tell me something")
                    .setView(editPass)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            if ( !editPass.getText().toString().trim().equals( PASSWORD ) )
                                finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            finish();
                        }
                    })
                    /*.setCancelable( false )*/
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog)
                        {
                            finish();
                        }
                    })
                    .show();
        }

        // mainLayout
        mainLayout = new LinearLayout( this );
        mainLayout.setLayoutParams( new ViewGroup.LayoutParams( MATCH_PARENT, MATCH_PARENT ) );
        mainLayout.setOrientation( LinearLayout.VERTICAL );
        mainLayout.setBackgroundColor( Color.parseColor( "#7777ff" ) );
        setContentView( mainLayout );

        mainLayout.post( new Runnable() {
            @Override
            public void run() {
                createGrid();
            }
        });
    }

    private void createGrid()
    {
        // reset values
        firstCardButton = secondCardButton = null;
        moves = 0;

        // create logic grid
        cardGrid = new Card[rows][cols];
        List<Integer> deck = new ArrayList<>( images.length );
        for ( int i = 0; i < images.length; i++ )
            deck.add(i);
        Collections.shuffle( deck );
        List<Integer> hand = new ArrayList<>( getCardCount() );
        for ( int i = 0; i < getPairs(); i++ )
        {
            hand.add( deck.get(i) );
            hand.add( deck.get(i) );
        }
        Collections.shuffle( hand );
        for ( int row = 0; row < rows; row++ )
            for ( int col = 0; col < cols; col++ )
                cardGrid[row][col] = new Card( hand.remove(0), Card.HIDDEN );

        // get actionbar height
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        //styledAttributes.recycle();

        // statusbar height
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = Math.max( rectangle.top, 24 );

        // set dimens
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics( metrics );
        int margin = 1;
        int width = 0;
        int height = ( metrics.heightPixels - statusBarHeight - actionBarHeight
                                                                    - 2 * margin * rows ) / rows;
        float weight = 1f;

        // create GUI grid
        final List<CardButton> btnList = new ArrayList<>( getCardCount() );
        for ( int row = 0; row < rows; row++ )
        {
            LinearLayout linear = new LinearLayout( this );
            mainLayout.addView( linear );
            for ( int col = 0; col < cols; col++ )
            {
                CardButton btn = new CardButton( this, cardGrid[row][col] );
                btnList.add( btn );
                linear.addView( btn );
                btn.setLayoutParams( new LinearLayout.LayoutParams( width, height, weight ) );
                ( (LinearLayout.LayoutParams) btn.getLayoutParams() ).setMargins(
                                                                margin, margin, margin, margin );
                btn.requestLayout();
            }
        }

        mainLayout.post( new Runnable() {
            @Override
            public void run()
            {
                for ( CardButton btn : btnList )
                    btn.setImageBitmap( decodeSampledBitmapFromResource(
                                    getResources(), backward, btn.getWidth(), btn.getHeight() ) );
            }
        });
    }

    /**
     * Check for complete grid
     * @return true for game over
     */
    private boolean checkEndGame()
    {
        for ( int row = 0; row < rows; row++ )
            for ( int col = 0; col < cols; col++ )
                if ( cardGrid[row][col].getState() == Card.HIDDEN )
                    return false;

        return true;
    }


    ////////////////////////
    // IMAGE HANDLING (from developers doc)
    ////////////////////////

    public static int calculateInSampleSize( BitmapFactory.Options options,
                                             int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource( Resources res, int resId,
                                                          int reqWidth, int reqHeight )
    {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }


    /////////////////////////
    // INNER CLASSES
    /////////////////////////

    /**
     * Row/col dimension handler
     */
    private class Dimen
    {
        private Dimen( int rows, int cols )
        {
            this.rows = rows;
            this.cols = cols;
        }

        private int rows, cols;
    }

    /**
     * Custom ImageButton for cards
     */
    private class CardButton extends ImageButton
    {
        private Card card;

        private CardButton( Context context, Card card )
        {
            super( context );
            this.card = card;
            setBackground( null );
            setPadding( 0, 0, 0, 0 );
            setScaleType( ScaleType.FIT_XY );

            // set listeners
            setOnClickListener( MemoryGame.this );
        }

        Card getCard() { return card; }
    }

    /**
     * Card info
     */
    private class Card
    {
        private static final int TURNED = 0, HIDDEN = 1;  // Card states

        private int type;
        private int state;

        private Card( int type, int state )
        {
            this.type = type;
            this.state = state;
        }

        public int getType() { return type; }

        public int getState() { return state; }
        public void setState(int state) { this.state = state; }
    }


    ////////////////
    // MENU
    ////////////////

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        // add sizes
        for ( int i = 0; i < dimens.length; i++ )
            if ( images.length * 2 >= dimens[i].rows * dimens[i].cols )
            menu.add( Menu.NONE, i, Menu.NONE,
                      String.format( "New %dx%d game", dimens[i].rows, dimens[i].cols ) );
        // add reset record option
        menu.add( Menu.NONE, 1000, Menu.NONE, "Reset records" );

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        if ( item.getItemId() == 1000 )
        {
            if ( deleteFile( FILENAME_RECORDS ) )
            {
                Toast.makeText( this, "Records file deleted", Toast.LENGTH_LONG ).show();
                checkRecordFile();
            }
        }
        else
        {
            mainLayout.removeAllViews();
            rows = dimens[item.getItemId()].rows;
            cols = dimens[item.getItemId()].cols;
            createGrid();
        }

        return super.onOptionsItemSelected( item );
    }


    ////////////////
    // EVENTS
    ////////////////

    @Override
    public void onClick( final View v )
    {
        if ( !( v instanceof CardButton ) )  return;
        CardButton button = (CardButton) v;

        if ( button.getCard().getState() == Card.HIDDEN && secondCardButton == null )
        {
            // turn card
            button.setImageBitmap( decodeSampledBitmapFromResource( getResources(),
                    images[ button.getCard().getType() ], v.getWidth(), v.getHeight() ) );
            button.getCard().setState( Card.TURNED );
            // first movement
            if ( firstCardButton == null )
                firstCardButton = button;
                // second movement
            else
            {
                secondCardButton = button;
                moves++;
                if ( firstCardButton.getCard().getType() == button.getCard().getType() )  // pair
                {
                    firstCardButton = secondCardButton = null;
                    if ( checkEndGame() )  // game finished
                    {
                        Map<String,Integer> mapRecord = loadRecords();
                        if ( mapRecord == null )
                        {
                            Toast.makeText(MemoryGame.this,
                                    String.format("FINISHED in: %d", moves),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        Integer currentRecord = currentRecord = mapRecord.get( rows + "x" + cols );
                        if ( currentRecord == null || currentRecord > moves )
                        {
                            mapRecord.put( rows + "x" + cols, moves );
                            Toast.makeText( MemoryGame.this,
                                            String.format( "NEW RECORD: %d", moves ),
                                            Toast.LENGTH_LONG ).show();
                            saveRecords( mapRecord );
                        }
                        else
                            Toast.makeText( MemoryGame.this,
                                            String.format( "FINISHED in %d moves. Best score %d",
                                                           moves, currentRecord ),
                                             Toast.LENGTH_LONG ).show();
                    }
                }
                else  // player fails
                {
                    button.post( new Runnable() {
                        @Override
                        public void run()
                        {
                            try { Thread.sleep( 2000 ); } catch ( InterruptedException ignored ) {}
                            firstCardButton.setImageBitmap( decodeSampledBitmapFromResource(
                                    getResources(), backward, v.getWidth(), v.getHeight() ) );
                            firstCardButton.getCard().setState( Card.HIDDEN );
                            secondCardButton.setImageBitmap( decodeSampledBitmapFromResource(
                                    getResources(), backward, v.getWidth(), v.getHeight() ) );
                            secondCardButton.getCard().setState( Card.HIDDEN );
                            firstCardButton = secondCardButton = null;
                        }
                    });
                }
            }
        }
    }


    //////////////////
    // HELPERS
    //////////////////

    /**
     * Checks if the file exists, if not, create empty one
     */
    private void checkRecordFile()
    {
        try
        {
            openFileInput( FILENAME_RECORDS );
        }
        catch ( FileNotFoundException e )
        {
            // save empty map file
            ObjectOutputStream oos = null;
            try
            {
                oos = new ObjectOutputStream( openFileOutput( FILENAME_RECORDS, MODE_PRIVATE) );
                oos.writeObject( new HashMap<String,Integer>() );
                Log.d( "SAVING", "Saving " + oos.toString() );
            }
            catch ( IOException ex )
            {
                new AlertDialog.Builder( this )
                        .setTitle( "Can't create records file" )
                        .setMessage( ex.getMessage() )
                        .setPositiveButton( "Ok", null )
                        .create()
                        .show();
            }
            finally
            {
                if ( oos != null )
                    try
                    {
                        oos.close();
                    }
                    catch ( IOException ex )
                    {
                        ex.printStackTrace();
                    }
            }
        }
    }

    /**
     * Load records map
     * @return records map or null if an error occurs
     */
    private Map<String,Integer> loadRecords()
    {
        ObjectInputStream ois = null;
        try
        {
            ois = new ObjectInputStream( openFileInput( FILENAME_RECORDS ) );

            return (Map<String,Integer>) ois.readObject();
        }
        catch ( IOException|ClassNotFoundException e )
        {
            new AlertDialog.Builder( this )
                    .setTitle( "Can't load records file" )
                    .setMessage( e.getMessage() )
                    .setPositiveButton( "Ok", null )
                    .create()
                    .show();
        }
        finally
        {
            if ( ois != null )
                try
                {
                    ois.close();
                }
                catch ( IOException ex )
                {
                    ex.printStackTrace();
                }
        }

        return null;
    }

    /**
     * Save records map
     * @param recordMap a likely modified records map
     */
    private void saveRecords( Map<String,Integer> recordMap )
    {
        ObjectOutputStream oos = null;
        try
        {
            oos = new ObjectOutputStream( openFileOutput( FILENAME_RECORDS, MODE_PRIVATE ) );
            oos.writeObject( recordMap );
        }
        catch ( IOException e )
        {
            new AlertDialog.Builder( this )
                    .setTitle( "Can't save records file" )
                    .setMessage( e.getMessage() )
                    .setPositiveButton( "Ok", null )
                    .create()
                    .show();
        }
        finally
        {
            if ( oos != null )
                try
                {
                    oos.close();
                }
                catch ( IOException ex )
                {
                    ex.printStackTrace();
                }
        }
    }

    private int getCardCount() { return rows * cols; }
    private int getPairs() { return getCardCount() / 2; }


    private final boolean PROTECTED = false;
    private final String PASSWORD = "malika";
}
