package com.qiang.meidaproject.utils;

import android.app.Activity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Qiang on 2016/3/24.
 *
 *
 */
public class CommonUtil {

    /**
     * 动态修改tv的字体大小，已达到自适应显示的效果
     *
     * @param tv
     *            EditText 或 TextView
     * @param isWholeScreen
     *            true 宽度为占1/3屏 false 半屏
     */
    public static void adaptiveTextSize( Object tv, boolean isWholeScreen ) {

        String text = "";
        int textSize = 12;
        if( tv instanceof EditText ) {
            text = ( ( EditText )tv ).getText().toString();
        } else if( tv instanceof TextView ) {
            text = ( ( TextView )tv ).getText().toString();
        }

        if( !isWholeScreen ) {
            if( text.length() < 12 ) {
                textSize = 24;
            } else if( text.length() < 14 ) {
                textSize = 20;
            } else if( text.length() < 17 ) {
                textSize = 16;
            } else {
                textSize = 12;
            }
        } else {
            if( text.length() < 12 ) {
                textSize = 22;
            } else if( text.length() < 14 ) {
                textSize = 18;
            } else if( text.length() < 17 ) {
                textSize = 14;
            } else {
                textSize = 10;
            }
        }

        if( tv instanceof EditText) {
            ( ( EditText )tv ).setTextSize( TypedValue.COMPLEX_UNIT_SP, textSize );
        } else if( tv instanceof TextView) {
            ( ( TextView )tv ).setTextSize( TypedValue.COMPLEX_UNIT_SP, textSize );
        }
    }


    /**
     * 给edittext赋值
     **/
    public static void setViewText( Activity act, Integer editId, String text ) {

        if( TextUtils.isEmpty(text) ) {
            text = "";
        }

        View tv = act.findViewById( editId );

        if( tv instanceof EditText ) {
            ( ( EditText )tv ).setText( text );
        } else if( tv instanceof TextView ) {
            ( ( TextView )tv ).setText( text );
        }

        CommonUtil.adaptiveTextSize( tv, false );
    }

}
