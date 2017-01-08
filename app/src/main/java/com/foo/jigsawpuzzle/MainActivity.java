package com.foo.jigsawpuzzle;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PADDING = 4; // 图片之间的间隙

    private ImageView[][] mArr = new ImageView[3][5];

    // 空方块
    private ImageView       mNullImageView;
    private GestureDetector mDetector; // 手势识别

    private boolean    isGameStart;
    private boolean    isAnimRun;
    private GridLayout mGridLayout;

    private int mCount = 10;// 打乱步数
    private TextView mScore;
    private TextView mStep;
    private TextView mAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initListener();
        mScore = (TextView)findViewById(R.id.tv_main_score);
        mStep = (TextView)findViewById(R.id.tv_main_step);
        mAnswer = (TextView)findViewById(R.id.tv_main_answer);
        findViewById(R.id.btn_main_restart).setOnClickListener(this);
        mGridLayout = (GridLayout)findViewById(R.id.gl_main_game);

        initData();
    }

    private void initData() {// 获取大图
        isGameStart = false;
        mCount = 10;
        mGridLayout.removeAllViews();
        Bitmap srcBm = ((BitmapDrawable)getResources().getDrawable(R.mipmap.lm)).getBitmap();
        int width = (srcBm.getWidth()) / 5;
        int ivWandH = getWindowManager().getDefaultDisplay().getWidth() / 5; // 缩放后每个图块宽度
        for (int i = 0; i < mArr.length; i++) {
            for (int j = 0; j < mArr[0].length; j++) {
                // 切割大图
                Bitmap bm = Bitmap.createBitmap(srcBm, j * width, i * width, width, width);
                ImageView iv = new ImageView(this);
                iv.setImageBitmap(bm);
                iv.setLayoutParams(new RelativeLayout.LayoutParams(ivWandH, ivWandH));
                iv.setPadding(PADDING, PADDING, PADDING, PADDING);
                iv.setTag(new GameData(i, j, bm));
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean hasByNullImageView = canChange((ImageView)v);
                        if (hasByNullImageView) {
                            changeData((ImageView)v, true);
                        }
                    }
                });
                mArr[i][j] = iv;
                mGridLayout.addView(iv);
            }
        }

        // 设置空方块
        setNullImageView(mArr[2][4]);

        randomMove();

        isGameStart = true;
    }

    private void initListener() {
        mDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                DirectionEnum type = getDirByGes(e1.getX(), e1.getY(), e2.getX(), e2.getY());
                changeByDir(type);

                return false;
            }
        });
    }

    // 随机打乱
    public void randomMove() {
        mScore.setText("" + mCount);
        mStep.setText("");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mCount; i++) {
            int type = (int)((Math.random() * 4));
            DirectionEnum[] enums = new DirectionEnum[]{DirectionEnum.LEFT, DirectionEnum.UP,
                    DirectionEnum.RIGHT, DirectionEnum.DOWN};
            changeByDir(enums[type], false);
            LogUtil.printE("type " + enums[type]);
            mStep.append(enums[type].toString() + ", ");
            sb.append(getReverse(enums[type])).append(", ");
        }
        String answer = sb.toString().substring(0, sb.toString().length() - 2);
        mAnswer.setText(TextUtils.getReverse(answer, 0, answer.length()));
    }


    public String getReverse(DirectionEnum dir) {
        switch (dir) {
            case LEFT:
                return DirectionEnum.RIGHT.toString();
            case UP:
                return DirectionEnum.DOWN.toString();
            case RIGHT:
                return DirectionEnum.LEFT.toString();
            case DOWN:
                return DirectionEnum.UP.toString();
        }
        return null;
    }

    /**
     * @param type   方向
     * @param isAnim 是否有动画
     */
    public void changeByDir(DirectionEnum type, boolean isAnim) {
        GameData mNullGameData = (GameData)mNullImageView.getTag();
        int newX = mNullGameData.x;
        int newY = mNullGameData.y;
        switch (type) {
            case UP:
                newX++;
                break;
            case DOWN:
                newX--;
                break;
            case LEFT:
                newY++;
                break;
            case RIGHT:
                newY--;
                break;
        }

        if (newX >= 0 && newX < mArr.length && newY >= 0 && newY < mArr[0].length) {

            changeData(mArr[newX][newY], isAnim);
        }
    }

    public void changeByDir(DirectionEnum type) {
        changeByDir(type, true);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 交换方块的数据
     *
     * @param isAnim 是否动画
     */
    public void changeData(final ImageView imageView, final boolean isAnim) {
        if (isAnimRun) {
            return;
        }
        if (!isAnim) {
            changeDataWithoutAnim(imageView);

            if (isGameOver()) {
                gameOver();
            }
        } else {
            // 创建动画，设置方向，移动距离
            TranslateAnimation ta;
            float toXDelta = 0.1f;
            float toYDelta = 0.1f;
            float imageWidth = imageView.getWidth();

            if (imageView.getX() > this.mNullImageView.getX()) {
                // 点击方块在空方块下，往上移动
                toXDelta = -imageWidth;
            } else if (imageView.getX() < this.mNullImageView.getX()) {
                // 右移
                toXDelta = imageWidth;
            } else if (imageView.getY() > this.mNullImageView.getY()) {
                // 左移
                toYDelta = -imageWidth;
            } else if (imageView.getY() < this.mNullImageView.getY()) {
                // 下移
                toYDelta = imageWidth;
            }
            ta = new TranslateAnimation(0.1f, toXDelta, 0.1f, toYDelta);
            ta.setDuration(70);
            ta.setFillAfter(true);
            ta.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                    isAnimRun = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    isAnimRun = false;
                    imageView.clearAnimation();
                    // 交换数据
                    changeDataWithoutAnim(imageView);

                    if (isGameOver()) {
                        gameOver();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {


                }
            });

            imageView.startAnimation(ta);
        }
    }

    private void gameOver() {
        isGameStart = false;
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setCancelable(false)
                .setMessage("恭喜通关!")
                .setPositiveButton("继续游戏", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCount += 10;
                        randomMove();
                        isGameStart = true;
                    }
                })
                .show();
    }

    private void changeDataWithoutAnim(ImageView imageView) {
        GameData mGameData = (GameData)imageView.getTag();
        mNullImageView.setImageBitmap(mGameData.bm);
        GameData mNullGameData = (GameData)this.mNullImageView.getTag();
        mNullGameData.bm = mGameData.bm;
        mNullGameData.p_x = mGameData.p_x;
        mNullGameData.p_y = mGameData.p_y;

        setNullImageView(imageView);
    }

    public boolean isGameOver() {
        if (!isGameStart) {
            return false;
        }
        boolean isGameOver = true;
        for (int i = 0; i < mArr.length; i++) {
            for (int j = 0; j < mArr[0].length; j++) {
                if (mArr[i][j] == mNullImageView) {
                    continue;
                }
                GameData mGameData = (GameData)mArr[i][j].getTag();
                if (!mGameData.isTrue()) {
                    isGameOver = false;
                    break;
                }
            }
        }

        return isGameOver;
    }

    /**
     * 手势判断
     */
    public DirectionEnum getDirByGes(float startX, float startY, float endX, float endY) {
        boolean isHorizontal = Math.abs(startX - endX) > Math.abs(startY - endY);
        if (isHorizontal) {
            // 左右
            boolean isLeft = startX > endX;
            if (isLeft) {
                return DirectionEnum.LEFT;
            } else {
                return DirectionEnum.RIGHT;
            }
        } else {
            // 上下
            boolean isUp = startY > endY;
            if (isUp) {
                return DirectionEnum.UP;
            } else {
                return DirectionEnum.DOWN;
            }
        }
    }

    /** 设置空方块 */
    public void setNullImageView(ImageView imageView) {
        imageView.setImageBitmap(null);
        mNullImageView = imageView;
    }

    /**
     * 交换逻辑
     *
     * @return false 不能交换
     */
    public boolean canChange(ImageView mImageView) {
        GameData mGameData = (GameData)mImageView.getTag();
        GameData mNullGameData = (GameData)this.mNullImageView.getTag();

        if (mNullGameData.y == mGameData.y
            && mGameData.x + 1 == mNullGameData.x) {
            // 点击方块在空方块上
            return true;
        } else if (mNullGameData.y == mGameData.y
                   && mGameData.x - 1 == mNullGameData.x) {
            // 下
            return true;
        } else if (mNullGameData.y - 1 == mGameData.y
                   && mGameData.x == mNullGameData.x) {
            // 左
            return true;
        } else if (mNullGameData.y + 1 == mGameData.y
                   && mGameData.x == mNullGameData.x) {
            // 右
            return true;
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_main_restart:

                initData();
                break;
        }
    }
}
