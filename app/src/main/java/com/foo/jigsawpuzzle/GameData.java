package com.foo.jigsawpuzzle;

import android.graphics.Bitmap;

/**
 * @desc: 小方块要绑定的数据
 * @author: Major
 * @since: 2017/1/4 0:58
 */
class GameData {

    // 实际位置
    public int    x;
    public int    y;
    // 图片
    public Bitmap bm;
    // 图片的位置
    public int    p_x;
    public int    p_y;

    public GameData(int x, int y, Bitmap bm) {
        this.x = x;
        this.y = y;
        this.bm = bm;
        p_x = x;
        p_y = y;
    }

    // 位置是否正确
    public boolean isTrue() {
        return x == p_x && y == p_y;
    }
}