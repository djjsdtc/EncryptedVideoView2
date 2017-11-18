package cookiework.encryptedvideoview2.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static cookiework.encryptedvideoview2.Constants.*;

/**
 * Created by Administrator on 2017/01/16.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String CREATE_STATEMENT =
            "create table viewertag (id int primary key, tag text, r text, N text, sigma text)";
    private static final String SELECT_STATEENT =
            "select * from viewertag where id=?";
    private static final String INSERT_STATEMENT =
            "insert into viewertag(id, tag, r, N) values(?,?,?,?)";
    private static final String UPDATE_STATEMENT =
            "update viewertag set sigma=? where id=?";
    private static final String DELETE_STATEMENT =
            "delete from viewertag where id=?";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STATEMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ViewerTag getTagItem(int id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_STATEENT, new String[]{Integer.toString(id)});
        ViewerTag result = null;
        if(cursor.moveToNext()){
            result = new ViewerTag();
            result.setId(id);
            result.setTag(cursor.getString(cursor.getColumnIndex("tag")));
            result.setR(cursor.getString(cursor.getColumnIndex("r")));
            result.setSigma(cursor.getString(cursor.getColumnIndex("sigma")));
            result.setN(cursor.getString(cursor.getColumnIndex("N")));
        }
        cursor.close();
        db.close();
        return result;
    }

    public void addTagItem(ViewerTag tag){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(DELETE_STATEMENT, new Object[]{tag.getId()});
        db.execSQL(INSERT_STATEMENT, new Object[]{tag.getId(), tag.getTag(), tag.getR(), tag.getN()});
        db.close();
    }

    public void updateTagItem(ViewerTag tag){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(UPDATE_STATEMENT, new Object[]{tag.getSigma(), tag.getId()});
        db.close();
    }
}
