package com.foo.jigsawpuzzle;

/**
 * @desc: TODO
 * @author: Major
 * @since: 2017/1/4 1:30
 */

public enum DirectionEnum {

    LEFT("左"), UP("上"), RIGHT("右"), DOWN("下");

    private String mDesc;

    DirectionEnum(String des) {
        mDesc = des;
    }

    @Override
    public String toString() {
        return mDesc;
    }


}

